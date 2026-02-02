package com.example.ridenow.service;

import com.example.ridenow.dto.auth.LogoutResponseDTO;
import com.example.ridenow.util.ClientUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogoutService {
    public interface LogoutCallback {
        void onLogoutSuccess();
        void onLogoutFailure(String error);
    }

    public static void logout(LogoutCallback callback) {
        AuthService authService = ClientUtils.getClient(AuthService.class);
        authService.logout().enqueue(new Callback<LogoutResponseDTO>() {
            @Override
            public void onResponse(Call<LogoutResponseDTO> call, Response<LogoutResponseDTO> response) {
                ClientUtils.getTokenUtils().clearAuthData();
                if (response.isSuccessful() && response.body() != null) {
                    callback.onLogoutSuccess();
                } else {
                    String errorMessage = "Logout failed";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            if (errorBody.contains("\"message\"")) {
                                errorMessage = errorBody;
                            } else {
                                errorMessage = errorBody;
                                if (errorMessage.startsWith("\"") && errorMessage.endsWith("\"")) {
                                    errorMessage = errorMessage.substring(1, errorMessage.length() - 1);
                                }
                            }
                        } else {
                            errorMessage = "Logout failed (code: " + response.code() + ")";
                        }
                    } catch (Exception e) {
                        errorMessage = "Logout failed (code: " + response.code() + ")";
                    }
                    callback.onLogoutFailure(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<LogoutResponseDTO> call, Throwable t) {
                ClientUtils.getTokenUtils().clearAuthData();
                callback.onLogoutFailure("Network error: " + t.getMessage());
            }
        });
    }
}