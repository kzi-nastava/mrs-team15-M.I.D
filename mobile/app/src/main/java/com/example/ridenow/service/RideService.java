package com.example.ridenow.service;

import com.example.ridenow.dto.rating.RatingRequest;
import com.example.ridenow.dto.rating.RatingResponse;
import com.example.ridenow.dto.ride.CurrentRideResponse;
import com.example.ridenow.dto.ride.InconsistencyRequest;
import com.example.ridenow.dto.ride.TrackVehicleResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

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
}
