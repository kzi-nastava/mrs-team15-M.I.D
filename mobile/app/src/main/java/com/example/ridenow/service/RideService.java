package com.example.ridenow.service;

import com.example.ridenow.dto.rating.RatingRequestDTO;
import com.example.ridenow.dto.rating.RatingResponseDTO;
import com.example.ridenow.dto.ride.ActiveRideDTO;
import com.example.ridenow.dto.ride.CancelRideRequestDTO;
import com.example.ridenow.dto.ride.CurrentRideResponse;
import com.example.ridenow.dto.ride.InconsistencyRequestDTO;
import com.example.ridenow.dto.ride.RideEstimateResponseDTO;
import com.example.ridenow.dto.ride.TrackVehicleResponseDTO;
import com.example.ridenow.dto.ride.UpcomingRideResponseDTO;
import com.example.ridenow.dto.ride.EstimateRouteRequestDTO;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RideService {
    @POST("rides/{rideId}/rate")
    Call<RatingResponseDTO> rateRide(@Path("rideId") String rideId, @Body RatingRequestDTO ratingRequest);

    @GET("rides/my-current-ride")
    Call<CurrentRideResponse> getCurrentRide();

    @GET("rides/{rideId}/track")
    Call<TrackVehicleResponseDTO> trackVehicle(@Path("rideId") String rideId);

    @POST("rides/inconsistency")
    Call<Void> reportInconsistency(@Body InconsistencyRequestDTO inconsistencyRequest);

    @POST("rides/{rideId}/finish")
    Call<Boolean> finishRide(@Path("rideId") String rideId);

    @GET("rides/estimate")
    Call<RideEstimateResponseDTO> estimate(@Query("startLatitude") Double startLatitude,
                                           @Query("startLongitude") Double startLongitude,
                                           @Query("endLatitude") Double endLatitude,
                                           @Query("endLongitude") Double endLongitude);

    @POST("rides/estimate-route")
    Call<RideEstimateResponseDTO> estimateRoute(@Body EstimateRouteRequestDTO request);

    @PUT("rides/{id}/cancel")
    Call<Void> cancel(@Path("id") Long id, @Body CancelRideRequestDTO request);

    @GET("rides/my-upcoming-rides")
    Call<List<UpcomingRideResponseDTO>>getUpcomingRides();

    @POST("rides/panic-alert")
    Call <Map<String, String>> triggerPanicAlert();

    @GET("/api/rides/active-rides")
    Call<List<ActiveRideDTO>> getActiveRides();
}
