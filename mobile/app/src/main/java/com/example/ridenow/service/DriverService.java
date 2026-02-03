package com.example.ridenow.service;

import com.example.ridenow.dto.driver.DriverHistoryResponse;
import com.example.ridenow.dto.ride.UpcomingRide;
import com.example.ridenow.dto.user.UserResponseDTO;
import com.example.ridenow.dto.driver.DriverChangeResponseDTO;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DriverService {
    @GET("driver/ride-history")
    Call<DriverHistoryResponse> getDriverRideHistory(@Query("page") int page, @Query("size") int size, @Query("sortBy") String sortBy, @Query("sortDir") String sortOrder, @Query("date") String date);

    @GET("users")
    Call<UserResponseDTO> getUser();

    @Multipart
    @POST("driver/change-request")
    Call<DriverChangeResponseDTO> requestDriverChange(@PartMap Map<String, RequestBody> partMap, @Part MultipartBody.Part profileImage);

    @GET("driver/rides")
    Call<List<UpcomingRide>> getUpcomingRides();
}
