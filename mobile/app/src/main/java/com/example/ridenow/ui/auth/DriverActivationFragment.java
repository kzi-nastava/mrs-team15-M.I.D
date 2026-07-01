package com.example.ridenow.ui.auth;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ridenow.R;
import com.example.ridenow.dto.driver.DriverAccountActivationRequestDTO;
import com.example.ridenow.service.DriverService;
import com.example.ridenow.ui.auth.util.PasswordToggleUtil;
import com.example.ridenow.util.ClientUtils;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverActivationFragment extends Fragment {
    private EditText etToken;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private Button btnActivate;
    private TextView tvBackToLogin;
    private DriverService driverService;

    public DriverActivationFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_activation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        driverService = ClientUtils.getClient(DriverService.class);

        etToken = view.findViewById(R.id.etToken);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnActivate = view.findViewById(R.id.btnActivate);
        tvBackToLogin = view.findViewById(R.id.tvBackToLogin);

        PasswordToggleUtil.addPasswordToggle(etPassword);
        PasswordToggleUtil.addPasswordToggle(etConfirmPassword);

        String token = getArguments() != null ? getArguments().getString("token", "") : "";
        if (!token.isEmpty()) {
            etToken.setText(token);
        }

        btnActivate.setOnClickListener(v -> activateDriver());
        tvBackToLogin.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.login));
    }

    private void activateDriver() {
        String token = valueOf(etToken);
        String password = valueOf(etPassword);
        String confirmPassword = valueOf(etConfirmPassword);

        if (token.isEmpty()) {
            etToken.setError("Activation token is required");
            etToken.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords must match");
            etConfirmPassword.requestFocus();
            return;
        }

        btnActivate.setEnabled(false);
        DriverAccountActivationRequestDTO dto = new DriverAccountActivationRequestDTO(password, confirmPassword, token);
        driverService.activateDriverAccount(dto).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                btnActivate.setEnabled(true);
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Account activated successfully", Toast.LENGTH_LONG).show();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (isAdded()) {
                            NavHostFragment.findNavController(DriverActivationFragment.this).navigate(R.id.login);
                        }
                    }, 2000);
                } else {
                    Toast.makeText(requireContext(), parseErrorMessage(response), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                btnActivate.setEnabled(true);
                Toast.makeText(requireContext(), "Activation failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String valueOf(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private String parseErrorMessage(Response<?> response) {
        String errorMessage = "Activation failed";
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                if (errorBody.contains("\"message\"")) {
                    int start = errorBody.indexOf("\"message\":\"") + 11;
                    int end = errorBody.indexOf('"', start);
                    if (start > 10 && end > start) {
                        errorMessage = errorBody.substring(start, end);
                    }
                } else {
                    errorMessage = errorBody;
                    if (errorMessage.startsWith("\"") && errorMessage.endsWith("\"")) {
                        errorMessage = errorMessage.substring(1, errorMessage.length() - 1);
                    }
                }
            }
        } catch (Exception ignored) {
            errorMessage = "Activation failed (code: " + response.code() + ")";
        }
        return errorMessage;
    }
}