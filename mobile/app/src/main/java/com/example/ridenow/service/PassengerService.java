package com.example.ridenow.service;

import com.example.ridenow.dto.passenger.RideHistoryItemDTO;
import com.example.ridenow.dto.ride.FavoriteRouteResponseDTO;
import com.example.ridenow.dto.ride.RouteResponseDTO;
import com.example.ridenow.dto.util.PageResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PassengerService {

    @GET("passengers/ride-history")
    Call<PageResponse<RideHistoryItemDTO>> getPassengerRideHistory(@Query("page") int page, @Query("size") int size,
            @Query("sortBy") String sortBy, @Query("sortDir") String sortDir, @Query("date") Long date);

    @GET("passengers/favorite-routes")
    Call<List<FavoriteRouteResponseDTO>> getFavoriteRoutes();

    @GET("passengers/favorite-routes/{id}")
    Call<RouteResponseDTO> getFavoriteRoute(@Path("id") Long id);

    @POST("passengers/favorite-routes/{routeId}")
    Call<Void> addFavorite(@Path("routeId") Long routeId);

    @DELETE("passengers/favorite-routes/{routeId}")
    Call<Void> removeFavorite(@Path("routeId") Long routeId);
}
