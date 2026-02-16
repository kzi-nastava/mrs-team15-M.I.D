package com.example.ridenow.service;

import com.example.ridenow.dto.user.FcmTokenDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserService {
    @POST("fcm/register-token")
    Call<Void> registerToken(@Body FcmTokenDTO tokenDTO);
}
