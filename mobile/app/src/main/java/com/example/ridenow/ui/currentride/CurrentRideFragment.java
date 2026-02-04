package com.example.ridenow.ui.currentride;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ridenow.R;
import com.example.ridenow.dto.model.Location;
import com.example.ridenow.dto.ride.CurrentRideResponse;
import com.example.ridenow.dto.ride.TrackVehicleResponse;
import com.example.ridenow.service.RideService;
import com.example.ridenow.ui.components.RouteMapView;
import com.example.ridenow.util.AddressUtils;
import com.example.ridenow.util.ClientUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurrentRideFragment extends Fragment {
    private RouteMapView routeMapView;
    private TextView startAddressText;
    private TextView endAddressText;
    private Button reportInconsistencyButton;
    private Button panicButton;

    private RideService rideService;
    private CurrentRideResponse currentRide;
    private Handler trackingHandler;
    private Runnable trackingRunnable;
    private static final long TRACKING_INTERVAL = 10000; // 10 seconds

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_ride, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initServices();
        getCurrentRide();
    }

    private void initViews(View view) {
        routeMapView = view.findViewById(R.id.routeMapView);
        startAddressText = view.findViewById(R.id.startAddressText);
        endAddressText = view.findViewById(R.id.endAddressText);
        reportInconsistencyButton = view.findViewById(R.id.reportInconsistencyButton);
        panicButton = view.findViewById(R.id.panicButton);

        // Set button listeners
        reportInconsistencyButton.setOnClickListener(v -> {
            if (currentRide != null && currentRide.getRideId() != null) {
                showReportInconsistencyDialog();
            } else {
                Toast.makeText(getContext(), "No active ride found", Toast.LENGTH_SHORT).show();
            }
        });

        panicButton.setOnClickListener(v -> {
            // TODO: Implement panic button functionality
            Toast.makeText(getContext(), "Panic Button - To be implemented", Toast.LENGTH_SHORT).show();
        });
    }

    private void initServices() {
        rideService = ClientUtils.getClient(RideService.class);
        trackingHandler = new Handler(Looper.getMainLooper());
    }

    private void getCurrentRide() {
        Call<CurrentRideResponse> call = rideService.getCurrentRide();
        call.enqueue(new Callback<CurrentRideResponse>() {
            @Override
            public void onResponse(@NonNull Call<CurrentRideResponse> call, @NonNull Response<CurrentRideResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentRide = response.body();
                    setupRideInfo();
                    startVehicleTracking();
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

    private void trackVehicle() {
        if (currentRide == null || currentRide.getRideId() == null) return;

        Call<TrackVehicleResponse> call = rideService.trackVehicle(currentRide.getRideId().toString());
        call.enqueue(new Callback<TrackVehicleResponse>() {
            @Override
            public void onResponse(@NonNull Call<TrackVehicleResponse> call, @NonNull Response<TrackVehicleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TrackVehicleResponse trackData = response.body();
                    updateVehicleLocation(trackData.getLocation());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TrackVehicleResponse> call, @NonNull Throwable t) {
                // Silently fail for tracking errors to avoid spamming user
            }
        });
    }

    private void updateVehicleLocation(Location vehicleLocation) {
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
        stopVehicleTracking();
        if (routeMapView != null) {
            routeMapView.onDestroy();
        }
    }

    private void stopVehicleTracking() {
        if (trackingHandler != null && trackingRunnable != null) {
            trackingHandler.removeCallbacks(trackingRunnable);
        }
    }
}
