package com.example.ridenow.service;

import com.example.ridenow.dto.report.ReportResponseDTO;
import com.example.ridenow.dto.user.BlockedStatusResponseDTO;
import com.example.ridenow.dto.user.FcmTokenDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserService {
    @POST("fcm/register-token")
    Call<Void> registerToken(@Body FcmTokenDTO tokenDTO);

    @GET("/api/users/blocked-status")
    Call<BlockedStatusResponseDTO> getBlockedStatus();

    @GET("/api/users/report")
    Call<ReportResponseDTO> getReport(
            @Query("startDate") Long startDate,
            @Query("endDate") Long endDate
    );
}
