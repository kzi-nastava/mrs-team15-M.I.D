package com.example.ridenow.service;

import com.example.ridenow.dto.auth.LoginRequestDTO;
import com.example.ridenow.dto.auth.LoginResponseDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("login")
    Call<LoginResponseDTO> login(@Body LoginRequestDTO dto);
}
