package com.example.ridenow.service;

import com.example.ridenow.dto.vehicle.VehicleResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface VehicleService {
    @GET("vehicles/")
    Call<List<VehicleResponseDTO>> getAllVehicles(@Query("lat") Double lat, @Query("lon") Double lon);

}
