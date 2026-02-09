package com.example.ridenow.service;

import com.example.ridenow.dto.passenger.RideHistoryItemDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PassengerService {

    @GET("passengers/ride-history")
    Call<List<RideHistoryItemDTO>> getPassengerRideHistory();
}
