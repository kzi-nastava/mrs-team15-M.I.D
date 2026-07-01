package com.example.ridenow.ui.ride;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.ridenow.R;
import com.example.ridenow.dto.driver.DriverCanStartRideResponseDTO;
import com.example.ridenow.dto.model.LocationDTO;
import com.example.ridenow.dto.model.PolylinePointDTO;
import com.example.ridenow.dto.ride.StartRideResponseDTO;
import com.example.ridenow.service.DriverService;
import com.example.ridenow.service.RideService;
import com.example.ridenow.ui.components.RouteMapView;
import com.example.ridenow.util.ClientUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StartRideFragement extends Fragment {

    private RouteMapView routeMapView;
    private TextView tvPickupLocation;
    private TextView tvDestination;
    private TextView tvRideInfo;
    private TextView tvPassengerCount;
    private TextView tvWarning;
    private TextView tvDriverStatus;
    private ProgressBar progressBar;
    private LinearLayout passengersContainer;
    private Button btnStartRide;

    private DriverService driverService;
    private RideService rideService;
    private long rideId = -1L;
    private String route = "";
    private String startTime = "";
    private final List<PassengerItem> passengers = new ArrayList<>();
    private boolean driverCanStart = false;
    private StartRideResponseDTO rideDetails;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start_ride, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        driverService = ClientUtils.getClient(DriverService.class);
        rideService = ClientUtils.getClient(RideService.class);
        bindViews(view);
        readArguments();
        renderRideDetails();
        renderPassengers();
        loadDriverEligibility();
        loadRideDetails();

        btnStartRide.setOnClickListener(v -> handleStartRide());
    }

    private void bindViews(@NonNull View view) {
        routeMapView = view.findViewById(R.id.routeMapView);
        tvPickupLocation = view.findViewById(R.id.tvPickupLocation);
        tvDestination = view.findViewById(R.id.tvDestination);
        tvRideInfo = view.findViewById(R.id.tvRideInfo);
        tvPassengerCount = view.findViewById(R.id.tvPassengerCount);
        tvWarning = view.findViewById(R.id.tvWarning);
        tvDriverStatus = view.findViewById(R.id.tvDriverStatus);
        progressBar = view.findViewById(R.id.progressBar);
        passengersContainer = view.findViewById(R.id.passengersContainer);
        btnStartRide = view.findViewById(R.id.btnStartRide);
    }

    private void readArguments() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        rideId = args.getLong("rideId", -1L);
        route = args.getString("route", "");
        startTime = args.getString("startTime", "");

        String passengersValue = args.getString("passengers", "");
        if (!TextUtils.isEmpty(passengersValue)) {
            for (String rawPassenger : passengersValue.split(",")) {
                String passengerName = rawPassenger.trim();
                if (!passengerName.isEmpty()) {
                    passengers.add(new PassengerItem(passengerName));
                }
            }
        }

        if (passengers.isEmpty()) {
            passengers.add(new PassengerItem("No passengers assigned"));
        }
    }

    private void renderRideDetails() {
        String[] endpoints = splitRoute(route);
        tvPickupLocation.setText(endpoints[0]);
        tvDestination.setText(endpoints[1]);

        if (rideId > 0) {
            tvRideInfo.setText("Ride #" + rideId + (TextUtils.isEmpty(startTime) ? "" : " • " + startTime));
        } else {
            tvRideInfo.setText(TextUtils.isEmpty(startTime) ? "Ride details" : startTime);
        }

        updateStartStateMessage();
    }

    private void loadRideDetails() {
        if (rideId <= 0) {
            return;
        }

        rideService.passengerPickup(rideId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<StartRideResponseDTO> call, @NonNull Response<StartRideResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    rideDetails = response.body();
                    applyRideDetails(rideDetails);
                }
            }

            @Override
            public void onFailure(@NonNull Call<StartRideResponseDTO> call, @NonNull Throwable t) {
                tvDriverStatus.setText("Network error while loading ride details.");
            }
        });
    }

    private void applyRideDetails(@NonNull StartRideResponseDTO details) {
        if (details.getStartAddress() != null && !details.getStartAddress().isEmpty()) {
            tvPickupLocation.setText(details.getStartAddress());
        }
        if (details.getEndAddress() != null && !details.getEndAddress().isEmpty()) {
            tvDestination.setText(details.getEndAddress());
        }
        if (details.getPassengers() != null && !details.getPassengers().isEmpty()) {
            passengers.clear();
            for (String passenger : details.getPassengers()) {
                passengers.add(new PassengerItem(passenger));
            }
            renderPassengers();
        }

        renderRideOnMap(details);
    }

    private void renderRideOnMap(@NonNull StartRideResponseDTO details) {
        if (routeMapView == null || details.getRoute() == null || details.getRoute().isEmpty()) {
            return;
        }

        List<PolylinePointDTO> polylinePoints = new ArrayList<>();
        for (com.example.ridenow.dto.ride.RoutePointDTO point : details.getRoute()) {
            polylinePoints.add(new PolylinePointDTO(point.getLat(), point.getLng()));
        }

        com.example.ridenow.dto.ride.RoutePointDTO firstPoint = details.getRoute().get(0);
        com.example.ridenow.dto.ride.RoutePointDTO lastPoint = details.getRoute().get(details.getRoute().size() - 1);

        LocationDTO startLocation = new LocationDTO(firstPoint.getLat(), firstPoint.getLng(), details.getStartAddress());
        LocationDTO endLocation = new LocationDTO(lastPoint.getLat(), lastPoint.getLng(), details.getEndAddress());

        routeMapView.setShowMarkers(true);
        routeMapView.displayRoute(startLocation, endLocation, null, polylinePoints);
    }

    private void renderPassengers() {
        passengersContainer.removeAllViews();

        for (PassengerItem passenger : passengers) {
            MaterialCardView cardView = new MaterialCardView(requireContext());
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cardParams.bottomMargin = dpToPx(10);
            cardView.setLayoutParams(cardParams);
            cardView.setRadius(dpToPx(14));
            cardView.setCardElevation(dpToPx(1));
            cardView.setUseCompatPadding(true);
            cardView.setStrokeWidth(dpToPx(1));

            LinearLayout content = new LinearLayout(requireContext());
            content.setOrientation(LinearLayout.HORIZONTAL);
            content.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));
            content.setGravity(Gravity.CENTER_VERTICAL);

            TextView badge = new TextView(requireContext());
            LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40));
            badgeParams.rightMargin = dpToPx(12);
            badge.setLayoutParams(badgeParams);
            badge.setGravity(Gravity.CENTER);
            badge.setText(getInitials(passenger.name));
            badge.setTextColor(getResources().getColor(R.color.black, requireContext().getTheme()));
            badge.setBackgroundResource(R.drawable.rounded_background_light);

            LinearLayout textColumn = new LinearLayout(requireContext());
            textColumn.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            textColumn.setLayoutParams(textParams);

            TextView nameText = new TextView(requireContext());
            nameText.setText(passenger.name);
            nameText.setTextSize(16f);
            nameText.setTextColor(getResources().getColor(R.color.black, requireContext().getTheme()));
            nameText.setSingleLine(true);
            nameText.setEllipsize(TextUtils.TruncateAt.END);

            TextView statusText = new TextView(requireContext());
            statusText.setText(passenger.present ? "Present" : "Tap to mark present");
            statusText.setTextSize(12f);
            statusText.setTextColor(getResources().getColor(R.color.black, requireContext().getTheme()));

            textColumn.addView(nameText);
            textColumn.addView(statusText);

            cardView.addView(content);
            content.addView(badge);
            content.addView(textColumn);

            updatePassengerCardAppearance(cardView, passenger, statusText);
            cardView.setOnClickListener(v -> {
                passenger.present = !passenger.present;
                updatePassengerCardAppearance(cardView, passenger, statusText);
                updateStartStateMessage();
            });

            passengersContainer.addView(cardView);
        }

        updatePassengerCount();
    }

    private void updatePassengerCardAppearance(@NonNull MaterialCardView cardView, @NonNull PassengerItem passenger, @NonNull TextView statusText) {
        int backgroundColor = passenger.present
                ? getResources().getColor(R.color.success_background, requireContext().getTheme())
                : getResources().getColor(R.color.light_primary_container, requireContext().getTheme());
        int strokeColor = passenger.present
                ? getResources().getColor(R.color.success, requireContext().getTheme())
                : getResources().getColor(R.color.divider_color, requireContext().getTheme());
        int textColor = passenger.present
                ? getResources().getColor(R.color.black, requireContext().getTheme())
                : getResources().getColor(R.color.black, requireContext().getTheme());

        cardView.setCardBackgroundColor(backgroundColor);
        cardView.setStrokeColor(strokeColor);
        statusText.setText(passenger.present ? "Present" : "Tap to mark present");
        statusText.setTextColor(textColor);
    }

    private void updatePassengerCount() {
        int presentCount = 0;
        for (PassengerItem passenger : passengers) {
            if (passenger.present) {
                presentCount++;
            }
        }

        tvPassengerCount.setText(presentCount + " / " + passengers.size() + " passengers present");
    }

    private void updateStartStateMessage() {
        int presentCount = 0;
        for (PassengerItem passenger : passengers) {
            if (passenger.present) {
                presentCount++;
            }
        }

        updatePassengerCount();

        if (presentCount == 0) {
            tvWarning.setText("No passengers are marked present yet.");
            tvWarning.setVisibility(View.VISIBLE);
        } else if (presentCount < passengers.size()) {
            tvWarning.setText("Start ride when all passengers are aboard.");
            tvWarning.setVisibility(View.VISIBLE);
        } else {
            tvWarning.setVisibility(View.GONE);
        }
    }

    private void loadDriverEligibility() {
        showLoading(true);
        driverService.canStartRide().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<DriverCanStartRideResponseDTO> call, @NonNull Response<DriverCanStartRideResponseDTO> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    driverCanStart = response.body().isCanStart();
                    tvDriverStatus.setText(driverCanStart ? "Driver status is ready to start a ride." : "Driver is not currently allowed to start a ride.");
                    btnStartRide.setEnabled(driverCanStart);
                } else {
                    tvDriverStatus.setText("Unable to load driver status.");
                    btnStartRide.setEnabled(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<DriverCanStartRideResponseDTO> call, @NonNull Throwable t) {
                showLoading(false);
                tvDriverStatus.setText("Network error while checking driver status.");
                btnStartRide.setEnabled(false);
            }
        });
    }

    private void handleStartRide() {
        if (!driverCanStart) {
            Toast.makeText(requireContext(), "You cannot start a ride right now.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (rideId <= 0) {
            Toast.makeText(requireContext(), "Missing ride id.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> missingPassengers = new ArrayList<>();
        for (PassengerItem passenger : passengers) {
            if (!passenger.present) {
                missingPassengers.add(passenger.name);
            }
        }

        if (passengers.size() == 1 && "No passengers assigned".equals(passengers.get(0).name)) {
            startRideRequest();
            return;
        }

        if (!missingPassengers.isEmpty()) {
            showMissingPassengersDialog(missingPassengers);
            return;
        }

        startRideRequest();
    }

    private void showMissingPassengersDialog(@NonNull List<String> missingPassengers) {
        StringBuilder message = new StringBuilder("The following passengers are not marked present:\n\n");
        for (String passenger : missingPassengers) {
            message.append("• ").append(passenger).append('\n');
        }
        message.append("\nStart the ride anyway?");

        new AlertDialog.Builder(requireContext())
                .setTitle("Start Ride")
                .setMessage(message.toString())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Start", (dialog, which) -> startRideRequest())
                .show();
    }

    private void startRideRequest() {
        showLoading(true);
        driverService.startRide(rideId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Navigation.findNavController(requireView()).navigate(R.id.current_ride);
                } else {
                    Toast.makeText(requireContext(), "Failed to start ride.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(requireContext(), "Network error while starting ride.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnStartRide.setEnabled(!loading && driverCanStart);
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
        if (routeMapView != null) {
            routeMapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (routeMapView != null) {
            routeMapView.onDestroy();
        }
        super.onDestroyView();
    }

    private String[] splitRoute(@Nullable String rawRoute) {
        String defaultPickup = "Pickup location unavailable";
        String defaultDestination = "Destination unavailable";

        if (TextUtils.isEmpty(rawRoute)) {
            return new String[]{defaultPickup, defaultDestination};
        }

        String normalizedRoute = rawRoute.replace("->", "→");
        if (normalizedRoute.contains("→")) {
            String[] parts = normalizedRoute.split("→", 2);
            return new String[]{parts[0].trim(), parts[1].trim()};
        }

        return new String[]{rawRoute.trim(), defaultDestination};
    }

    private String getInitials(@NonNull String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 0) {
            return "?";
        }

        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }

        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private static class PassengerItem {
        private final String name;
        private boolean present;

        private PassengerItem(String name) {
            this.name = name;
        }
    }
}