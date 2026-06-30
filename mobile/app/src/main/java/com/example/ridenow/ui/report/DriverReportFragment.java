package com.example.ridenow.ui.report;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ridenow.R;
import com.example.ridenow.dto.report.ReportResponseDTO;
import com.example.ridenow.service.UserService;
import com.example.ridenow.util.ClientUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverReportFragment extends Fragment {

    private static final String TAG = "DriverReportFragment";
    private static final SimpleDateFormat DISPLAY_FMT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private EditText etStartDate, etEndDate;
    private Button btnLoadReport;
    private TextView tvMessage;
    private ReportTabsView reportTabsView;

    private UserService userService;

    private Long startDateMillis = null;
    private Long endDateMillis = null;
    private boolean loading = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userService = ClientUtils.getClient(UserService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etStartDate = view.findViewById(R.id.etStartDate);
        etEndDate = view.findViewById(R.id.etEndDate);
        btnLoadReport = view.findViewById(R.id.btnLoadReport);
        tvMessage = view.findViewById(R.id.tvMessage);
        reportTabsView = view.findViewById(R.id.reportTabsView);

        reportTabsView.setRole(true, false);

        etStartDate.setOnClickListener(v -> pickDate(true));
        etEndDate.setOnClickListener(v -> pickDate(false));
        btnLoadReport.setOnClickListener(v -> loadReport());
    }

    private void pickDate(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, day, 0, 0, 0);
            selected.set(Calendar.MILLISECOND, 0);
            long millis = selected.getTimeInMillis();
            if (isStart) {
                startDateMillis = millis;
                etStartDate.setText(DISPLAY_FMT.format(selected.getTime()));
            } else {
                endDateMillis = millis;
                etEndDate.setText(DISPLAY_FMT.format(selected.getTime()));
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadReport() {
        hideMessage();
        setLoading(true);

        Call<ReportResponseDTO> call = userService.getReport(startDateMillis, endDateMillis);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ReportResponseDTO> call, @NonNull Response<ReportResponseDTO> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    renderReport(response.body());
                } else {
                    Log.e(TAG, "Failed to load report: " + response.code());
                    showMessage("Failed to load report.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReportResponseDTO> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Network error loading report", t);
                showMessage("Failed to request report.");
            }
        });
    }

    private void renderReport(ReportResponseDTO resp) {
        ReportDataProcessor.Result result = ReportDataProcessor.process(resp, startDateMillis, endDateMillis);

        if (!result.hasRange) {
            reportTabsView.clearCharts();
            reportTabsView.setSummaries(0d, 0d, 0d, 0d, 0d, 0d);
            showMessage("No report data for the selected range.");
            return;
        }

        if (result.allZero) {
            reportTabsView.clearCharts();
            reportTabsView.setSummaries(null, null, null, null, null, null);
            showMessage("You have no ride history.");
            return;
        }

        hideMessage();
        reportTabsView.setSummaries(result.ridesSum, result.ridesAvg, result.kmSum, result.kmAvg, result.moneySum, result.moneyAvg);
        reportTabsView.renderCharts(result.labels, result.rides, result.km, result.money);
    }

    private void setLoading(boolean isLoading) {
        loading = isLoading;
        btnLoadReport.setEnabled(!isLoading);
        btnLoadReport.setText(isLoading ? "Loading..." : "Load report");
    }

    private void showMessage(String msg) {
        tvMessage.setText(msg);
        tvMessage.setVisibility(View.VISIBLE);
    }

    private void hideMessage() {
        tvMessage.setVisibility(View.GONE);
    }
}
