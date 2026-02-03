package com.example.ridenow.service;

import com.example.ridenow.dto.auth.ForgotPasswordRequestDTO;
import com.example.ridenow.dto.auth.LoginRequestDTO;
import com.example.ridenow.dto.auth.LoginResponseDTO;
import com.example.ridenow.dto.auth.LogoutResponseDTO;
import com.example.ridenow.dto.auth.ResetPasswordRequestDTO;
import com.example.ridenow.dto.auth.VerifyCodeRequestDTO;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthService {
    @POST("auth/login")
    Call<LoginResponseDTO> login(@Body LoginRequestDTO dto);

    @POST("auth/logout")
    Call<LogoutResponseDTO> logout();

    @POST("auth/forgot-password")
    Call<Void> forgotPassword(@Body ForgotPasswordRequestDTO dto);

    @POST("auth/verify-reset-code")
    Call<Map<String, String>> verifyResetCode(@Body VerifyCodeRequestDTO dto);

    @POST("auth/reset-password")
    Call<Map<String, String>> resetPassword(@Query("token") String token, @Body ResetPasswordRequestDTO dto);

}