package com.example.ridenow.service;

import com.example.ridenow.dto.admin.AdminChangesReviewRequestDTO;
import com.example.ridenow.dto.admin.DriverChangeRequestDTO;
import com.example.ridenow.dto.admin.PriceConfigRequestDTO;
import com.example.ridenow.dto.admin.PriceConfigResponseDTO;
import com.example.ridenow.dto.user.UserResponseDTO;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AdminService {

    @GET("/api/admins/driver-requests")
    Call<List<DriverChangeRequestDTO>> getDriverRequests();

    @PUT("/api/admins/driver-requests/{requestId}")
    Call<Void> reviewDriverRequest(@Path("requestId") long requestId, @Body AdminChangesReviewRequestDTO dto);

    @GET("/api/admins/users/{id}")
    Call<UserResponseDTO> getUserById(@Path("id") long id);

    @GET("/api/admins/price-configs")
    Call<PriceConfigResponseDTO> getPriceConfig();

    @PUT("/api/admins/price-configs")
    Call<Void> updatePriceConfig(@Body PriceConfigRequestDTO dto);
}
