package com.example.ridenow.service;

import com.example.ridenow.dto.auth.LoginRequestDTO;
import com.example.ridenow.dto.auth.LoginResponseDTO;
import com.example.ridenow.dto.auth.LogoutResponseDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("auth/login")
    Call<LoginResponseDTO> login(@Body LoginRequestDTO dto);
    @POST("auth/logout")
    Call<LogoutResponseDTO> logout();
}
