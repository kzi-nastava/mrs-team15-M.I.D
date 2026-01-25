package com.example.ridenow.service;

import com.example.ridenow.dto.driver.DriverHistoryResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DriverService {
    @GET("driver/ride-history")
    Call<DriverHistoryResponse> getDriverRideHistory(@Query("page") int page, @Query("size") int size, @Query("sortBy") String sortBy, @Query("sortDir") String sortOrder, @Query("date") String date);

}
