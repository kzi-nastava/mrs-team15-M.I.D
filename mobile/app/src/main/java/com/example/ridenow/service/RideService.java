package com.example.ridenow.service;

import com.example.ridenow.dto.rating.RatingRequest;
import com.example.ridenow.dto.rating.RatingResponse;
import com.example.ridenow.dto.ride.CancelRideRequestDTO;
import com.example.ridenow.dto.ride.CurrentRideResponse;
import com.example.ridenow.dto.ride.InconsistencyRequest;
import com.example.ridenow.dto.ride.RideEstimateResponseDTO;
import com.example.ridenow.dto.ride.TrackVehicleResponse;
import com.example.ridenow.dto.ride.UpcomingRideResponse;

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
    Call<RatingResponse> rateRide(@Path("rideId") String rideId, @Body RatingRequest ratingRequest);

    @GET("rides/my-current-ride")
    Call<CurrentRideResponse> getCurrentRide();

    @GET("rides/{rideId}/track")
    Call<TrackVehicleResponse> trackVehicle(@Path("rideId") String rideId);

    @POST("rides/inconsistency")
    Call<Void> reportInconsistency(@Body InconsistencyRequest inconsistencyRequest);

    @POST("rides/{rideId}/finish")
    Call<Boolean> finishRide(@Path("rideId") String rideId);

    @GET("rides/estimate")
    Call<RideEstimateResponseDTO> estimate(@Query("startLatitude") Double startLatitude,
                                           @Query("startLongitude") Double startLongitude,
                                           @Query("endLatitude") Double endLatitude,
                                           @Query("endLongitude") Double endLongitude);

    @PUT("rides/{id}/cancel")
    Call<Map<String, String>> cancel(@Path("id") Long id, @Body CancelRideRequestDTO request);

    @GET("rides/my-upcoming-rides")
    Call<List<UpcomingRideResponse>>getUpcomingRides();
}
