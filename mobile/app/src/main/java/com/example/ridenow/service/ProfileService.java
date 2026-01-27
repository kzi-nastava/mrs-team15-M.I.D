package com.example.ridenow.service;

import com.example.ridenow.dto.user.UserResponseDTO;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.PUT;

public interface ProfileService {
    @GET("users")
    Call<UserResponseDTO> getUser();

    @Multipart
    @PUT("users")
    Call<Void> updateUser(@PartMap Map<String, RequestBody> partMap, @Part MultipartBody.Part profileImage);
}

