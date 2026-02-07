package com.example.ridenow.ui.history;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ridenow.R;
import com.example.ridenow.dto.driver.RideHistoryDTO;
import com.example.ridenow.ui.components.RouteMapView;
import com.example.ridenow.util.AddressUtils;
import com.example.ridenow.util.DateUtils;

import java.util.Locale;

public class RideDetailsFragment extends Fragment {
    private RideHistoryDTO rideHistory;
    private RouteMapView routeMapView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            rideHistory = (RideHistoryDTO) getArguments().getSerializable("ride_history");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ride_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (rideHistory != null) {
            setupMap(view);
            populateRideDetails(view);
        }
    }

    private void populateRideDetails(View view) {
        TextView tvRoute = view.findViewById(R.id.tvDetailRoute);
        TextView tvPassengers = view.findViewById(R.id.tvDetailPassengers);
        TextView tvDate = view.findViewById(R.id.tvDetailDate);
        TextView tvDuration = view.findViewById(R.id.tvDetailDuration);
        TextView tvCancelled = view.findViewById(R.id.tvDetailCancelled);
        TextView tvCost = view.findViewById(R.id.tvDetailCost);
        TextView tvPanicButton = view.findViewById(R.id.tvDetailPanicButton);
        TextView tvInconsistencies = view.findViewById(R.id.tvDetailInconsistencies);

        // Rating components
        LinearLayout llDriverStars = view.findViewById(R.id.llDriverStars);
        TextView tvDriverComment = view.findViewById(R.id.tvDriverComment);
        LinearLayout llVehicleStars = view.findViewById(R.id.llVehicleStars);
        TextView tvVehicleComment = view.findViewById(R.id.tvVehicleComment);

        // Populate basic ride information
        String startAddress = AddressUtils.formatAddress(rideHistory.getRoute().getStartLocation().getAddress());
        String endAddress = AddressUtils.formatAddress(rideHistory.getRoute().getEndLocation().getAddress());
        String routeDisplay = startAddress + " → " + endAddress;
        tvRoute.setText(getString(R.string.ride_details_route, routeDisplay));

        String passengersDisplay = rideHistory.getPassengers() != null && !rideHistory.getPassengers().isEmpty()
                                 ? String.join(", ", rideHistory.getPassengers())
                                 : getString(R.string.ride_details_na);
        tvPassengers.setText(getString(R.string.ride_details_passengers, passengersDisplay));

        // Format date from startTime if available, otherwise use the existing date field
        String dateDisplay;
        if (rideHistory.getStartTime() != null && !rideHistory.getStartTime().trim().isEmpty()) {
            dateDisplay = DateUtils.formatDateFromISO(rideHistory.getStartTime());
        } else {
            dateDisplay = rideHistory.getDate() != null ? rideHistory.getDate() : getString(R.string.ride_details_na);
        }
        tvDate.setText(getString(R.string.ride_details_date, dateDisplay));

        // Calculate duration and format time range
        String durationDisplay;
        if (rideHistory.getStartTime() != null && rideHistory.getEndTime() != null &&
            !rideHistory.getStartTime().trim().isEmpty() && !rideHistory.getEndTime().trim().isEmpty()) {

            long calculatedDuration = DateUtils.calculateDurationMinutes(rideHistory.getStartTime(), rideHistory.getEndTime());
            String timeRange = DateUtils.formatTimeRange(rideHistory.getStartTime(), rideHistory.getEndTime());

            durationDisplay = getString(R.string.ride_details_duration_with_time, calculatedDuration, timeRange);
        } else {
            // Fallback to the existing durationMinutes field
            durationDisplay = String.format(Locale.getDefault(), getString(R.string.ride_details_duration_simple), rideHistory.getDurationMinutes());
        }
        tvDuration.setText(getString(R.string.ride_details_duration, durationDisplay));

        String cancelledDisplay = rideHistory.isCancelled()
                                ? (rideHistory.getCancelledBy() != null ? rideHistory.getCancelledBy() : getString(R.string.ride_details_yes))
                                : getString(R.string.ride_details_no);
        tvCancelled.setText(getString(R.string.ride_details_cancelled, cancelledDisplay));

        String costDisplay = String.format(Locale.getDefault(), getString(R.string.ride_details_cost_format), rideHistory.getCost());
        tvCost.setText(getString(R.string.ride_details_cost, costDisplay));

        String panicDisplay;
        if (rideHistory.getPanic() != null && rideHistory.getPanic()) {
            if (rideHistory.getPanicBy() != null) {
                panicDisplay = getString(R.string.ride_details_panic_with_person, rideHistory.getPanicBy());
            } else {
                panicDisplay = getString(R.string.ride_details_yes);
            }
        } else {
            panicDisplay = getString(R.string.ride_details_no);
        }
        tvPanicButton.setText(getString(R.string.ride_details_panic_button, panicDisplay));

        String inconsistenciesDisplay = rideHistory.getInconsistencies() != null && !rideHistory.getInconsistencies().isEmpty()
                                      ? String.join(", ", rideHistory.getInconsistencies())
                                      : getString(R.string.ride_details_none);
        tvInconsistencies.setText(getString(R.string.ride_details_inconsistencies, inconsistenciesDisplay));

        // Handle ratings
        if (rideHistory.getRating() != null) {
            // Driver rating
            createStarRating(llDriverStars, rideHistory.getRating().getDriverRating());
            String driverComment = rideHistory.getRating().getDriverComment();
            tvDriverComment.setText(driverComment != null && !driverComment.trim().isEmpty()
                ? driverComment : getString(R.string.ride_details_no_comment));

            // Vehicle rating
            createStarRating(llVehicleStars, rideHistory.getRating().getVehicleRating());
            String vehicleComment = rideHistory.getRating().getVehicleComment();
            tvVehicleComment.setText(vehicleComment != null && !vehicleComment.trim().isEmpty()
                ? vehicleComment : getString(R.string.ride_details_no_comment));
        } else {
            // No rating available
            createStarRating(llDriverStars, 0);
            tvDriverComment.setText(getString(R.string.ride_details_no_ratings));

            createStarRating(llVehicleStars, 0);
            tvVehicleComment.setText(getString(R.string.ride_details_no_ratings));
        }
    }

    private void createStarRating(LinearLayout starContainer, int rating) {
        starContainer.removeAllViews();

        for (int i = 1; i <= 5; i++) {
            TextView star = new TextView(getContext());
            star.setText("★");
            star.setTextSize(24);
            star.setPadding(4, 0, 4, 0);

            if (i <= rating) {
                star.setTextColor(Color.parseColor("#FFD700")); // Gold color for filled stars
            } else {
                star.setTextColor(Color.parseColor("#CCCCCC")); // Gray color for empty stars
            }

            starContainer.addView(star);
        }

        // Add rating number next to stars
        TextView ratingText = new TextView(getContext());
        ratingText.setText(getString(R.string.ride_details_star_rating, rating));
        ratingText.setTextSize(14);
        ratingText.setTextColor(Color.parseColor("#666666"));
        ratingText.setPadding(8, 0, 0, 0);
        starContainer.addView(ratingText);
    }

    private void setupMap(View view) {
        routeMapView = view.findViewById(R.id.routeMapView);

        if (rideHistory != null && rideHistory.getRoute() != null) {
            routeMapView.displayRoute(
                rideHistory.getRoute().getStartLocation(),
                rideHistory.getRoute().getEndLocation(),
                rideHistory.getRoute().getStopLocations(),
                rideHistory.getRoute().getPolylinePoints()
            );
        }
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
    public void onDestroy() {
        super.onDestroy();
        if (routeMapView != null) {
            routeMapView.onDestroy();
        }
    }
}
