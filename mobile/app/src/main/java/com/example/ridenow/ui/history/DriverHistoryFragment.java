package com.example.ridenow.ui.history;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ridenow.R;

public class DriverHistoryFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        populateDriverHistoryTable();
    }

    private void populateDriverHistoryTable() {
        View rootView = getView();
        if (rootView == null) return;

        TableLayout tableLayout = rootView.findViewById(R.id.tableDriverHistory);

        // Sample data based on your JSON
        String[][] rideData = {
                {"Bulevar oslobođenja, Novi Sad → Aerodrom Nikola Tesla, Beograd", "Marko Marković, Ana Jovanović", "2025-12-12", "25 min", "14:30 - 14:55", null, null, "1550 RSD", null, null},
                {"Trg slobode → Železnička stanica", "Petar Petrović", "2025-12-11", "12 min", "09:15 - 09:27", "Od strane putnika", "Petar Petrović", "800 RSD", null, null},
                {"Liman 3 → Promenada Shopping", "Jovana Nikolić, Stefan Stojanović", "2025-12-12", "18 min", "16:00 - 16:18", null, null, "1275 RSD", "Od strane putnika", "Jovana Nikolić"},
                {"Hotel Park → Spens", "Milica Đorđević", "2025-12-10", "30 min", "11:00 - 11:30", "Od strane vozača", null, "1800 RSD", null, null},
                {"Centar → Štrand", "Nikola Ilić, Jelena Pavlović, Dušan Stanković", "2025-12-12", "22 min", "13:45 - 14:07", null, null, "2050 RSD", null, null}
        };

        for (String[] ride : rideData) {
            TableRow tableRow = new TableRow(getContext());
            tableRow.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
            tableRow.setGravity(Gravity.CENTER);

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

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

}
