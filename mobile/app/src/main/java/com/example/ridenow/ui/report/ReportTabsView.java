package com.example.ridenow.ui.report;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.ridenow.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.tabs.TabLayout;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Native equivalent of the Angular <app-report-tabs> component.
 * Hosts three tabs (Rides / Distance / Money) each with a sum/avg row and a chart,
 * matching the bar (rides) + line (distance, money) chart types used on the web.
 */
public class ReportTabsView extends LinearLayout {

    private TabLayout tabLayout;
    private LinearLayout paneRides, paneDistance, paneExpense;
    private TextView tvRidesSum, tvRidesAvg, tvKmSum, tvKmAvg, tvMoneySum, tvMoneyAvg;
    private BarChart ridesChart;
    private LineChart distanceChart;
    private LineChart expenseChart;

    private boolean isDriver = false;
    private boolean isAdmin = false;

    private final DecimalFormat intFmt = new DecimalFormat("#,##0");
    private final DecimalFormat dec2Fmt = new DecimalFormat("#,##0.00");
    private final DecimalFormat dec3Fmt = new DecimalFormat("#,##0.000");

    public ReportTabsView(Context context) {
        super(context);
        init(context);
    }

    public ReportTabsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_report_tabs, this, true);

        tabLayout = findViewById(R.id.reportTabLayout);
        paneRides = findViewById(R.id.paneRides);
        paneDistance = findViewById(R.id.paneDistance);
        paneExpense = findViewById(R.id.paneExpense);

        tvRidesSum = findViewById(R.id.tvRidesSum);
        tvRidesAvg = findViewById(R.id.tvRidesAvg);
        tvKmSum = findViewById(R.id.tvKmSum);
        tvKmAvg = findViewById(R.id.tvKmAvg);
        tvMoneySum = findViewById(R.id.tvMoneySum);
        tvMoneyAvg = findViewById(R.id.tvMoneyAvg);

        ridesChart = findViewById(R.id.ridesChart);
        distanceChart = findViewById(R.id.distanceChart);
        expenseChart = findViewById(R.id.expenseChart);

        styleBarChart(ridesChart);
        styleLineChart(distanceChart);
        styleLineChart(expenseChart);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                paneRides.setVisibility(tab.getPosition() == 0 ? VISIBLE : GONE);
                paneDistance.setVisibility(tab.getPosition() == 1 ? VISIBLE : GONE);
                paneExpense.setVisibility(tab.getPosition() == 2 ? VISIBLE : GONE);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /** Mirrors [isDriver] / [isAdmin] @Input()s — controls the Money tab label (Earned / Spent / Money). */
    public void setRole(boolean isDriver, boolean isAdmin) {
        this.isDriver = isDriver;
        this.isAdmin = isAdmin;
        TabLayout.Tab expenseTab = tabLayout.getTabAt(2);
        if (expenseTab != null) {
            expenseTab.setText(isAdmin ? "Money" : (isDriver ? "Earned" : "Spent"));
        }
    }

    /** Sets the sum/avg summary numbers shown above each chart (null renders as "-"). */
    public void setSummaries(Double ridesSum, Double ridesAvg,
                              Double kmSum, Double kmAvg,
                              Double moneySum, Double moneyAvg) {
        tvRidesSum.setText("Sum: " + (ridesSum != null ? intFmt.format(ridesSum) : "-"));
        tvRidesAvg.setText("Avg: " + (ridesAvg != null ? dec2Fmt.format(ridesAvg) : "-"));

        tvKmSum.setText("Sum (km): " + (kmSum != null ? dec3Fmt.format(kmSum) : "-"));
        tvKmAvg.setText("Avg (km): " + (kmAvg != null ? dec3Fmt.format(kmAvg) : "-"));

        tvMoneySum.setText("Sum (RSD): " + (moneySum != null ? dec2Fmt.format(moneySum) + " RSD" : "-"));
        tvMoneyAvg.setText("Avg (RSD): " + (moneyAvg != null ? dec2Fmt.format(moneyAvg) + " RSD" : "-"));
    }

    /** Clears all three charts (call when there's no data to show). */
    public void clearCharts() {
        ridesChart.clear();
        distanceChart.clear();
        expenseChart.clear();
    }

    /** Renders the three charts from parallel label/value lists, same shape as the web renderChartsFromResponse(). */
    public void renderCharts(List<String> labels, List<Double> rides, List<Double> distances, List<Double> expenses) {
        // Rides -> bar chart
        List<BarEntry> rideEntries = new java.util.ArrayList<>();
        for (int i = 0; i < rides.size(); i++) {
            rideEntries.add(new BarEntry(i, rides.get(i).floatValue()));
        }
        BarDataSet rideSet = new BarDataSet(rideEntries, "Rides");
        rideSet.setColor(Color.BLACK);
        rideSet.setValueTextColor(Color.BLACK);
        ridesChart.setData(new BarData(rideSet));
        ridesChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        ridesChart.invalidate();

        // Distance -> line chart
        List<Entry> kmEntries = new java.util.ArrayList<>();
        for (int i = 0; i < distances.size(); i++) {
            kmEntries.add(new Entry(i, distances.get(i).floatValue()));
        }
        LineDataSet kmSet = new LineDataSet(kmEntries, "Distance (km)");
        kmSet.setColor(Color.BLACK);
        kmSet.setCircleColor(Color.BLACK);
        kmSet.setDrawFilled(false);
        kmSet.setValueTextColor(Color.BLACK);
        distanceChart.setData(new LineData(kmSet));
        distanceChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        distanceChart.invalidate();

        // Expense -> line chart
        List<Entry> moneyEntries = new java.util.ArrayList<>();
        for (int i = 0; i < expenses.size(); i++) {
            moneyEntries.add(new Entry(i, expenses.get(i).floatValue()));
        }
        String moneyLabel = isAdmin ? "Amount" : (isDriver ? "Earned" : "Spent");
        LineDataSet moneySet = new LineDataSet(moneyEntries, moneyLabel);
        moneySet.setColor(Color.BLACK);
        moneySet.setCircleColor(Color.BLACK);
        moneySet.setDrawFilled(false);
        moneySet.setValueTextColor(Color.BLACK);
        expenseChart.setData(new LineData(moneySet));
        expenseChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        expenseChart.invalidate();
    }

    private void styleBarChart(BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setTextColor(Color.BLACK);
        chart.getAxisLeft().setTextColor(Color.BLACK);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setTextColor(Color.BLACK);
    }

    private void styleLineChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setGranularity(1f);
        chart.getXAxis().setTextColor(Color.BLACK);
        chart.getAxisLeft().setTextColor(Color.BLACK);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setTextColor(Color.BLACK);
    }
}
