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
import com.example.ridenow.dto.auth.ForgotPasswordRequestDTO;
import com.example.ridenow.dto.auth.VerifyCodeRequestDTO;
import com.example.ridenow.service.AuthService;
import com.example.ridenow.util.ClientUtils;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordVerifyCodeFragment extends Fragment {

    private EditText etCode;
    private TextView tvResendCodeLink;
    private TextView tvBackToLoginLink;
    private AuthService authService;

    private Button btnVerifyCode;
    private String email;

    public ForgotPasswordVerifyCodeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password_verify_code, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AuthService authService = ClientUtils.getClient(AuthService.class);
        etCode = view.findViewById(R.id.etCode);
        tvResendCodeLink = view.findViewById(R.id.tvResendCode);
        tvBackToLoginLink = view.findViewById(R.id.tvBackToLogin);
        btnVerifyCode = view.findViewById(R.id.btnVerifyCode);
        email = getArguments() != null ? getArguments().getString("email") : "";

        tvResendCodeLink.setOnClickListener(v -> resendCode());
        btnVerifyCode.setOnClickListener(v -> verifyCode());
        tvBackToLoginLink.setOnClickListener(
                v -> NavHostFragment.findNavController(this).navigate(R.id.login)
        );
    }

    private void verifyCode() {
        String code = etCode.getText() == null ? "" : etCode.getText().toString();
        if(code.isEmpty()){
            etCode.setError("Code is required");
            etCode.requestFocus();
            return;
        }
        if (code.length() != 6) {
            etCode.setError("Please enter a 6-digit code");
            etCode.requestFocus();
            return;
        }
        btnVerifyCode.setEnabled(true);
        VerifyCodeRequestDTO dto = new VerifyCodeRequestDTO(email, code);
        authService.verifyResetCode(dto).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if(response.isSuccessful() && response.body() != null){
                    String token = response.body().get("token");
                    Toast.makeText(getContext(), "Code verified!", Toast.LENGTH_SHORT).show();
                    Bundle bundle = new Bundle();
                    bundle.putString("token", token);
                    //NavHostFragment.findNavController(ForgotPasswordVerifyCodeFragment.this).navigate(R.id.action_verifyCode_to_resetPassword, bundle);
                }
                else{
                    Toast.makeText(getContext(), "Invalid or expired code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                btnVerifyCode.setEnabled(true);
                Toast.makeText(getContext(),"Network error: " + t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendCode() {
        tvResendCodeLink.setEnabled(false);
        ForgotPasswordRequestDTO dto = new ForgotPasswordRequestDTO(email);
        authService.forgotPassword(dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                tvResendCodeLink.setEnabled(true);
                if(response.isSuccessful()){
                    Toast.makeText(getContext(),  "New code sent to your email!",Toast.LENGTH_LONG).show();
                    etCode.setText("");
                }
                else{
                    Toast.makeText(getContext(),"Failed to resend code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(),"Network error: " + t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}