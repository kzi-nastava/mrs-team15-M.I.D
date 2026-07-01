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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.Locale;
import java.util.List;
import com.example.ridenow.R;
import com.example.ridenow.dto.driver.DriverHistoryResponseDTO;
import com.example.ridenow.dto.driver.RideHistoryDTO;
import com.example.ridenow.service.DriverService;
import com.example.ridenow.util.AddressUtils;
import com.example.ridenow.util.ClientUtils;
import com.example.ridenow.util.DateUtils;

import retrofit2.Call;

public class DriverHistoryFragment extends Fragment implements SensorEventListener {
    // Filter UI components
    private EditText etDateFilter;
    private AutoCompleteTextView spinnerSortBy, spinnerOrder;
    private Button btnApplyFilter, btnClearFilter;

    // Content container
    private LinearLayout ridesContainer;

    // Date picker
    private Calendar selectedDate;

    // Shake detection
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 12.0f;
    private static final int SHAKE_TIMEOUT = 1000;
    private long lastShakeTime = 0;

    // Pagination and data management
    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private String currentSortBy = "date";
    private String currentSortDir = "desc";
    private String currentDateFilter = null;

    // Service
    private DriverService driverService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_history, container, false);

        // Initialize UI components
        etDateFilter = view.findViewById(R.id.etDateFilter);
        spinnerSortBy = view.findViewById(R.id.spinnerSortBy);
        spinnerOrder = view.findViewById(R.id.spinnerOrder);
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter);
        btnClearFilter = view.findViewById(R.id.btnClearFilter);
        ridesContainer = view.findViewById(R.id.ridesContainer);

        // Initialize service
        driverService = ClientUtils.getClient(DriverService.class);

        // Setup UI
        setupDropdowns();
        setupDatePicker();
        setupButtons();

        // Load initial data
        loadDriverHistory();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupShakeDetection();
    }

    private void setupDropdowns() {
        // Sort By dropdown - matching original table columns
        String[] sortOptions = {"Route", "Passengers", "Date", "Duration", "Cancelled", "Cost", "Panic Button"};
        ArrayAdapter<String> sortAdapter = createCustomAdapter(sortOptions);
        spinnerSortBy.setAdapter(sortAdapter);
        spinnerSortBy.setText(getString(R.string.driver_history_date), false);

        // Order dropdown
        String[] orderOptions = {getString(R.string.driver_history_ascending), getString(R.string.driver_history_descending)};
        ArrayAdapter<String> orderAdapter = createCustomAdapter(orderOptions);
        spinnerOrder.setAdapter(orderAdapter);
        spinnerOrder.setText(getString(R.string.driver_history_descending), false);

        // Set listeners
        spinnerSortBy.setOnItemClickListener((parent, view, position, id) ->
            currentSortBy = convertSortByToApi(sortOptions[position])
        );

        spinnerOrder.setOnItemClickListener((parent, view, position, id) ->
            currentSortDir = position == 0 ? "asc" : "desc"
        );
    }

    private ArrayAdapter<String> createCustomAdapter(String[] items) {
        return new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, items) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(Color.BLACK);
                textView.setBackgroundColor(Color.WHITE);
                textView.setPadding(16, 16, 16, 16);
                return view;
            }

            @NonNull
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(Color.BLACK);
                textView.setBackgroundColor(Color.WHITE);
                textView.setPadding(16, 16, 16, 16);
                return view;
            }
        };
    }

    private String convertSortByToApi(String displayName) {
        switch (displayName) {
            case "Route": return "route";
            case "Passengers": return "passengers";
            case "Date": return "date";
            case "Duration": return "duration";
            case "Cancelled": return "cancelled";
            case "Cost": return "cost";
            case "Panic Button": return "panic";
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

    // Shake detection logic
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
        // No need for this
    }

    private void onShakeDetected() {
        // Store current sort by to check if we're already sorting by date
        String previousSortBy = currentSortBy;

        // Set to date sorting
        currentSortBy = "date";

        // If we're already sorting by date, toggle direction
        if ("date".equals(previousSortBy)) {
            currentSortDir = currentSortDir.equals("asc") ? "desc" : "asc";
        } else {
            // If switching from another sort field, default to descending (newest first)
            currentSortDir = "desc";
        }

        // Update UI to reflect the change
        spinnerSortBy.setText(getString(R.string.driver_history_date), false);
        spinnerOrder.setText(currentSortDir.equals("asc") ? getString(R.string.driver_history_ascending) : getString(R.string.driver_history_descending), false);

        currentPage = 0;
        loadDriverHistory();
        Toast.makeText(getContext(), "Sorted by date: " + (currentSortDir.equals("asc") ? "Oldest first" : "Newest first"), Toast.LENGTH_SHORT).show();
    }

    private void createRideCard(RideHistoryDTO rideHistory) {
        // Inflate the card layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View cardView = inflater.inflate(R.layout.item_ride_card, ridesContainer, false);

        // Find views
        TextView tvRoute = cardView.findViewById(R.id.tvRoute);
        TextView tvDate = cardView.findViewById(R.id.tvDate);
        TextView tvCost = cardView.findViewById(R.id.tvCost);
        TextView tvPassengers = cardView.findViewById(R.id.tvPassengers);
        TextView tvDuration = cardView.findViewById(R.id.tvDuration);
        TextView tvTimeRange = cardView.findViewById(R.id.tvTimeRange);
        LinearLayout statusContainer = cardView.findViewById(R.id.statusContainer);
        Button btnRating = cardView.findViewById(R.id.btnRating);

        // Hide rating button for driver history (only show for passenger history)
        if (btnRating != null) {
            btnRating.setVisibility(View.GONE);
        }

        // Populate route
        String startAddress = AddressUtils.formatAddress(rideHistory.getRoute().getStartLocation().getAddress());
        String endAddress = AddressUtils.formatAddress(rideHistory.getRoute().getEndLocation().getAddress());
        String routeDisplay = startAddress + " → " + endAddress;
        tvRoute.setText(routeDisplay);

        // Populate date
        String dateDisplay;
        if (rideHistory.getStartTime() != null && !rideHistory.getStartTime().trim().isEmpty()) {
            dateDisplay = DateUtils.formatDateFromISO(rideHistory.getStartTime());
        } else {
            dateDisplay = rideHistory.getDate() != null ? rideHistory.getDate() : "N/A";
        }
        tvDate.setText(dateDisplay);

        // Populate cost
        String costDisplay = String.format(Locale.getDefault(), "%.0f RSD", rideHistory.getCost());
        tvCost.setText(costDisplay);

        // Populate passengers
        String passengersDisplay = rideHistory.getPassengers() != null && !rideHistory.getPassengers().isEmpty()
                                 ? String.join(", ", rideHistory.getPassengers())
                                 : "No passengers";
        tvPassengers.setText(passengersDisplay);

        // Populate duration and time range
        String durationDisplay;
        String timeRangeDisplay;

        if (rideHistory.getStartTime() != null && rideHistory.getEndTime() != null &&
            !rideHistory.getStartTime().trim().isEmpty() && !rideHistory.getEndTime().trim().isEmpty()) {

            long calculatedDuration = DateUtils.calculateDurationMinutes(rideHistory.getStartTime(), rideHistory.getEndTime());
            durationDisplay = calculatedDuration + " min";
            timeRangeDisplay = DateUtils.formatTimeRange(rideHistory.getStartTime(), rideHistory.getEndTime());
        } else {
            // Handle estimated duration from API
            double duration = rideHistory.getDurationMinutes();
            if (duration > 0) {
                durationDisplay = String.format(Locale.getDefault(), "%.0f min", duration);
            } else {
                durationDisplay = "N/A";
            }
            timeRangeDisplay = "Estimated duration";
        }

        tvDuration.setText(durationDisplay);
        tvTimeRange.setText(timeRangeDisplay);

        // Add status indicators
        addStatusIndicators(statusContainer, rideHistory);

        // Set click listener to open ride details
        cardView.setOnClickListener(v -> openRideDetailsFromAPI(rideHistory));

        // Add to container
        ridesContainer.addView(cardView);
    }

    private void addStatusIndicators(LinearLayout statusContainer, RideHistoryDTO rideHistory) {
        statusContainer.removeAllViews();

        // Check for cancelled status
        if (rideHistory.isCancelled()) {
            TextView cancelledBadge = createStatusBadge("CANCELLED", Color.parseColor("#F44336"));
            statusContainer.addView(cancelledBadge);
        }

        // Check for panic button
        if (rideHistory.getPanic() != null && rideHistory.getPanic()) {
            TextView panicBadge = createStatusBadge("PANIC", Color.parseColor("#FF5722"));
            statusContainer.addView(panicBadge);
        }

        // If neither cancelled nor panic, show completed
        if (!rideHistory.isCancelled() && (rideHistory.getPanic() == null || !rideHistory.getPanic())) {
            TextView completedBadge = createStatusBadge("COMPLETED", Color.parseColor("#4CAF50"));
            statusContainer.addView(completedBadge);
        }
    }

    private TextView createStatusBadge(String text, int backgroundColor) {
        TextView badge = new TextView(getContext());
        badge.setText(text);
        badge.setTextSize(10);
        badge.setTextColor(Color.WHITE);
        badge.setBackgroundColor(backgroundColor);
        badge.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2));
        badge.setGravity(Gravity.CENTER);

        // Add corner radius
        badge.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.red_rounded_background));
        badge.getBackground().setTint(backgroundColor);

        // Add margin between badges
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, dpToPx(4), 0);
        badge.setLayoutParams(params);

        return badge;
    }

    private void addLoadMoreButtonIfNeeded() {
        if (!hasMoreData) return;

        // Remove existing load more button if any
        removeLoadMoreButton();

        Button loadMoreButton = new Button(getContext());
        loadMoreButton.setText(R.string.driver_history_load_more);
        loadMoreButton.setTag("load_more_button");

        // Style the button
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dpToPx(16), 0, dpToPx(16));
        loadMoreButton.setLayoutParams(params);

        loadMoreButton.setOnClickListener(v -> {
            // Remove the button before loading more data
            removeLoadMoreButton();
            currentPage++;
            loadDriverHistory();
        });

        ridesContainer.addView(loadMoreButton);
    }

    private void removeLoadMoreButton() {
        for (int i = ridesContainer.getChildCount() - 1; i >= 0; i--) {
            View child = ridesContainer.getChildAt(i);
            if (child instanceof Button && "load_more_button".equals(child.getTag())) {
                ridesContainer.removeViewAt(i);
                break;
            }
        }
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
            // Update date filter if a date is selected
            if (selectedDate != null) {
                currentDateFilter = String.valueOf(selectedDate.getTimeInMillis());
            } else {
                currentDateFilter = null; // Clear date filter if no date selected
            }

            currentPage = 0; // Reset to first page
            loadDriverHistory();
            Toast.makeText(getContext(), "Filters applied", Toast.LENGTH_SHORT).show();
        });

        btnClearFilter.setOnClickListener(v -> clearFilter());
    }


    private void clearFilter() {
        etDateFilter.setText("");
        selectedDate = null;
        currentDateFilter = null;
        currentPage = 0; // Reset to first page

        // Reset dropdowns to default values
        spinnerSortBy.setText(getString(R.string.driver_history_date), false);
        spinnerOrder.setText(getString(R.string.driver_history_descending), false);

        // Reset API parameters
        currentSortBy = "date";
        currentSortDir = "desc";

        loadDriverHistory();
        Toast.makeText(getContext(), "All filters cleared", Toast.LENGTH_SHORT).show();
    }

    private void loadDriverHistory() {
        if (isLoading) return;

        isLoading = true;

        Call<DriverHistoryResponseDTO> call = driverService.getDriverRideHistory(
            currentPage,
            10, // page size
            currentSortBy,
            currentSortDir,
            currentDateFilter
        );

        call.enqueue(new retrofit2.Callback<>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<DriverHistoryResponseDTO> call, @NonNull retrofit2.Response<DriverHistoryResponseDTO> response) {
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    DriverHistoryResponseDTO historyResponse = response.body();

                    if (currentPage == 0) {
                        // First page, clear existing data
                        ridesContainer.removeAllViews();
                    } else {
                        // Remove existing load more button before adding new cards
                        removeLoadMoreButton();
                    }

                    List<RideHistoryDTO> newRides = historyResponse.getContent();
                    hasMoreData = !historyResponse.isLast();

                    // Add cards for new rides
                    for (RideHistoryDTO ride : newRides) {
                        createRideCard(ride);
                    }

                    // Add "Load More" button if there's more data
                    addLoadMoreButtonIfNeeded();
                } else {
                    Toast.makeText(getContext(), "Failed to load driver history", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<DriverHistoryResponseDTO> call, @NonNull Throwable t) {
                isLoading = false;
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void openRideDetailsFromAPI(RideHistoryDTO rideHistory) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("ride_history", rideHistory);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_driverHistory_to_rideDetails, bundle);
    }



    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
