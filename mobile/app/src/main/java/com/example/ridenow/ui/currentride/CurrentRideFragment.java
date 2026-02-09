package com.example.ridenow.ui.currentride;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.ridenow.R;
import com.example.ridenow.dto.driver.DriverLocationRequestDTO;
import com.example.ridenow.dto.driver.DriverLocationResponseDTO;
import com.example.ridenow.dto.model.LocationDTO;
import com.example.ridenow.dto.ride.CurrentRideResponse;
import com.example.ridenow.dto.ride.TrackVehicleResponseDTO;
import com.example.ridenow.service.DriverService;
import com.example.ridenow.service.RideService;
import com.example.ridenow.ui.components.RouteMapView;
import com.example.ridenow.util.AddressUtils;
import com.example.ridenow.util.ClientUtils;
import com.example.ridenow.util.TokenUtils;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurrentRideFragment extends Fragment {
    private static final String TAG = "CurrentRideFragment";
    private RouteMapView routeMapView;
    private TextView startAddressText;
    private TextView endAddressText;

    // User buttons
    private LinearLayout userButtonsLayout;
    private Button reportInconsistencyButton;
    private Button panicButton;

    // Driver buttons
    private LinearLayout driverButtonsLayout;
    private Button driverPanicButton;
    private Button stopRideButton;
    private Button markCompletedButton;

    private RideService rideService;
    private DriverService driverService;
    private CurrentRideResponse currentRide;
    private Handler trackingHandler;
    private Runnable trackingRunnable;
    private static final long TRACKING_INTERVAL = 10000; // 10 seconds

    private boolean isDriver = false;
    private TokenUtils tokenUtils;
    private LocationManager locationManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_ride, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initServices();
        checkUserRole();
        setupButtonVisibility();
        testCurrentLocation(); // Test what location the emulator is reporting
        getCurrentRide();
    }

    private void initViews(View view) {
        routeMapView = view.findViewById(R.id.routeMapView);
        startAddressText = view.findViewById(R.id.startAddressText);
        endAddressText = view.findViewById(R.id.endAddressText);

        // User buttons
        userButtonsLayout = view.findViewById(R.id.userButtonsLayout);
        reportInconsistencyButton = view.findViewById(R.id.reportInconsistencyButton);
        panicButton = view.findViewById(R.id.panicButton);

        // Driver buttons
        driverButtonsLayout = view.findViewById(R.id.driverButtonsLayout);
        driverPanicButton = view.findViewById(R.id.driverPanicButton);
        stopRideButton = view.findViewById(R.id.stopRideButton);
        markCompletedButton = view.findViewById(R.id.markCompletedButton);

        setupButtonListeners();
    }

    private void setupButtonListeners() {
        // User button listeners
        reportInconsistencyButton.setOnClickListener(v -> {
            if (currentRide != null && currentRide.getRideId() != null) {
                showReportInconsistencyDialog();
            } else {
                Toast.makeText(getContext(), "No active ride found", Toast.LENGTH_SHORT).show();
            }
        });

        driverPanicButton.setOnClickListener(v -> triggerPanicButton());
        panicButton.setOnClickListener(v -> triggerPanicButton());

        stopRideButton.setOnClickListener(v ->
            Toast.makeText(getContext(), "Stop Ride - To be implemented", Toast.LENGTH_SHORT).show()
        );

        markCompletedButton.setOnClickListener(v -> {
            if (currentRide != null && currentRide.getRideId() != null) {
                showFinishRideConfirmationDialog();
            } else {
                Toast.makeText(getContext(), "No active ride found", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void triggerPanicButton() {
        rideService.triggerPanicAlert().enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                panicButton.setEnabled(true);
                if(response.isSuccessful()){
                    routeMapView.setPanicMode(true);
                    if(currentRide != null || currentRide.getRoute() != null){
                        routeMapView.displayRoute(currentRide.getRoute().getStartLocation(),
                                currentRide.getRoute().getEndLocation(),
                                currentRide.getRoute().getStopLocations(),
                                currentRide.getRoute().getPolylinePoints());
                    }
                    Toast.makeText(getContext(), "Panic alert is activated, help is on the way", Toast.LENGTH_LONG).show();
                }
                else{
                    String errorMessage = "Error activating panic alert";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            if (errorBody.contains("\"message\"")) {
                                int start = errorBody.indexOf("\"message\":\"") + 11;
                                int end = errorBody.indexOf("\"", start);
                                if (start > 10 && end > start) {
                                    errorMessage = errorBody.substring(start, end);
                                }
                            } else {
                                errorMessage = errorBody;
                                if (errorMessage.startsWith("\"") && errorMessage.endsWith("\"")) {
                                    errorMessage = errorMessage.substring(1, errorMessage.length() - 1);
                                }
                            }
                        }
                    } catch (Exception e) {
                        errorMessage = "Error activating panic alert (code: " + response.code() + ")";
                    }
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                panicButton.setEnabled(true);
                Toast.makeText(getContext(),"Network error: " + t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initServices() {
        rideService = ClientUtils.getClient(RideService.class);
        driverService = ClientUtils.getClient(DriverService.class);
        tokenUtils = new TokenUtils(requireContext());
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        trackingHandler = new Handler(Looper.getMainLooper());
    }

    private void checkUserRole() {
        String role = tokenUtils.getRole();
        isDriver = "DRIVER".equals(role);
    }

    private void setupButtonVisibility() {
        if (isDriver) {
            userButtonsLayout.setVisibility(View.GONE);
            driverButtonsLayout.setVisibility(View.VISIBLE);
        } else {
            userButtonsLayout.setVisibility(View.VISIBLE);
            driverButtonsLayout.setVisibility(View.GONE);
        }
    }

    private void getCurrentRide() {
        Call<CurrentRideResponse> call = rideService.getCurrentRide();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<CurrentRideResponse> call, @NonNull Response<CurrentRideResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentRide = response.body();
                    setupRideInfo();
                    if (isDriver) {
                        startDriverLocationUpdates();
                    } else {
                        startVehicleTracking();
                    }
                } else {
                    Toast.makeText(getContext(), "No current ride found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CurrentRideResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Failed to get current ride: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRideInfo() {
        if (currentRide != null && currentRide.getRoute() != null) {
            // Set addresses
            String startAddress = AddressUtils.formatAddress(currentRide.getRoute().getStartLocation().getAddress());
            String endAddress = AddressUtils.formatAddress(currentRide.getRoute().getEndLocation().getAddress());

            startAddressText.setText(startAddress);
            endAddressText.setText(endAddress);

            // Display route on map
            routeMapView.displayRoute(
                currentRide.getRoute().getStartLocation(),
                currentRide.getRoute().getEndLocation(),
                currentRide.getRoute().getStopLocations(),
                currentRide.getRoute().getPolylinePoints()
            );
        }
    }

    private void startVehicleTracking() {
        if (currentRide == null) return;

        trackingRunnable = new Runnable() {
            @Override
            public void run() {
                trackVehicle();
                trackingHandler.postDelayed(this, TRACKING_INTERVAL);
            }
        };

        // Start tracking immediately
        trackVehicle();
        // Schedule periodic tracking
        trackingHandler.postDelayed(trackingRunnable, TRACKING_INTERVAL);
    }

    private void startDriverLocationUpdates() {
        if (currentRide == null) return;

        trackingRunnable = new Runnable() {
            @Override
            public void run() {
                updateDriverLocation();
                trackingHandler.postDelayed(this, TRACKING_INTERVAL);
            }
        };

        // Start updates immediately
        updateDriverLocation();
        // Schedule periodic updates
        trackingHandler.postDelayed(trackingRunnable, TRACKING_INTERVAL);
    }

    private void trackVehicle() {
        if (currentRide == null || currentRide.getRideId() == null) return;

        Call<TrackVehicleResponseDTO> call = rideService.trackVehicle(currentRide.getRideId().toString());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<TrackVehicleResponseDTO> call, @NonNull Response<TrackVehicleResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TrackVehicleResponseDTO trackData = response.body();
                    updateVehicleLocation(trackData.getLocation());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TrackVehicleResponseDTO> call, @NonNull Throwable t) {
                // Silently fail for tracking errors to avoid spamming user
            }
        });
    }

    private void updateDriverLocation() {
        Log.d(TAG, "updateDriverLocation() called");

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permission not granted");
            return;
        }

        Log.d(TAG, "Getting last known location from LocationManager...");

        // Try GPS provider first, then network provider
        android.location.Location location = null;

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.d(TAG, "GPS location: " + (location != null ? location.getLatitude() + ", " + location.getLongitude() : "null"));
        }

        if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Log.d(TAG, "Network location: " + (location != null ? location.getLatitude() + ", " + location.getLongitude() : "null"));
        }

        if (location != null) {
            Log.d(TAG, "Location received: " + location.getLatitude() + ", " + location.getLongitude());

            // Store final values for callback usage
            final double latitude = location.getLatitude();
            final double longitude = location.getLongitude();

            // Send location to server
            DriverLocationRequestDTO request = new DriverLocationRequestDTO(latitude, longitude);

            Call<DriverLocationResponseDTO> call = driverService.updateDriverLocation(request);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<DriverLocationResponseDTO> call, @NonNull Response<DriverLocationResponseDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d(TAG, "Location updated on server successfully");

                        // Update driver marker on map
                        LocationDTO driverLocation = new LocationDTO();
                        driverLocation.setLatitude(latitude);
                        driverLocation.setLongitude(longitude);
                        driverLocation.setAddress("Current Location");

                        routeMapView.updateDriverMarker(driverLocation);
                        routeMapView.centerOnLocation(driverLocation);
                    } else {
                        Log.w(TAG, "Failed to update location on server: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<DriverLocationResponseDTO> call, @NonNull Throwable t) {
                    Log.e(TAG, "Failed to update location on server", t);
                }
            });
        } else {
            Log.w(TAG, "Location is null from all providers");
            Toast.makeText(getContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateVehicleLocation(LocationDTO vehicleLocation) {
        if (vehicleLocation != null) {
            // Add/update vehicle marker on map
            routeMapView.updateVehicleMarker(vehicleLocation);
            // Center map on vehicle location
            routeMapView.centerOnLocation(vehicleLocation);
        }
    }

    private void showReportInconsistencyDialog() {
        ReportInconsistencyDialog dialog = new ReportInconsistencyDialog(
            getContext(),
            currentRide.getRideId(),
            () -> {
                // Callback when inconsistency is reported successfully
                Toast.makeText(getContext(), "Thank you for your feedback", Toast.LENGTH_SHORT).show();
            }
        );
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (routeMapView != null) {
            routeMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (routeMapView != null) {
            routeMapView.onPause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTracking();
        if (routeMapView != null) {
            routeMapView.onDestroy();
        }
    }

    private void stopTracking() {
        if (trackingHandler != null && trackingRunnable != null) {
            trackingHandler.removeCallbacks(trackingRunnable);
        }
    }

    private void testCurrentLocation() {
        Log.d(TAG, "testCurrentLocation() called - checking emulator location");

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permission not granted for test");
            return;
        }

        // Try GPS provider first, then network provider
        android.location.Location location = null;

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.d(TAG, "TEST GPS location: " + (location != null ? location.getLatitude() + ", " + location.getLongitude() : "null"));
        }

        if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Log.d(TAG, "TEST Network location: " + (location != null ? location.getLatitude() + ", " + location.getLongitude() : "null"));
        }

        if (location != null) {
            Log.d(TAG, "TEST: Current emulator location: " + location.getLatitude() + ", " + location.getLongitude());
            Toast.makeText(getContext(), "Current location: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_LONG).show();
        } else {
            Log.w(TAG, "TEST: Location is null from all providers");
            Toast.makeText(getContext(), "Location is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFinishRideConfirmationDialog() {
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Finish Ride")
                .setMessage("Are you sure you want to finish this ride?")
                .setPositiveButton("Yes", (dialogInterface, which) -> finishRide())
                .setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.show();

        // Set button text colors to black to make them visible
        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.black, null));
        }
        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.black, null));
        }
    }

    private void finishRide() {
        if (currentRide == null || currentRide.getRideId() == null) {
            Toast.makeText(getContext(), "No active ride found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Stop any ongoing tracking
        stopTracking();

        Call<Boolean> call = rideService.finishRide(currentRide.getRideId().toString());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean shouldStayOnCurrentRide = response.body();
                    Log.d(TAG, "Finish ride response: " + shouldStayOnCurrentRide);

                    if (shouldStayOnCurrentRide) {
                        // Stay on current ride page and reload the ride info
                        Toast.makeText(getContext(), "Ride status updated. Reloading...", Toast.LENGTH_SHORT).show();
                        getCurrentRide(); // Reload the current ride data
                    } else {
                        // Navigate to upcoming rides page
                        Toast.makeText(getContext(), "Ride finished successfully!", Toast.LENGTH_SHORT).show();
                        try {
                            Navigation.findNavController(requireView()).navigate(R.id.upcoming_rides);
                        } catch (Exception e) {
                            Log.e(TAG, "Navigation failed", e);
                            Toast.makeText(getContext(), "Ride finished, but navigation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.w(TAG, "Failed to finish ride: " + response.code());
                    Toast.makeText(getContext(), "Failed to finish ride. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                Log.e(TAG, "Error finishing ride", t);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
