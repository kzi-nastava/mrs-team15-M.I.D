package com.example.ridenow.service;

import com.example.ridenow.dto.notification.NotificationResponseDTO;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface NotificationService {
    @GET("/api/notifications")
    Call<List<NotificationResponseDTO>> getAllNotifications();

    @GET("/api/notifications/unseen")
    Call<List<NotificationResponseDTO>> getUnseenNotifications();

    @GET("/api/notifications/count")
    Call<Map<String, Long>> getUnseenCount();

    @PUT("/api/notifications/{id}/seen")
    Call<Void> markNotificationAsSeen(@Path("id") Long id);

    @PUT("/api/notifications/mark-all-seen")
    Call<Void> markAllNotificationsSeen();

    @DELETE("/api/notifications/delete/{id}")
    Call<Void> deleteNotification(@Path("id") Long id);
}
