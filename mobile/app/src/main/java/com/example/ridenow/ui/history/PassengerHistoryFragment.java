package com.example.ridenow.ui.history;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.ridenow.R;
import com.example.ridenow.dto.driver.RideHistoryDTO;
import com.example.ridenow.dto.model.RouteDTO;
import com.example.ridenow.dto.passenger.RideHistoryItemDTO;
import com.example.ridenow.dto.util.PageResponse;
import com.example.ridenow.service.PassengerService;
import com.example.ridenow.util.AddressUtils;
import com.example.ridenow.util.ClientUtils;
import com.example.ridenow.util.DateUtils;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PassengerHistoryFragment extends Fragment implements SensorEventListener {
    private EditText etDateFilter;
    private AutoCompleteTextView spinnerSortBy, spinnerOrder;
    private Button btnApplyFilter, btnClearFilter;
    private LinearLayout cardsContainer;
    private Calendar selectedDate;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 12.0f;
    private static final int SHAKE_TIMEOUT = 1000;
    private long lastShakeTime = 0;
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private String currentSortBy = "date";
    private String currentSortDir = "desc";
    private Long currentDateFilter = null;
    private PassengerService passengerService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passenger_history, container, false);

        try {
            etDateFilter = view.findViewById(R.id.etDateFilter);
            spinnerSortBy = view.findViewById(R.id.spinnerSortBy);
            spinnerOrder = view.findViewById(R.id.spinnerOrder);
            btnApplyFilter = view.findViewById(R.id.btnApplyFilter);
            btnClearFilter = view.findViewById(R.id.btnClearFilter);
            cardsContainer = view.findViewById(R.id.cardsContainer);

            passengerService = ClientUtils.getClient(PassengerService.class);

            spinnerSortBy.setDropDownBackgroundResource(android.R.color.white);
            spinnerOrder.setDropDownBackgroundResource(android.R.color.white);

            setupDropdowns();
            setupDatePicker();
            setupButtons();

            loadPassengerHistory();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error initializing page: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupShakeDetection();
    }

    private void setupDropdowns() {
        String[] sortOptions = {"Route", "Start Time", "End Time", "Date"};
        ArrayAdapter<String> sortAdapter = createCustomAdapter(sortOptions);
        spinnerSortBy.setAdapter(sortAdapter);
        spinnerSortBy.setTextColor(Color.BLACK);
        spinnerSortBy.setBackgroundColor(Color.WHITE);
        spinnerSortBy.setText("Date", false);

        String[] orderOptions = {"Asc", "Desc"};
        ArrayAdapter<String> orderAdapter = createCustomAdapter(orderOptions);
        spinnerOrder.setAdapter(orderAdapter);
        spinnerOrder.setTextColor(Color.BLACK);
        spinnerOrder.setBackgroundColor(Color.WHITE);
        spinnerOrder.setText("Desc", false);

        spinnerSortBy.setOnItemClickListener((parent, v, position, id) ->
                currentSortBy = convertSortByToApi(sortOptions[position])
        );

        spinnerOrder.setOnItemClickListener((parent, v, position, id) ->
                currentSortDir = position == 0 ? "asc" : "desc"
        );
    }

    private ArrayAdapter<String> createCustomAdapter(String[] items) {
        return new ArrayAdapter<String>(requireContext(), R.layout.dropdown_item, items) {
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        results.values = items;
                        results.count = items.length;
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        notifyDataSetChanged();
                    }
                };
            }
        };
    }

    private String convertSortByToApi(String displayName) {
        switch (displayName) {
            case "Route": return "route";
            case "Start Time": return "startTime";
            case "End Time": return "endTime";
            case "Date":
            default: return "date";
        }
    }

    private void setupShakeDetection() {
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double acceleration = Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;

            if (acceleration > SHAKE_THRESHOLD) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastShakeTime > SHAKE_TIMEOUT) {
                    lastShakeTime = currentTime;
                    onShakeDetected();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void onShakeDetected() {
        String previousSortBy = currentSortBy;
        currentSortBy = "date";

        if ("date".equals(previousSortBy)) {
            currentSortDir = currentSortDir.equals("asc") ? "desc" : "asc";
        } else {
            currentSortDir = "desc";
        }

        spinnerSortBy.setText("Date", false);
        spinnerOrder.setText(currentSortDir.equals("asc") ? "Asc" : "Desc", false);

        currentPage = 0;
        loadPassengerHistory();
        Toast.makeText(getContext(), "Sorted by date: " + (currentSortDir.equals("asc") ? "Oldest first" : "Newest first"), Toast.LENGTH_SHORT).show();
    }

    private void setupDatePicker() {
        etDateFilter.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    R.style.CustomDatePickerDialog,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        selectedDate = Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        etDateFilter.setText(dateFormat.format(selectedDate.getTime()));
                    }, year, month, day);

            datePickerDialog.show();
        });
    }

    private void setupButtons() {
        btnApplyFilter.setOnClickListener(v -> {
            if (selectedDate != null) {
                currentDateFilter = selectedDate.getTimeInMillis();
            } else {
                currentDateFilter = null;
            }

            currentPage = 0;
            loadPassengerHistory();
            Toast.makeText(getContext(), "Filters applied", Toast.LENGTH_SHORT).show();
        });

        btnClearFilter.setOnClickListener(v -> clearFilter());
    }

    private void clearFilter() {
        etDateFilter.setText("");
        selectedDate = null;
        currentDateFilter = null;
        currentPage = 0;

        spinnerSortBy.setText("Date", false);
        spinnerOrder.setText("Desc", false);

        currentSortBy = "date";
        currentSortDir = "desc";

        loadPassengerHistory();
        Toast.makeText(getContext(), "All filters cleared", Toast.LENGTH_SHORT).show();
    }

    private void loadPassengerHistory() {
        if (passengerService == null) {
            Toast.makeText(getContext(), "Service not available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isLoading) return;

        isLoading = true;

        Call<PageResponse<RideHistoryItemDTO>> call = passengerService.getPassengerRideHistory(
                currentPage, 10, currentSortBy, currentSortDir, currentDateFilter);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PageResponse<RideHistoryItemDTO>> call,
                                   @NonNull Response<PageResponse<RideHistoryItemDTO>> response) {
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    PageResponse<RideHistoryItemDTO> data = response.body();

                    if (currentPage == 0) {
                        cardsContainer.removeAllViews();
                    } else {
                        removeLoadMoreButton();
                    }

                    hasMoreData = !data.isLast();

                    List<RideHistoryItemDTO> rides = data.getContent();

                    try {
                        for (RideHistoryItemDTO ride : rides) {
                            createRideCard(ride);
                        }
                    } catch (Exception e) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error displaying rides: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    if (hasMoreData) {
                        addLoadMoreButton();
                    }
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load passenger history", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PageResponse<RideHistoryItemDTO>> call, @NonNull Throwable t) {
                isLoading = false;
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createRideCard(RideHistoryItemDTO ride) {
        if (getContext() == null) return;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View cardView = inflater.inflate(R.layout.item_ride_card, cardsContainer, false);

        // Tag the card with its routeId for bulk-update on favorite toggle
        cardView.setTag(ride.getRouteId());

        TextView tvRoute = cardView.findViewById(R.id.tvRoute);
        TextView tvDate = cardView.findViewById(R.id.tvDate);
        TextView tvCost = cardView.findViewById(R.id.tvCost);
        TextView tvPassengers = cardView.findViewById(R.id.tvPassengers);
        TextView tvDuration = cardView.findViewById(R.id.tvDuration);
        TextView tvTimeRange = cardView.findViewById(R.id.tvTimeRange);
        LinearLayout statusContainer = cardView.findViewById(R.id.statusContainer);
        Button btnRating = cardView.findViewById(R.id.btnRating);
        ImageView ivFavorite = cardView.findViewById(R.id.ivFavorite);

        String startAddress = AddressUtils.formatAddress(ride.getRoute().getStartLocation().getAddress());
        String endAddress = AddressUtils.formatAddress(ride.getRoute().getEndLocation().getAddress());
        tvRoute.setText(startAddress + " → " + endAddress);

        tvDate.setText(DateUtils.formatDateFromISO(ride.getStartTime()));

        tvCost.setText(String.format(Locale.getDefault(), "%.0f RSD",
                ride.getPrice() != null ? ride.getPrice() : 0.0));

        tvPassengers.setText(ride.getDriver() != null ? ride.getDriver() : "Driver assigned");

        if (ride.getStartTime() != null && ride.getEndTime() != null) {
            long durationMinutes = DateUtils.calculateDurationMinutes(ride.getStartTime(), ride.getEndTime());
            tvDuration.setText(durationMinutes > 0 ? durationMinutes + " min" : "N/A");
            tvTimeRange.setText(DateUtils.formatTimeRange(ride.getStartTime(), ride.getEndTime()));
        } else {
            tvDuration.setText("N/A");
            tvTimeRange.setText("N/A");
        }

        addStatusIndicators(statusContainer, ride);

        // --- Favorite star ---
        updateStarIcon(ivFavorite, ride.isFavoriteRoute());
        ivFavorite.setOnClickListener(v -> {
            if (ride.isFavoriteRoute()) {
                showRemoveFavoriteDialog(ride, ivFavorite);
            } else {
                showAddFavoriteDialog(ride, ivFavorite);
            }
        });

        cardView.setOnClickListener(v -> openRideDetails(ride));

        if (btnRating != null) {
            btnRating.setVisibility(View.VISIBLE);
            btnRating.setOnClickListener(v -> {
                try {
                    NavController navController = Navigation.findNavController(v);
                    Bundle bundle = new Bundle();
                    bundle.putString("rideId", String.valueOf(ride.getRideId()));
                    navController.navigate(R.id.rating, bundle);
                } catch (Exception e) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Cannot navigate to rating page", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        cardsContainer.addView(cardView);
    }

    // -------------------------------------------------------------------------
    // Favorite dialogs — mirrors Angular add-favorite-modal / remove-favorite-modal
    // -------------------------------------------------------------------------

    private void showAddFavoriteDialog(RideHistoryItemDTO ride, ImageView ivFavorite) {
        if (getContext() == null) return;

        String message = buildRouteInfoMessage(ride);

        new AlertDialog.Builder(requireContext())
                .setTitle("Add to Favorites")
                .setMessage("Do you want to add this route to your favorites?\n\n" + message)
                .setPositiveButton("Add", (dialog, which) -> callAddFavorite(ride, ivFavorite))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRemoveFavoriteDialog(RideHistoryItemDTO ride, ImageView ivFavorite) {
        if (getContext() == null) return;

        String message = buildRouteInfoMessage(ride);

        new AlertDialog.Builder(requireContext())
                .setTitle("Remove from Favorites")
                .setMessage("Remove this route from your favorites?\n\n" + message)
                .setPositiveButton("Remove", (dialog, which) -> callRemoveFavorite(ride, ivFavorite))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String buildRouteInfoMessage(RideHistoryItemDTO ride) {
        StringBuilder sb = new StringBuilder();
        RouteDTO route = ride.getRoute();

        if (route != null) {
            if (route.getStartLocation() != null && route.getStartLocation().getAddress() != null) {
                sb.append("Pickup: ").append(AddressUtils.formatAddress(route.getStartLocation().getAddress())).append("\n");
            }
            if (route.getEndLocation() != null && route.getEndLocation().getAddress() != null) {
                sb.append("Destination: ").append(AddressUtils.formatAddress(route.getEndLocation().getAddress()));
            }
            if (route.getStopLocations() != null && !route.getStopLocations().isEmpty()) {
                sb.append("\nStops:");
                for (var stop : route.getStopLocations()) {
                    if (stop.getAddress() != null) {
                        sb.append("\n  • ").append(AddressUtils.formatAddress(stop.getAddress()));
                    }
                }
            }
        }

        return sb.toString();
    }

    private void callAddFavorite(RideHistoryItemDTO ride, ImageView ivFavorite) {
        if (ride.getRouteId() == null) return;

        passengerService.addFavorite(ride.getRouteId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    // Update the DTO state and all visible cards sharing this routeId
                    ride.setFavoriteRoute(true);
                    updateAllCardsWithRouteId(ride.getRouteId(), true);
                    Toast.makeText(getContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to add favorite", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void callRemoveFavorite(RideHistoryItemDTO ride, ImageView ivFavorite) {
        if (ride.getRouteId() == null) return;

        passengerService.removeFavorite(ride.getRouteId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    ride.setFavoriteRoute(false);
                    updateAllCardsWithRouteId(ride.getRouteId(), false);
                    Toast.makeText(getContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to remove favorite", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * after toggling one ride, all cards that share
     * the same routeId get their star updated (since it's the same route).
     */
    private void updateAllCardsWithRouteId(Long routeId, boolean isFavorite) {
        for (int i = 0; i < cardsContainer.getChildCount(); i++) {
            View child = cardsContainer.getChildAt(i);
            Object tag = child.getTag();
            if (tag instanceof Long && tag.equals(routeId)) {
                ImageView star = child.findViewById(R.id.ivFavorite);
                if (star != null) {
                    updateStarIcon(star, isFavorite);
                }
            }
        }
    }

    private void updateStarIcon(ImageView ivFavorite, boolean isFavorite) {
        ivFavorite.setImageResource(isFavorite
                ? android.R.drawable.btn_star_big_on
                : android.R.drawable.btn_star_big_off);
    }

    // -------------------------------------------------------------------------
    // Status badges
    // -------------------------------------------------------------------------

    private void addStatusIndicators(LinearLayout statusContainer, RideHistoryItemDTO ride) {
        if (statusContainer == null) return;
        statusContainer.removeAllViews();

        if (ride.isCancelled()) {
            statusContainer.addView(createStatusBadge("CANCELLED", Color.parseColor("#F44336")));
        }

        if (Boolean.TRUE.equals(ride.getPanic())) {
            statusContainer.addView(createStatusBadge("PANIC", Color.parseColor("#FF5722")));
        }

        if (!ride.isCancelled() && !Boolean.TRUE.equals(ride.getPanic())) {
            statusContainer.addView(createStatusBadge("COMPLETED", Color.parseColor("#4CAF50")));
        }
    }

    private TextView createStatusBadge(String text, int backgroundColor) {
        TextView badge = new TextView(getContext());
        badge.setText(text);
        badge.setTextSize(10);
        badge.setTextColor(Color.WHITE);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2));

        badge.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.red_rounded_background));
        if (badge.getBackground() != null) {
            badge.getBackground().setTint(backgroundColor);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, dpToPx(4), 0);
        badge.setLayoutParams(params);

        return badge;
    }

    // -------------------------------------------------------------------------
    // Load more
    // -------------------------------------------------------------------------

    private void addLoadMoreButton() {
        Button btn = new Button(getContext());
        btn.setText(R.string.driver_history_load_more);
        btn.setTag("load_more_button");
        btn.setBackgroundColor(Color.BLACK);
        btn.setTextColor(Color.WHITE);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dpToPx(16), 0, dpToPx(16));
        btn.setLayoutParams(p);

        btn.setOnClickListener(v -> {
            removeLoadMoreButton();
            currentPage++;
            loadPassengerHistory();
        });

        cardsContainer.addView(btn);
    }

    private void removeLoadMoreButton() {
        for (int i = cardsContainer.getChildCount() - 1; i >= 0; i--) {
            View child = cardsContainer.getChildAt(i);
            if (child instanceof Button && "load_more_button".equals(child.getTag())) {
                cardsContainer.removeViewAt(i);
                break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Navigation helpers
    // -------------------------------------------------------------------------

    private RideHistoryDTO mapToRideHistoryDTO(RideHistoryItemDTO ride) {
        RideHistoryDTO mapped = new RideHistoryDTO();
        mapped.setRideId(ride.getRideId());
        mapped.setRoute(ride.getRoute());
        mapped.setPassengers(ride.getPassengers());
        mapped.setDate(null);
        mapped.setStartTime(ride.getStartTime());
        mapped.setEndTime(ride.getEndTime());
        mapped.setDurationMinutes(0.0);
        mapped.setCost(ride.getPrice() != null ? ride.getPrice() : 0.0);
        mapped.setCancelled(ride.isCancelled());
        mapped.setCancelledBy(ride.getCancelledBy());
        mapped.setPanic(ride.getPanic());
        mapped.setPanicBy(ride.getPanicBy());
        mapped.setRating(ride.getRating());
        mapped.setInconsistencies(ride.getInconsistencies());
        return mapped;
    }

    private void openRideDetails(RideHistoryItemDTO ride) {
        try {
            RideHistoryDTO mapped = mapToRideHistoryDTO(ride);

            Bundle bundle = new Bundle();
            bundle.putSerializable("ride_history", mapped);

            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_passengerHistory_to_rideDetails, bundle);
        } catch (Exception e) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "Cannot open ride details", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}