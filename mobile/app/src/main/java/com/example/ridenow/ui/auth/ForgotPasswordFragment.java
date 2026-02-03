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
import com.example.ridenow.service.AuthService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordFragment extends Fragment {

    private EditText etEmail;
    private Button btnSendCode;
    private AuthService authService;

    public ForgotPasswordFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEmail = view.findViewById(R.id.etEmail);
        btnSendCode = view.findViewById(R.id.btnSendCode);
        btnSendCode.setOnClickListener(v -> sendResetCode());

        TextView tvBackToLogin = view.findViewById(R.id.tvBackToLogin);
        tvBackToLogin.setOnClickListener(
                v -> NavHostFragment.findNavController(this).navigate(R.id.login)
        );
    }

    private void sendResetCode() {
        String email = etEmail.getText() == null ? "" : etEmail.getText().toString();

        if(email.isEmpty()){
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (!email.matches(emailPattern)) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        ForgotPasswordRequestDTO dto = new ForgotPasswordRequestDTO(email);
        authService.forgotPassword(dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()){
                    Toast.makeText(getContext(),"Reset code sent to your email!",Toast.LENGTH_LONG).show();
                    Bundle bundle = new Bundle();
                    bundle.putString("email", email);
//                  NavHostFragment.findNavController(ForgotPasswordFragment.this).navigate(R.id.action_forgotPassword_to_verifyCode, bundle);
                }
                else{
                    Toast.makeText(getContext(),"Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(),"Network error: " + t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
