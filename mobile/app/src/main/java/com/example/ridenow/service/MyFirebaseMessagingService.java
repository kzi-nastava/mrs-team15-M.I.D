package com.example.ridenow.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.example.ridenow.R;
import com.example.ridenow.dto.user.FcmTokenDTO;
import com.example.ridenow.ui.main.MainActivity;
import com.example.ridenow.util.ClientUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "ridenow_notifications";
    private static final String TAG = "FCM_Service";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {
            sendNotification(remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody());
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);
        registerTokenWithBackend(token);
    }

    private void registerTokenWithBackend(String token) {
        try {
            // Check if user is logged in before sending token
            com.example.ridenow.util.TokenUtils tokenUtils = ClientUtils.getTokenUtils();
            if (!tokenUtils.isLoggedIn()) {
                Log.d(TAG, "User not logged in, skipping FCM token registration");
                return;
            }

            Log.d(TAG, "Registering FCM token with backend: " + token);
            UserService userService = ClientUtils.getClient(UserService.class);
            FcmTokenDTO tokenDTO = new FcmTokenDTO();
            tokenDTO.setToken(token);

            userService.registerToken(tokenDTO).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "FCM token registered successfully: " + token);
                    } else {
                        Log.e(TAG, "Failed to register FCM token. Code: " + response.code());
                        if (response.errorBody() != null) {
                            try {
                                Log.e(TAG, "Error body: " + response.errorBody().string());
                            } catch (Exception e) {
                                Log.e(TAG, "Could not read error body", e);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Log.e(TAG, "Error registering FCM token: " + t.getMessage(), t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error creating service to register token: " + e.getMessage(), e);
        }
    }

    private void sendNotification(String title, String body) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        // Add flag to navigate to notifications
        intent.putExtra("navigateToNotifications", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel() {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "RideNow Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
    }
}
