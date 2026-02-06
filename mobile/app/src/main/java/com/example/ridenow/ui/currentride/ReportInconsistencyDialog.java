package com.example.ridenow.ui.currentride;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.ridenow.R;
import com.example.ridenow.dto.ride.InconsistencyRequestDTO;
import com.example.ridenow.service.RideService;
import com.example.ridenow.util.ClientUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportInconsistencyDialog {

    public interface OnInconsistencyReportedListener {
        void onInconsistencyReported();
    }

    private Dialog dialog;
    private EditText inconsistencyEditText;
    private TextView characterCounter;
    private Button submitButton;
    private Button cancelButton;

    private RideService rideService;
    private Long rideId;
    private OnInconsistencyReportedListener listener;

    public ReportInconsistencyDialog(Context context, Long rideId, OnInconsistencyReportedListener listener) {
        this.rideId = rideId;
        this.listener = listener;
        this.rideService = ClientUtils.getClient(RideService.class);

        initializeDialog(context);
    }

    private void initializeDialog(Context context) {
        dialog = new Dialog(context);
        dialog.setCancelable(true);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_report_inconsistency, null);
        dialog.setContentView(dialogView);

        // Configure dialog window
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        // Initialize views
        inconsistencyEditText = dialogView.findViewById(R.id.inconsistencyEditText);
        characterCounter = dialogView.findViewById(R.id.characterCounter);
        submitButton = dialogView.findViewById(R.id.submitButton);
        cancelButton = dialogView.findViewById(R.id.cancelButton);

        setupTextWatcher();
        setupButtonListeners();
    }

    private void setupTextWatcher() {
        inconsistencyEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                String counterText = dialog.getContext().getString(R.string.report_inconsistency_counter, length);
                characterCounter.setText(counterText);

                // Enable submit button only if text is not empty and within limit
                boolean isValid = length > 0 && length <= 300;
                submitButton.setEnabled(isValid);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupButtonListeners() {
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        submitButton.setOnClickListener(v -> {
            String description = inconsistencyEditText.getText().toString().trim();
            if (validateInput(description)) {
                submitInconsistencyReport(description);
            }
        });
    }

    private boolean validateInput(String description) {
        if (description.isEmpty()) {
            Toast.makeText(dialog.getContext(), "Please describe the inconsistency", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (description.length() > 300) {
            Toast.makeText(dialog.getContext(), "Description must be 300 characters or less", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void submitInconsistencyReport(String description) {
        // Disable button to prevent multiple submissions
        submitButton.setEnabled(false);
        submitButton.setText(R.string.report_inconsistency_submitting);

        InconsistencyRequestDTO request = new InconsistencyRequestDTO(rideId, description);

        Call<Void> call = rideService.reportInconsistency(request);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(dialog.getContext(), "Inconsistency reported successfully", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onInconsistencyReported();
                    }
                    dialog.dismiss();
                } else {
                    showError("Failed to submit report. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                showError("Network error. Please check your connection and try again.");
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(dialog.getContext(), message, Toast.LENGTH_LONG).show();

        // Re-enable button
        submitButton.setEnabled(true);
        submitButton.setText(R.string.report_inconsistency_submit);
    }

    public void show() {
        if (dialog != null) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
