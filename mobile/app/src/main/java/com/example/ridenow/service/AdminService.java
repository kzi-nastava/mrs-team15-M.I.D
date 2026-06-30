package com.example.ridenow.service;

import com.example.ridenow.dto.admin.AdminChangesReviewRequestDTO;
import com.example.ridenow.dto.admin.AdminUserResponseDTO;
import com.example.ridenow.dto.admin.BlockUserRequestDTO;
import com.example.ridenow.dto.admin.AdminRideHistoryItemDTO;
import com.example.ridenow.dto.admin.DriverChangeRequestDTO;
import com.example.ridenow.dto.admin.PagedResponseDTO;
import com.example.ridenow.dto.admin.PriceConfigRequestDTO;
import com.example.ridenow.dto.admin.PriceConfigResponseDTO;
import com.example.ridenow.dto.report.ReportResponseDTO;
import com.example.ridenow.dto.user.UserItemDTO;
import com.example.ridenow.dto.user.UserResponseDTO;
import com.example.ridenow.dto.util.PageResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    @GET("/api/admins/users")
    Call<PagedResponseDTO<AdminUserResponseDTO>> getAllUsers(
            @Query("search") String search,
            @Query("sortBy") String sortBy,
            @Query("sortDir") String sortDir,
            @Query("page") int page,
            @Query("size") int size
    );

    @PUT("/api/admins/block/{id}")
    Call<Void> blockUser(@Path("id") Long id, @Body BlockUserRequestDTO dto);

    @PUT("/api/admins/unblock/{id}")
    Call<Void> unblockUser(@Path("id") Long id);

    @GET("/api/admins/report")
    Call<ReportResponseDTO> getReport(
            @Query("startDate") Long startDate,
            @Query("endDate") Long endDate,
            @Query("drivers") boolean drivers,
            @Query("users") boolean users,
            @Query("personId") String personId
    );
    @GET("/api/admins/all-users")
    Call<PageResponse<UserItemDTO>> getAllUsers(@Query("page") int page, @Query("size") int size,
                                                @Query("sortBy") String sortBy, @Query("sortDir") String sortDir);

    @GET("/api/admins/ride-history")
    Call<PageResponse<AdminRideHistoryItemDTO>> getRideHistory( @Query("id") long userId, @Query("page") int page,
                                                                @Query("size") int size, @Query("sortBy") String sortBy,
                                                                @Query("sortDir") String sortDir, @Query("date") Long date);
}
