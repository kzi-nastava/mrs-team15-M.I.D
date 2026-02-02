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
import com.example.ridenow.dto.auth.LoginRequestDTO;
import com.example.ridenow.dto.auth.LoginResponseDTO;
import com.example.ridenow.service.AuthService;
import com.example.ridenow.ui.auth.util.PasswordToggleUtil;
import com.example.ridenow.ui.main.MainActivity;
import com.example.ridenow.util.ClientUtils;
import com.example.ridenow.util.TokenUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    public LoginFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        PasswordToggleUtil.addPasswordToggle(etPassword);

        Button btnLogin = view.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> {
                    String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
                    String password = etPassword.getText() == null ? "" : etPassword.getText().toString().trim();

                    if (email.isEmpty()) {
                        etEmail.setError("Email is required");
                        etEmail.requestFocus();
                        return;
                    }

                    if (password.isEmpty()) {
                        etPassword.setError("Password is required");
                        etPassword.requestFocus();
                        return;
                    }

                    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                    if (!email.matches(emailPattern)) {
                        etEmail.setError("Enter a valid email");
                        etEmail.requestFocus();
                        return;
                    }

                    if (password.length() < 6) {
                        etPassword.setError("Password must be at least 6 characters");
                        etPassword.requestFocus();
                        return;
                    }

                    LoginRequestDTO dto = new LoginRequestDTO(email, password);
                    AuthService authService = ClientUtils.getClient(AuthService.class);
                    btnLogin.setEnabled(false);

                    authService.login(dto).enqueue(new Callback<LoginResponseDTO>() {
                        @Override
                        public void onResponse(Call<LoginResponseDTO> call, Response<LoginResponseDTO> response) {
                            btnLogin.setEnabled(true);
                            if(response.isSuccessful() && response.body() != null){
                                LoginResponseDTO resp = response.body();
                                TokenUtils tokenUtils = ClientUtils.getTokenUtils();
                                tokenUtils.saveAuthData(
                                        resp.getToken(), resp.getRole(),
                                        resp.getExpiresAt(), resp.getHasCurrentRide()
                                );
                                Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                                if (getActivity() instanceof MainActivity) {
                                    ((MainActivity) getActivity()).onLoginSuccess();
                                }
                                NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.nav_home);
                            }
                            else{
                                String errorMessage = "Login failed";
                                try {
                                    if (response.errorBody() != null) {
                                        String errorBody = response.errorBody().string();
                                        errorMessage = errorBody;
                                        if (errorMessage.startsWith("\"") && errorMessage.endsWith("\"")) {
                                            errorMessage = errorMessage.substring(1, errorMessage.length() - 1);
                                        }
                                    }
                                } catch (Exception e) {
                                    errorMessage = "Login failed (code: " + response.code() + ")";
                                }

                                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<LoginResponseDTO> call, Throwable t) {
                            btnLogin.setEnabled(true);
                            Toast.makeText(requireContext(),"Error: " + t.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
        );
        TextView tvSignUp = view.findViewById(R.id.tvSignUp);
        tvSignUp.setOnClickListener(
                v -> NavHostFragment.findNavController(this).navigate(R.id.registration)
        );

        TextView tvForgotPasswordLink = view.findViewById(R.id.tvForgotPasswordLink);
        tvForgotPasswordLink.setOnClickListener(
                v -> NavHostFragment.findNavController(this).navigate(R.id.forgot_password)
        );
    }
}