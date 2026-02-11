package com.example.ridenow.service;

import com.example.ridenow.dto.passenger.RideHistoryItemDTO;
import com.example.ridenow.dto.ride.FavoriteRouteResponseDTO;
import com.example.ridenow.dto.ride.RouteResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PassengerService {

    @GET("passengers/ride-history")
    Call<List<RideHistoryItemDTO>> getPassengerRideHistory();

    @GET("passengers/favorite-routes")
    Call<List<FavoriteRouteResponseDTO>> getFavoriteRoutes();

    @GET("passengers/favorite-routes/{id}")
    Call<RouteResponseDTO> getFavoriteRoute(@Path("id") Long id);
}
