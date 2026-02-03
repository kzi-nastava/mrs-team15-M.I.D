package com.example.ridenow.ui.auth;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ridenow.R;
import com.example.ridenow.dto.auth.ResetPasswordRequestDTO;
import com.example.ridenow.dto.auth.VerifyCodeRequestDTO;
import com.example.ridenow.service.AuthService;
import com.example.ridenow.ui.auth.util.PasswordToggleUtil;
import com.example.ridenow.util.ClientUtils;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordFragment extends Fragment {

    private TextView tvBackToLogin;
    private EditText etNewPassword;
    private  EditText etConfirmPassword;
    private AuthService authService;
    private Button btnResetPassword;
    private String token;

    public ResetPasswordFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authService = ClientUtils.getClient(AuthService.class);
        tvBackToLogin = view.findViewById(R.id.tvBackToLogin);
        tvBackToLogin.setOnClickListener(
                v -> NavHostFragment.findNavController(this).navigate(R.id.login)
        );
        token = getArguments() != null ? getArguments().getString("token") : "";
        etNewPassword = view.findViewById(R.id.etNewPassword);
        PasswordToggleUtil.addPasswordToggle(etNewPassword);

        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        PasswordToggleUtil.addPasswordToggle(etConfirmPassword);

        btnResetPassword = view.findViewById(R.id.btnResetPassword);
        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String newPassword = etNewPassword.getText() == null ? "" : etNewPassword.getText().toString();
        String newPasswordConfirmed = etConfirmPassword.getText() == null ? "" : etConfirmPassword.getText().toString();

        if(newPassword.isEmpty()){
            etNewPassword.setError("New password is required");
            etNewPassword.requestFocus();
            return;
        }

        if(newPasswordConfirmed.isEmpty()){
            etConfirmPassword.setError("Confirm password is required");
            etConfirmPassword.requestFocus();
            return;
        }

        if(newPassword.length() < 6){
            etNewPassword.setError("Password must be at least 6 characters");
            etNewPassword.requestFocus();
            return;
        }

        if(!newPassword.equals(newPasswordConfirmed)){
            etConfirmPassword.setError("Passwords must match");
            etConfirmPassword.requestFocus();
            return;
        }

        btnResetPassword.setEnabled(false);

        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO(newPassword, newPasswordConfirmed);
        authService.resetPassword(token, dto).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                btnResetPassword.setEnabled(true);
                if(response.isSuccessful()){
                    Toast.makeText(getContext(), "Password reset successfully!", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(ResetPasswordFragment.this).navigate(R.id.login);
                }
                else{
                    String errorMessage = "Error resetting password";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            // Try to extract the message from JSON if possible
                            if (errorBody.contains("\"message\"")) {
                                int start = errorBody.indexOf("\"message\":\"") + 11;
                                int end = errorBody.indexOf("\"", start);
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
                    } catch (Exception e) {
                        errorMessage = "Error resetting password (code: " + response.code() + ")";
                    }
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                btnResetPassword.setEnabled(true);
                Toast.makeText(getContext(),"Network error: " + t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}