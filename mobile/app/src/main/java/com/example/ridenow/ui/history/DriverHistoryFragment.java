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
import com.example.ridenow.R;

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

    private final String[][] rideDataTest = {
            {"Bulevar oslobođenja, Novi Sad → Aerodrom Nikola Tesla, Beograd", "Marko Marković, Ana Jovanović", "2025-12-12", "25 min", "14:30 - 14:55", null, null, "1550 RSD", null, null, "4", "Losa tura"},
            {"Trg slobode → Železnička stanica", "Petar Petrović", "2025-12-11", "12 min", "09:15 - 09:27", "Od strane putnika", "Petar Petrović", "800 RSD", null, null, "3", null},
            {"Liman 3 → Promenada Shopping", "Jovana Nikolić, Stefan Stojanović", "2025-12-12", "18 min", "16:00 - 16:18", null, null, "1275 RSD", "Od strane putnika", "Jovana Nikolić", "5", null},
            {"Hotel Park → Spens", "Milica Đorđević", "2025-12-10", "30 min", "11:00 - 11:30", "Od strane vozača", null, "1800 RSD", null, null, "2", "Vozac je kasnio"},
            {"Centar → Štrand", "Nikola Ilić, Jelena Pavlović, Dušan Stanković", "2025-12-12", "22 min", "13:45 - 14:07", null, null, "2050 RSD", null, null, "4", null}
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_history, container, false);

        // Initialize views
        etDateFilter = view.findViewById(R.id.etDateFilter);
        btnApplyFilter = view.findViewById(R.id.btnApplyFilter);
        btnClearFilter = view.findViewById(R.id.btnClearFilter);
        tableDriverHistory = view.findViewById(R.id.tableDriverHistory);

        setupDatePicker();
        setupButtons();
        loadDriverHistory();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupHeaderClickListeners();
        populateDriverHistoryTable(rideDataTest);
        sortTable(2);
        updateSortIndicators();
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
        if (currentSortColumn != 2) { // If already sorted by date
            currentSortColumn = 2; // Set to date column
            isAscending = true; // Ascending
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

        updateSortIndicators();

        String[][] sortedData = getSortedData();

        // Clear existing rows except header
        int childCount = tableDriverHistory.getChildCount();
        if (childCount > 2) {
            tableDriverHistory.removeViews(2, childCount - 2);
        }

        populateDriverHistoryTable(sortedData);
    }

    private String[][] getSortedData() {
        String[][] dataToSort;

        if (selectedDate != null) {
            // If filter is applied, get filtered data
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String filterDateString = dateFormat.format(selectedDate.getTime());
            dataToSort = java.util.Arrays.stream(rideDataTest)
                    .filter(ride -> ride[2].equals(filterDateString))
                    .toArray(String[][]::new);
        } else {
            dataToSort = rideDataTest.clone();
        }

        java.util.Arrays.sort(dataToSort, (row1, row2) -> {
            String value1 = row1[currentSortColumn] != null ? row1[currentSortColumn] : "";
            String value2 = row2[currentSortColumn] != null ? row2[currentSortColumn] : "";

            int comparison;

            // Special handling for different column types
            switch (currentSortColumn) {
                case 2: // Date column
                    try {
                        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        java.util.Date date1 = dateFormat.parse(value1);
                        java.util.Date date2 = dateFormat.parse(value2);
                        comparison = date1.compareTo(date2);
                    } catch (Exception e) {
                        comparison = value1.compareTo(value2);
                    }
                    break;
                case 7: // Cost column
                    try {
                        // Extract numeric value from cost string (e.g., "1550 RSD" -> 1550)
                        int cost1 = Integer.parseInt(value1.replaceAll("[^0-9]", ""));
                        int cost2 = Integer.parseInt(value2.replaceAll("[^0-9]", ""));
                        comparison = Integer.compare(cost1, cost2);
                    } catch (Exception e) {
                        comparison = value1.compareTo(value2);
                    }
                    break;
                case 3: // Duration column
                    try {
                        // Extract numeric value from duration string (e.g., "25 min" -> 25)
                        int duration1 = Integer.parseInt(value1.replaceAll("[^0-9]", ""));
                        int duration2 = Integer.parseInt(value2.replaceAll("[^0-9]", ""));
                        comparison = Integer.compare(duration1, duration2);
                    } catch (Exception e) {
                        comparison = value1.compareTo(value2);
                    }
                    break;
                default:
                    comparison = value1.compareToIgnoreCase(value2);
                    break;
            }

            return isAscending ? comparison : -comparison;
        });

        return dataToSort;
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
        // Clear existing rows except header
        int childCount = tableDriverHistory.getChildCount();
        if (childCount > 2) {
            tableDriverHistory.removeViews(2, childCount - 2);
        }

        // Load filtered and sorted data
        String[][] sortedData = getSortedData();
        populateDriverHistoryTable(sortedData);
        Toast.makeText(getContext(), "Filter applied", Toast.LENGTH_SHORT).show();
    }

    private void clearFilter() {
        etDateFilter.setText("");
        selectedDate = null;

        // Clear existing rows except header
        int childCount = tableDriverHistory.getChildCount();
        if (childCount > 2) {
            tableDriverHistory.removeViews(2, childCount - 2);
        }

        // Reload sorted data
        String[][] sortedData = getSortedData();
        populateDriverHistoryTable(sortedData);
        Toast.makeText(getContext(), "Filter cleared", Toast.LENGTH_SHORT).show();
    }

    private void loadDriverHistory() {
        populateDriverHistoryTable(rideDataTest);
    }

    private void populateDriverHistoryTable(String[][] rideData) {
        View rootView = getView();
        if (rootView == null) return;

        TableLayout tableLayout = rootView.findViewById(R.id.tableDriverHistory);

        for (String[] ride : rideData) {
            TableRow tableRow = new TableRow(getContext());
            tableRow.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
            tableRow.setGravity(Gravity.CENTER);
            tableRow.setOnClickListener(v -> openRideDetails(ride));
            tableRow.setBackgroundResource(R.drawable.custom_row_selector);

            // Route column
            TextView routeText = new TextView(getContext());
            routeText.setText(ride[0]);
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
            passengersText.setText(ride[1]);
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
            dateText.setText(ride[2]);
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
            durationText.setText(ride[3]);
            durationText.setTextSize(14f);
            durationText.setGravity(Gravity.CENTER);

            TextView timeRangeText = new TextView(getContext());
            timeRangeText.setText(ride[4]);
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

            if (ride[5] != null) {
                TextView cancelledText = new TextView(getContext());
                String cancelledBy = ride[5];
                cancelledText.setText(cancelledBy);
                cancelledText.setBackgroundResource(R.drawable.red_rounded_background);
                cancelledText.setTextColor(Color.WHITE);
                cancelledText.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
                cancelledText.setTextSize(15f);
                cancelledText.setGravity(Gravity.CENTER);
                cancelledLayout.addView(cancelledText);

                // Show passenger name if cancelled by passenger
                if (ride[6] != null) {
                    TextView cancelledByText = new TextView(getContext());
                    cancelledByText.setText(ride[6]);
                    cancelledByText.setTextSize(14f);
                    cancelledByText.setTextColor(Color.parseColor("#666666"));
                    cancelledByText.setGravity(Gravity.CENTER);
                    cancelledByText.setPadding(0, dpToPx(2), 0, 0);
                    cancelledLayout.addView(cancelledByText);
                }
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
            costText.setText(ride[7]);
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

            if (ride[8] != null) {
                TextView panicText = new TextView(getContext());
                panicText.setText(ride[8]);
                panicText.setBackgroundResource(R.drawable.red_rounded_background);
                panicText.setTextColor(Color.WHITE);
                panicText.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
                panicText.setTextSize(15f);
                panicText.setGravity(Gravity.CENTER);

                if (ride[9] != null) {
                    TextView panicByText = new TextView(getContext());
                    panicByText.setText(ride[9]);
                    panicByText.setTextSize(14f);
                    panicByText.setTextColor(Color.parseColor("#666666"));
                    panicByText.setGravity(Gravity.CENTER);

                    panicLayout.addView(panicText);
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
            tableLayout.addView(tableRow);

            // Add separator line
            View separator = new View(getContext());
            separator.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, dpToPx(1)));
            separator.setBackgroundColor(Color.parseColor("#CCCCCC"));
            tableLayout.addView(separator);
        }
    }

    private void openRideDetails(String[] rideData) {
        Bundle bundle = new Bundle();
        bundle.putStringArray("ride_data", rideData);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_driverHistory_to_rideDetails, bundle);
    }


    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
