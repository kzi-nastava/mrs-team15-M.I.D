package com.example.ridenow.service;

import com.example.ridenow.dto.rating.RatingRequest;
import com.example.ridenow.dto.rating.RatingResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RideService {
    @POST("rides/{rideId}/rate")
    Call<RatingResponse> rateRide(@Path("rideId") String rideId, @Body RatingRequest ratingRequest);
}
