package com.example.ridenow.ui.history;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ridenow.R;
import com.example.ridenow.dto.driver.RideHistoryDTO;
import com.example.ridenow.dto.ride.ReorderRideRequestDTO;
import com.example.ridenow.service.RideService;
import com.example.ridenow.ui.components.RouteMapView;
import com.example.ridenow.util.AddressUtils;
import com.example.ridenow.util.ClientUtils;
import com.example.ridenow.util.DateUtils;
import com.example.ridenow.util.TokenUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideDetailsFragment extends Fragment {
    private RideHistoryDTO rideHistory;
    private RouteMapView routeMapView;
    private RideService rideService;

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

        rideService = ClientUtils.getClient(RideService.class);

        if (rideHistory != null) {
            setupMap(view);
            populateRideDetails(view);
            setupReorderButton(view);
        }
    }
    private void setupReorderButton(View view) {
        Button btnReorderRide = view.findViewById(R.id.btnReorderRide);
        if(btnReorderRide == null) { return; }

        TokenUtils tokenUtils = ClientUtils.getTokenUtils();
        String userRole = tokenUtils.getRole();

        boolean isUser = "USER".equals(userRole);
        boolean isAdmin = "ADMIN".equals(userRole);

        if ((isAdmin || isUser) && rideHistory.getRideId() != null) {
            btnReorderRide.setVisibility(View.VISIBLE);
            btnReorderRide.setOnClickListener(v -> showReorderDialog());
        } else {
            btnReorderRide.setVisibility(View.GONE);
        }
    }

    private void showReorderDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_reorder_ride, null);

        LinearLayout llScheduleInputs = dialogView.findViewById(R.id.llScheduleInputs);
        EditText etReorderDate = dialogView.findViewById(R.id.etReorderDate);
        EditText etReorderTime = dialogView.findViewById(R.id.etReorderTime);
        TextView tvReorderError = dialogView.findViewById(R.id.tvReorderError);
        Button btnBookNow = dialogView.findViewById(R.id.btnBookNow);
        Button btnScheduleForLater = dialogView.findViewById(R.id.btnScheduleForLater);
        Button btnConfirmSchedule = dialogView.findViewById(R.id.btnConfirmSchedule);
        Button btnClose = dialogView.findViewById(R.id.btnReorderClose);

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(dialogView).create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.drawable.dialog_holo_light_frame);
        }

        final Calendar[] selectedDate = {null};
        final Calendar[] selectedTime = {null};

        etReorderDate.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            selectedDate[0] = (Calendar) now.clone();
            selectedTime[0] = (Calendar) now.clone();
            SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());

            etReorderDate.setText(dateFmt.format(now.getTime()));
            etReorderTime.setText(timeFmt.format(now.getTime()));
            new DatePickerDialog(requireContext(),
                    R.style.CustomDatePickerDialog,
                    (picker, year, month, day) -> {
                        Calendar c = Calendar.getInstance();
                        c.set(year, month, day);
                        selectedDate[0] = c;
                        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        etReorderDate.setText(fmt.format(c.getTime()));
                    }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
        });

        etReorderTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            selectedDate[0] = (Calendar) now.clone();
            selectedTime[0] = (Calendar) now.clone();
            SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());

            etReorderDate.setText(dateFmt.format(now.getTime()));
            etReorderTime.setText(timeFmt.format(now.getTime()));
            new TimePickerDialog(requireContext(),
                    R.style.CustomDatePickerDialog,
                    (picker, hour, minute) -> {
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.HOUR_OF_DAY, hour);
                        c.set(Calendar.MINUTE, minute);
                        selectedTime[0] = c;
                        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        etReorderTime.setText(fmt.format(c.getTime()));
                    }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
        });

        btnScheduleForLater.setOnClickListener(v -> {
            llScheduleInputs.setVisibility(View.VISIBLE);
            btnScheduleForLater.setVisibility(View.GONE);
            btnBookNow.setVisibility(View.GONE);
            btnConfirmSchedule.setVisibility(View.VISIBLE);

            Calendar now = Calendar.getInstance();
            selectedDate[0] = (Calendar) now.clone();
            selectedTime[0] = (Calendar) now.clone();

            SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
            etReorderDate.setText(dateFmt.format(now.getTime()));
            etReorderTime.setText(timeFmt.format(now.getTime()));
        });

        btnBookNow.setOnClickListener(v -> {
            sendReorderRequest(null, dialog);
        });

        btnConfirmSchedule.setOnClickListener(v -> {
            if (selectedDate[0] == null || selectedTime[0] == null) {
                tvReorderError.setText(getString(R.string.reorder_select_date_time_error));
                tvReorderError.setVisibility(View.VISIBLE);
                return;
            }

            Calendar combined = (Calendar) selectedDate[0].clone();
            combined.set(Calendar.HOUR_OF_DAY, selectedTime[0].get(Calendar.HOUR_OF_DAY));
            combined.set(Calendar.MINUTE, selectedTime[0].get(Calendar.MINUTE));
            combined.set(Calendar.SECOND, 0);

            if (combined.getTimeInMillis() < System.currentTimeMillis()) {
                tvReorderError.setText(getString(R.string.reorder_past_date_error));
                tvReorderError.setVisibility(View.VISIBLE);
                return;
            }

            tvReorderError.setVisibility(View.GONE);

            SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            String scheduledTime = isoFmt.format(combined.getTime());

            sendReorderRequest(scheduledTime, dialog);
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void sendReorderRequest(String scheduledTimeIso, AlertDialog dialog) {
        ReorderRideRequestDTO request = new ReorderRideRequestDTO();
        request.setRideId(rideHistory.getRideId());

        if (scheduledTimeIso != null) {
            request.setScheduledTime(scheduledTimeIso);
        }

        rideService.reorderRide(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), getString(R.string.reorder_success), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    String error = getString(R.string.reorder_failed);
                    try {
                        if (response.errorBody() != null) {
                            String body = response.errorBody().string();
                            if (body != null && !body.isEmpty()) {
                                error = body.replaceAll("^\"|\"$", "");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
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
