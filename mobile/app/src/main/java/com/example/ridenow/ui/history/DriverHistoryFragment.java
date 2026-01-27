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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import com.example.ridenow.R;
import com.example.ridenow.dto.driver.DriverHistoryResponse;
import com.example.ridenow.dto.driver.RideHistory;
import com.example.ridenow.service.DriverService;
import com.example.ridenow.util.ClientUtils;

import retrofit2.Call;

public class DriverHistoryFragment extends Fragment implements SensorEventListener {
    private EditText etDateFilter;
    private Button btnApplyFilter, btnClearFilter;
    private TableLayout tableDriverHistory;
    private Calendar selectedDate;
    private int currentSortColumn = 2;
    private boolean isAscending = true;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 12.0f;
    private static final int SHAKE_TIMEOUT = 1000;
    private long lastShakeTime = 0;

    // Pagination variables
    private int currentPage = 0;
    private int pageSize = 8;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private String currentSortBy = "date";
    private String currentSortDir = "desc";
    private String currentDateFilter = null;

    private DriverService driverService;
    private List<RideHistory> allRideData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_history, container, false);

        // Initialize views
        etDateFilter = view.findViewById(R.id.etDateFilter);
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter);
        btnClearFilter = view.findViewById(R.id.btnClearFilter);
        tableDriverHistory = view.findViewById(R.id.tableDriverHistory);

        // Initialize service and data
        driverService = ClientUtils.getClient(DriverService.class);
        allRideData = new ArrayList<>();

        setupDatePicker();
        setupButtons();
        loadDriverHistory();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupHeaderClickListeners();
        setupShakeDetection();
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
        // No need for this
    }

    private void onShakeDetected() {
        // Trigger the same action as clicking the date header
        if (currentSortColumn == 2 && currentSortBy.equals("date")) {
            // Already sorted by date, toggle direction
            isAscending = !isAscending;
        } else {
            // Set to date column
            currentSortColumn = 2;
            isAscending = true;
        }
        sortTable(2);
    }

    private void setupHeaderClickListeners() {
        View rootView = getView();
        if (rootView == null) return;

        rootView.findViewById(R.id.headerRoute).setOnClickListener(v -> sortTable(0));
        rootView.findViewById(R.id.headerPassengers).setOnClickListener(v -> sortTable(1));
        rootView.findViewById(R.id.headerDate).setOnClickListener(v -> sortTable(2));
        rootView.findViewById(R.id.headerDuration).setOnClickListener(v -> sortTable(3));
        rootView.findViewById(R.id.headerCancelled).setOnClickListener(v -> sortTable(5));
        rootView.findViewById(R.id.headerCost).setOnClickListener(v -> sortTable(7));
        rootView.findViewById(R.id.headerPanicButton).setOnClickListener(v -> sortTable(8));
    }

    private void updateSortIndicators() {
        View rootView = getView();
        if (rootView == null) return;

        // Reset all headers to remove existing arrows
        resetHeaderText(rootView.findViewById(R.id.headerRoute), "Route");
        resetHeaderText(rootView.findViewById(R.id.headerPassengers), "Passengers");
        resetHeaderText(rootView.findViewById(R.id.headerDate), "Date");
        resetHeaderText(rootView.findViewById(R.id.headerDuration), "Duration");
        resetHeaderText(rootView.findViewById(R.id.headerCancelled), "Cancelled");
        resetHeaderText(rootView.findViewById(R.id.headerCost), "Cost");
        resetHeaderText(rootView.findViewById(R.id.headerPanicButton), "Panic Button");

        // Add arrow to current sorted column
        String arrow = isAscending ? " ▲" : " ▼";
        TextView currentHeader = null;

        switch (currentSortColumn) {
            case 0:
                currentHeader = rootView.findViewById(R.id.headerRoute);
                break;
            case 1:
                currentHeader = rootView.findViewById(R.id.headerPassengers);
                break;
            case 2:
                currentHeader = rootView.findViewById(R.id.headerDate);
                break;
            case 3:
                currentHeader = rootView.findViewById(R.id.headerDuration);
                break;
            case 5:
                currentHeader = rootView.findViewById(R.id.headerCancelled);
                break;
            case 7:
                currentHeader = rootView.findViewById(R.id.headerCost);
                break;
            case 8:
                currentHeader = rootView.findViewById(R.id.headerPanicButton);
                break;
        }

        if (currentHeader != null) {
            String currentText = currentHeader.getText().toString();
            currentHeader.setText(currentText + arrow);
        }
    }

    private void resetHeaderText(TextView header, String originalText) {
        if (header != null) {
            header.setText(originalText);
        }
    }

    private void sortTable(int columnIndex) {
        if (currentSortColumn == columnIndex) {
            isAscending = !isAscending;
        } else {
            currentSortColumn = columnIndex;
            isAscending = true;
        }

        // Map column index to API sort fields
        switch (columnIndex) {
            case 0:
                currentSortBy = "route";
                break;
            case 1:
                currentSortBy = "passengers";
                break;
            case 2:
                currentSortBy = "date";
                break;
            case 3:
                currentSortBy = "durationMinutes";
                break;
            case 5:
                currentSortBy = "cancelled";
                break;
            case 7:
                currentSortBy = "cost";
                break;
            case 8:
                currentSortBy = "panic";
                break;
            default:
                currentSortBy = "date";
                break;
        }

        currentSortDir = isAscending ? "asc" : "desc";
        currentPage = 0; // Reset to first page when sorting changes

        // Reload data with new sorting
        loadDriverHistory();
        updateSortIndicators();
    }

    private void setupDatePicker() {
        etDateFilter.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
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
                applyDateFilter();
            } else {
                Toast.makeText(getContext(), "Please select a date first", Toast.LENGTH_SHORT).show();
            }
        });

        btnClearFilter.setOnClickListener(v -> {
            clearFilter();
        });
    }

    private void applyDateFilter() {
        if (selectedDate != null) {
            // Convert date to Long timestamp (milliseconds since epoch)
            currentDateFilter = String.valueOf(selectedDate.getTimeInMillis());
            currentPage = 0; // Reset to first page
            loadDriverHistory();
            Toast.makeText(getContext(), "Filter applied", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Please select a date first", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFilter() {
        etDateFilter.setText("");
        selectedDate = null;
        currentDateFilter = null;
        currentPage = 0; // Reset to first page
        loadDriverHistory();
        Toast.makeText(getContext(), "Filter cleared", Toast.LENGTH_SHORT).show();
    }

    private void loadDriverHistory() {
        if (isLoading) return;

        isLoading = true;

        Call<DriverHistoryResponse> call = driverService.getDriverRideHistory(
            currentPage,
            pageSize,
            currentSortBy,
            currentSortDir,
            currentDateFilter
        );

        call.enqueue(new retrofit2.Callback<DriverHistoryResponse>() {
            @Override
            public void onResponse(retrofit2.Call<DriverHistoryResponse> call, retrofit2.Response<DriverHistoryResponse> response) {
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    DriverHistoryResponse historyResponse = response.body();

                    if (currentPage == 0) {
                        // First page, clear existing data
                        allRideData.clear();
                    }

                    allRideData.addAll(historyResponse.getContent());
                    hasMoreData = !historyResponse.isLast();

                    if (currentPage == 0) {
                        // First load or refresh, clear table and populate
                        clearTableRows();
                        populateDriverHistoryTableFromAPI(allRideData);
                        updateSortIndicators();
                    } else {
                        // Load more data, append to table
                        removeLoadMoreButton(); // Remove button before adding new data
                        populateDriverHistoryTableFromAPI(historyResponse.getContent());
                    }

                    // Add "Load More" button if there's more data
                    addLoadMoreButtonIfNeeded();
                } else {
                    Toast.makeText(getContext(), "Failed to load driver history", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<DriverHistoryResponse> call, Throwable t) {
                isLoading = false;
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearTableRows() {
        // Clear existing rows except header and separators
        removeLoadMoreButton();
        int childCount = tableDriverHistory.getChildCount();
        if (childCount > 2) {
            tableDriverHistory.removeViews(2, childCount - 2);
        }
    }

    private void populateDriverHistoryTableFromAPI(List<RideHistory> rideHistories) {
        for (RideHistory rideHistory : rideHistories) {
            TableRow tableRow = new TableRow(getContext());
            tableRow.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
            tableRow.setGravity(Gravity.CENTER);
            tableRow.setOnClickListener(v -> openRideDetailsFromAPI(rideHistory));
            tableRow.setBackgroundResource(R.drawable.custom_row_selector);

            // Route column
            TextView routeText = new TextView(getContext());
            String routeDisplay = rideHistory.getRoute().getStartLocation().getAddress() +
                                " → " + rideHistory.getRoute().getEndLocation().getAddress();
            routeText.setText(routeDisplay);
            routeText.setGravity(Gravity.CENTER);
            routeText.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            routeText.setMinWidth(dpToPx(150));
            routeText.setMaxWidth(dpToPx(200));
            routeText.setSingleLine(false);
            routeText.setMaxLines(3);

            TableRow.LayoutParams routeParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            routeParams.gravity = Gravity.CENTER;
            routeText.setLayoutParams(routeParams);
            tableRow.addView(routeText);

            // Passengers column
            TextView passengersText = new TextView(getContext());
            String passengersDisplay = rideHistory.getPassengers() != null && !rideHistory.getPassengers().isEmpty()
                                     ? String.join(", ", rideHistory.getPassengers())
                                     : "N/A";
            passengersText.setText(passengersDisplay);
            passengersText.setGravity(Gravity.CENTER);
            passengersText.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
            passengersText.setMinWidth(dpToPx(120));
            passengersText.setMaxWidth(dpToPx(150));
            passengersText.setTextSize(12f);
            passengersText.setSingleLine(false);
            passengersText.setMaxLines(2);

            TableRow.LayoutParams passengersParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            passengersParams.gravity = Gravity.CENTER;
            passengersText.setLayoutParams(passengersParams);
            tableRow.addView(passengersText);

            // Date column
            TextView dateText = new TextView(getContext());
            dateText.setText(rideHistory.getDate());
            dateText.setGravity(Gravity.CENTER);
            dateText.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

            TableRow.LayoutParams dateParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            dateParams.gravity = Gravity.CENTER;
            dateText.setLayoutParams(dateParams);
            tableRow.addView(dateText);

            // Duration column
            LinearLayout durationLayout = new LinearLayout(getContext());
            durationLayout.setOrientation(LinearLayout.VERTICAL);
            durationLayout.setGravity(Gravity.CENTER);
            durationLayout.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

            TextView durationText = new TextView(getContext());
            String durationDisplay = String.format("%.0f min", rideHistory.getDurationMinutes());
            durationText.setText(durationDisplay);
            durationText.setTextSize(14f);
            durationText.setGravity(Gravity.CENTER);

            TextView timeRangeText = new TextView(getContext());
            // For now, we'll show estimated time as we don't have start/end times in the API
            String timeRangeDisplay = String.format("~%.0f min estimated", rideHistory.getRoute().getEstimatedTimeMin());
            timeRangeText.setText(timeRangeDisplay);
            timeRangeText.setTextSize(12f);
            timeRangeText.setTextColor(Color.parseColor("#666666"));
            timeRangeText.setGravity(Gravity.CENTER);

            durationLayout.addView(durationText);
            durationLayout.addView(timeRangeText);

            TableRow.LayoutParams durationParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            durationParams.gravity = Gravity.CENTER;
            durationLayout.setLayoutParams(durationParams);
            tableRow.addView(durationLayout);

            // Cancelled column
            LinearLayout cancelledLayout = new LinearLayout(getContext());
            cancelledLayout.setOrientation(LinearLayout.VERTICAL);
            cancelledLayout.setGravity(Gravity.CENTER);
            cancelledLayout.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

            if (rideHistory.isCancelled()) {
                TextView cancelledText = new TextView(getContext());
                String cancelledBy = rideHistory.getCancelledBy() != null ? rideHistory.getCancelledBy() : "Cancelled";
                cancelledText.setText(cancelledBy);
                cancelledText.setBackgroundResource(R.drawable.red_rounded_background);
                cancelledText.setTextColor(Color.WHITE);
                cancelledText.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
                cancelledText.setTextSize(15f);
                cancelledText.setGravity(Gravity.CENTER);
                cancelledLayout.addView(cancelledText);
            } else {
                TextView noText = new TextView(getContext());
                noText.setText("N/A");
                noText.setGravity(Gravity.CENTER);
                cancelledLayout.addView(noText);
            }

            TableRow.LayoutParams cancelledParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            cancelledParams.gravity = Gravity.CENTER;
            cancelledLayout.setLayoutParams(cancelledParams);
            tableRow.addView(cancelledLayout);

            // Cost column
            TextView costText = new TextView(getContext());
            String costDisplay = String.format("%.2f RSD", rideHistory.getCost());
            costText.setText(costDisplay);
            costText.setGravity(Gravity.CENTER);
            costText.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

            TableRow.LayoutParams costParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            costParams.gravity = Gravity.CENTER;
            costText.setLayoutParams(costParams);
            tableRow.addView(costText);

            // Panic button column
            LinearLayout panicLayout = new LinearLayout(getContext());
            panicLayout.setOrientation(LinearLayout.VERTICAL);
            panicLayout.setGravity(Gravity.CENTER);
            panicLayout.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

            if (rideHistory.getPanic() != null && rideHistory.getPanic()) {
                TextView panicText = new TextView(getContext());
                panicText.setText("Panic");
                panicText.setBackgroundResource(R.drawable.red_rounded_background);
                panicText.setTextColor(Color.WHITE);
                panicText.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
                panicText.setTextSize(15f);
                panicText.setGravity(Gravity.CENTER);
                panicLayout.addView(panicText);

                if (rideHistory.getPanicBy() != null) {
                    TextView panicByText = new TextView(getContext());
                    panicByText.setText(rideHistory.getPanicBy());
                    panicByText.setTextSize(14f);
                    panicByText.setTextColor(Color.parseColor("#666666"));
                    panicByText.setGravity(Gravity.CENTER);
                    panicByText.setPadding(0, dpToPx(2), 0, 0);
                    panicLayout.addView(panicByText);
                }
            } else {
                TextView noText = new TextView(getContext());
                noText.setText("N/A");
                noText.setGravity(Gravity.CENTER);
                panicLayout.addView(noText);
            }

            TableRow.LayoutParams panicParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            panicParams.gravity = Gravity.CENTER;
            panicLayout.setLayoutParams(panicParams);
            tableRow.addView(panicLayout);

            // Add the row to table
            tableDriverHistory.addView(tableRow);

            // Add separator line
            View separator = new View(getContext());
            separator.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, dpToPx(1)));
            separator.setBackgroundColor(Color.parseColor("#CCCCCC"));
            tableDriverHistory.addView(separator);
        }
    }

    private void openRideDetailsFromAPI(RideHistory rideHistory) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("ride_history", rideHistory);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_driverHistory_to_rideDetails, bundle);
    }

    private void addLoadMoreButtonIfNeeded() {
        if (!hasMoreData) return;

        // Remove existing load more button if any
        removeLoadMoreButton();

        Button loadMoreButton = new Button(getContext());
        loadMoreButton.setText("Load More");
        loadMoreButton.setTag("load_more_button");
        loadMoreButton.setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8));

        TableRow buttonRow = new TableRow(getContext());
        buttonRow.setGravity(Gravity.CENTER);
        buttonRow.setPadding(0, dpToPx(16), 0, dpToPx(16));

        TableRow.LayoutParams buttonParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        buttonParams.span = 7; // Span all columns
        loadMoreButton.setLayoutParams(buttonParams);

        buttonRow.addView(loadMoreButton);
        tableDriverHistory.addView(buttonRow);

        loadMoreButton.setOnClickListener(v -> {
            currentPage++;
            loadDriverHistory();
        });
    }

    private void removeLoadMoreButton() {
        for (int i = tableDriverHistory.getChildCount() - 1; i >= 0; i--) {
            View child = tableDriverHistory.getChildAt(i);
            if (child instanceof TableRow) {
                TableRow row = (TableRow) child;
                if (row.getChildCount() > 0) {
                    View firstChild = row.getChildAt(0);
                    if (firstChild instanceof Button && "load_more_button".equals(firstChild.getTag())) {
                        tableDriverHistory.removeViewAt(i);
                        break;
                    }
                }
            }
        }
    }


    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
