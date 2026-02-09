package com.example.ridenow.service;

import com.example.ridenow.dto.auth.ActivateAccountRequestDTO;
import com.example.ridenow.dto.auth.ForgotPasswordRequestDTO;
import com.example.ridenow.dto.auth.LoginRequestDTO;
import com.example.ridenow.dto.auth.LoginResponseDTO;
import com.example.ridenow.dto.auth.LogoutResponseDTO;
import com.example.ridenow.dto.auth.RegisterResponseDTO;
import com.example.ridenow.dto.auth.ResetPasswordRequestDTO;
import com.example.ridenow.dto.auth.VerifyCodeRequestDTO;
import com.example.ridenow.dto.driver.DriverChangeResponseDTO;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
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

    @PUT("auth/reset-password")
    Call<Map<String, String>> resetPassword(@Query("token") String token, @Body ResetPasswordRequestDTO dto);

    @Multipart
    @POST("auth/register")
    Call<RegisterResponseDTO> register(@PartMap Map<String, RequestBody> partMap, @Part MultipartBody.Part profileImage);
    @PUT("auth/activate-code")
    Call<Map<String, String>> activateCode(@Body VerifyCodeRequestDTO dto);

    @POST("auth/resend-activation-email")
    Call<Void> resendActivationEmail(@Body  ActivateAccountRequestDTO dto);

}