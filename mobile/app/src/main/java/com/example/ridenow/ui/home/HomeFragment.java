package com.example.ridenow.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.ridenow.R;
import com.example.ridenow.dto.vehicle.VehicleResponse;
import com.example.ridenow.service.VehicleService;
import com.example.ridenow.ui.components.RouteMapView;
import com.example.ridenow.util.ClientUtils;
import com.example.ridenow.util.TokenUtils;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements MapEventsReceiver {

    private static final long VEHICLE_UPDATE_INTERVAL = 10000; // 10 seconds

    private RouteMapView routeMapView;
    private LocationManager locationManager;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private VehicleService vehicleService;
    private TokenUtils tokenUtils;

    // Vehicle update timer
    private Handler vehicleUpdateHandler;
    private Runnable vehicleUpdateRunnable;
    private double currentCenterLatitude;
    private double currentCenterLongitude;
    private boolean isVehicleUpdateActive = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize location services
        locationManager = (LocationManager) requireContext().getSystemService(android.content.Context.LOCATION_SERVICE);

        // Initialize API services
        vehicleService = ClientUtils.getClient(VehicleService.class);

        // Initialize token utils
        tokenUtils = new TokenUtils(requireContext());

        // Initialize vehicle update handler
        vehicleUpdateHandler = new Handler(Looper.getMainLooper());
        setupVehicleUpdateRunnable();

        // Setup permission launcher
        locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                Boolean coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                if ((fineLocationGranted != null && fineLocationGranted) ||
                    (coarseLocationGranted != null && coarseLocationGranted)) {
                    centerMapOnUserLocation();
                } else {
                    // Center on default location (Belgrade)
                    centerMapOnLocation(44.8176, 20.4633);
                }
            }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(view);
        setupMap();

        return view;
    }

    private void initializeViews(View view) {
        routeMapView = view.findViewById(R.id.routeMapView);
    }

    private void setupMap() {
        // Set up map events receiver for detecting map movements
        routeMapView.setMapEventsReceiver(this);

        // Default to Belgrade and request user location
        centerMapOnLocation(44.8176, 20.4633);
        requestLocationPermission();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            centerMapOnUserLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void centerMapOnUserLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            Location location = null;

            // Try to get location from GPS first
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            // If GPS is not available, try network provider
            if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                centerMapOnLocation(location.getLatitude(), location.getLongitude());

                // Add driver marker if user is logged in as a driver
                if (isUserDriver()) {
                    routeMapView.addDriverLocation(location.getLatitude(), location.getLongitude());
                }
            }
        } catch (SecurityException e) {
            Log.e("HomeFragment", "Location permission error", e);
        }
    }

    private void centerMapOnLocation(double latitude, double longitude) {
        routeMapView.centerOnLocation(latitude, longitude);

        // Store current center coordinates
        currentCenterLatitude = latitude;
        currentCenterLongitude = longitude;

        // Load vehicles around this location and start periodic updates
        loadVehiclesAroundLocation(latitude, longitude);
        startVehicleUpdates();
    }

    private void loadVehiclesAroundLocation(double latitude, double longitude) {
        Call<List<VehicleResponse>> call = vehicleService.getAllVehicles(latitude, longitude);
        call.enqueue(new Callback<List<VehicleResponse>>() {
            @Override
            public void onResponse(Call<List<VehicleResponse>> call, Response<List<VehicleResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayVehiclesOnMap(response.body());
                } else {
                    Log.e("HomeFragment", "Failed to load vehicles: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<VehicleResponse>> call, Throwable t) {
                Log.e("HomeFragment", "Error loading vehicles", t);
                Toast.makeText(getContext(), "Failed to load vehicles", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayVehiclesOnMap(List<VehicleResponse> vehicles) {
        routeMapView.displayVehicles(vehicles);
    }

    // MapEventsReceiver interface methods
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        // When user long presses (moves map), load vehicles for the new center
        GeoPoint mapCenter = routeMapView.getMapCenter();
        if (mapCenter != null) {
            // Update stored coordinates
            currentCenterLatitude = mapCenter.getLatitude();
            currentCenterLongitude = mapCenter.getLongitude();

            loadVehiclesAroundLocation(mapCenter.getLatitude(), mapCenter.getLongitude());

            // Restart vehicle updates with new location
            startVehicleUpdates();
        }
        return true;
    }

    private boolean isUserDriver() {
        return tokenUtils != null && tokenUtils.isLoggedIn() && "DRIVER".equals(tokenUtils.getRole());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (routeMapView != null) {
            routeMapView.onResume();
        }
        // Restart vehicle updates if we have a location
        if (currentCenterLatitude != 0 && currentCenterLongitude != 0) {
            startVehicleUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (routeMapView != null) {
            routeMapView.onPause();
        }
        // Stop vehicle updates to save resources
        stopVehicleUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (routeMapView != null) {
            routeMapView.onDestroy();
        }
        // Stop vehicle updates and cleanup handler
        stopVehicleUpdates();
        if (vehicleUpdateHandler != null) {
            vehicleUpdateHandler.removeCallbacksAndMessages(null);
        }
    }

    private void setupVehicleUpdateRunnable() {
        vehicleUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isVehicleUpdateActive) {
                    // Update vehicles for current map center
                    loadVehiclesAroundLocation(currentCenterLatitude, currentCenterLongitude);

                    // Schedule next update
                    if (vehicleUpdateHandler != null) {
                        vehicleUpdateHandler.postDelayed(this, VEHICLE_UPDATE_INTERVAL);
                    }
                }
            }
        };
    }

    private void startVehicleUpdates() {
        stopVehicleUpdates(); // Stop any existing updates first

        isVehicleUpdateActive = true;
        if (vehicleUpdateHandler != null && vehicleUpdateRunnable != null) {
            vehicleUpdateHandler.postDelayed(vehicleUpdateRunnable, VEHICLE_UPDATE_INTERVAL);
        }
    }

    private void stopVehicleUpdates() {
        isVehicleUpdateActive = false;
        if (vehicleUpdateHandler != null && vehicleUpdateRunnable != null) {
            vehicleUpdateHandler.removeCallbacks(vehicleUpdateRunnable);
        }
    }
}
