package com.example.ridenow.service;

import com.example.ridenow.dto.driver.DriverCanStartRideResponseDTO;
import com.example.ridenow.dto.driver.DriverHistoryResponseDTO;
import com.example.ridenow.dto.driver.DriverLocationRequestDTO;
import com.example.ridenow.dto.driver.DriverLocationResponseDTO;
import com.example.ridenow.dto.ride.UpcomingRideResponseDTO;
import com.example.ridenow.dto.user.UserResponseDTO;
import com.example.ridenow.dto.driver.DriverChangeResponseDTO;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DriverService {
    @GET("driver/ride-history")
    Call<DriverHistoryResponseDTO> getDriverRideHistory(@Query("page") int page, @Query("size") int size, @Query("sortBy") String sortBy, @Query("sortDir") String sortOrder, @Query("date") String date);

    @GET("users")
    Call<UserResponseDTO> getUser();

    @Multipart
    @POST("driver/change-request")
    Call<DriverChangeResponseDTO> requestDriverChange(@PartMap Map<String, RequestBody> partMap, @Part MultipartBody.Part profileImage);

    @GET("driver/rides")
    Call<List<UpcomingRideResponseDTO>> getUpcomingRides();

    @PUT("driver/update-location")
    Call<DriverLocationResponseDTO> updateDriverLocation(@Body DriverLocationRequestDTO request);

    @GET("driver/can-start-ride")
    Call<DriverCanStartRideResponseDTO> canStartRide();
}
