package com.example.ridenow.service;

import com.example.ridenow.dto.auth.LogoutResponseDTO;
import com.example.ridenow.util.ClientUtils;
import com.google.gson.Gson;

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
                if (response.isSuccessful() && response.body() != null) {
                    ClientUtils.getTokenUtils().clearAuthData();
                    callback.onLogoutSuccess();
                } else {
                    String errorMessage = "Logout failed";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Gson gson = new Gson();
                            LogoutResponseDTO errorDto = gson.fromJson(errorBody, LogoutResponseDTO.class);
                            if (errorDto != null && errorDto.getMessage() != null) {
                                errorMessage = errorDto.getMessage();
                            } else {
                                errorMessage = errorBody;
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
                callback.onLogoutFailure("Network error: " + t.getMessage());
            }
        });
    }
}