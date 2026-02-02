package com.example.ridenow.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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
import com.example.ridenow.util.ClientUtils;
import com.example.ridenow.util.TokenUtils;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements MapEventsReceiver {

    private MapView mapView;
    private LocationManager locationManager;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;
    private VehicleService vehicleService;
    private TokenUtils tokenUtils;
    private Marker driverLocationMarker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize location services
        locationManager = (LocationManager) requireContext().getSystemService(android.content.Context.LOCATION_SERVICE);

        // Initialize API services
        vehicleService = ClientUtils.getClient(VehicleService.class);

        // Initialize token utils
        tokenUtils = new TokenUtils(requireContext());

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
        mapView = view.findViewById(R.id.mapView);
    }

    private void setupMap() {
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        IMapController mapController = mapView.getController();
        mapController.setZoom(12.0);

        // Add map events overlay to detect map movement
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
        mapView.getOverlays().add(0, mapEventsOverlay);

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
                    addDriverLocationMarker(location.getLatitude(), location.getLongitude());
                }
            }
        } catch (SecurityException e) {
            Log.e("HomeFragment", "Location permission error", e);
        }
    }

    private void centerMapOnLocation(double latitude, double longitude) {
        IMapController mapController = mapView.getController();
        GeoPoint point = new GeoPoint(latitude, longitude);
        mapController.setCenter(point);

        // Load vehicles around this location
        loadVehiclesAroundLocation(latitude, longitude);
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
        // Clear existing vehicle markers (but keep the map events overlay and driver marker)
        mapView.getOverlays().removeIf(overlay ->
            overlay instanceof Marker && overlay != driverLocationMarker);

        for (VehicleResponse vehicle : vehicles) {
            if (vehicle.getLocation() != null) {
                GeoPoint vehiclePoint = new GeoPoint(
                    vehicle.getLocation().getLatitude(),
                    vehicle.getLocation().getLongitude()
                );

                Marker marker = new Marker(mapView);
                marker.setPosition(vehiclePoint);
                marker.setTitle("Vehicle: " + vehicle.getLicencePlate());
                marker.setSnippet(vehicle.getAvailable() ? "Available" : "Not Available");

                // Set different icons based on availability
                if (vehicle.getAvailable()) {
                    // Green for available vehicles
                    marker.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_car_available));
                } else {
                    // Red for unavailable vehicles
                    marker.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_car_unavailable));
                }

                mapView.getOverlays().add(marker);
            }
        }

        mapView.invalidate();
    }

    // MapEventsReceiver interface methods
    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        return false;
    }

    @Override
    public boolean longPressHelper(GeoPoint p) {
        // When user long presses (moves map), load vehicles for the new center
        GeoPoint mapCenter = (GeoPoint) mapView.getMapCenter();
        loadVehiclesAroundLocation(mapCenter.getLatitude(), mapCenter.getLongitude());
        return true;
    }

    private boolean isUserDriver() {
        return tokenUtils != null && tokenUtils.isLoggedIn() && "DRIVER".equals(tokenUtils.getRole());
    }

    private void addDriverLocationMarker(double latitude, double longitude) {
        // Remove existing driver marker if it exists
        if (driverLocationMarker != null) {
            mapView.getOverlays().remove(driverLocationMarker);
        }

        GeoPoint driverPoint = new GeoPoint(latitude, longitude);
        driverLocationMarker = new Marker(mapView);
        driverLocationMarker.setPosition(driverPoint);
        driverLocationMarker.setTitle("Your Location");
        driverLocationMarker.setSnippet("Driver");

        // Use a distinctive blue marker for the driver
        driverLocationMarker.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_driver_location));

        // Add the driver marker to the map
        mapView.getOverlays().add(driverLocationMarker);
        mapView.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDetach();
        }
    }
}
