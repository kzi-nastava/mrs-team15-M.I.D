package com.example.ridenow.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ridenow.R;
import com.example.ridenow.adapter.NotificationsAdapter;
import com.example.ridenow.dto.enums.NotificationType;
import com.example.ridenow.dto.notification.NotificationResponseDTO;
import com.example.ridenow.service.NotificationService;
import com.example.ridenow.util.ClientUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    private RecyclerView notificationsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private NotificationsAdapter adapter;
    private List<NotificationResponseDTO> notificationsList;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        navController = Navigation.findNavController(view);
        notificationsList = new ArrayList<>();
        setupRecyclerView();
        loadNotifications();
    }

    private void initViews(View view) {
        notificationsRecyclerView = view.findViewById(R.id.notifications_recycler_view);
        progressBar = view.findViewById(R.id.notifications_progress_bar);
        emptyStateText = view.findViewById(R.id.notifications_empty_state);
    }

    private void setupRecyclerView() {
        adapter = new NotificationsAdapter(notificationsList,
            notification -> handleNotificationClick(notification),
            (notification, position) -> deleteNotification(notification, position));
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        notificationsRecyclerView.setAdapter(adapter);
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);

        NotificationService notificationService = ClientUtils.getClient(NotificationService.class);
        notificationService.getAllNotifications().enqueue(new Callback<List<NotificationResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<NotificationResponseDTO>> call, @NonNull Response<List<NotificationResponseDTO>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    notificationsList.clear();
                    notificationsList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (notificationsList.isEmpty()) {
                        emptyStateText.setVisibility(View.VISIBLE);
                        emptyStateText.setText(R.string.no_notifications);
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show();
                    emptyStateText.setVisibility(View.VISIBLE);
                    emptyStateText.setText(R.string.error_loading_notifications);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<NotificationResponseDTO>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                emptyStateText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void handleNotificationClick(NotificationResponseDTO notification) {
        // Mark as seen
        if (!notification.isSeen()) {
            markNotificationAsSeen(notification.getId());
        }

        // Navigate based on notification type
        if (notification.getType() == NotificationType.ADDED_AS_PASSENGER) {
            navController.navigate(R.id.upcoming_rides);
        } else if (notification.getType() == NotificationType.RIDE_STARTED) {
            navController.navigate(R.id.current_ride);
        } else if (notification.getType() == NotificationType.RIDE_FINISHED) {
            if (notification.getRelatedEntityId() != null) {
                Bundle bundle = new Bundle();
                bundle.putString("rideId", String.valueOf(notification.getRelatedEntityId()));
                navController.navigate(R.id.rating, bundle);
            }
        } else if (notification.getType() == NotificationType.RIDE_ASSIGNED) {
            navController.navigate(R.id.upcoming_rides);
        } else if (notification.getType() == NotificationType.PANIC) {
            // For panic notifications, navigate to current ride
            navController.navigate(R.id.current_ride);
        }
    }

    private void markNotificationAsSeen(Long notificationId) {
        NotificationService notificationService = ClientUtils.getClient(NotificationService.class);
        notificationService.markNotificationAsSeen(notificationId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                // Silently mark as seen
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                // Silently fail
            }
        });
    }

    private void deleteNotification(NotificationResponseDTO notification, int position) {
        NotificationService notificationService = ClientUtils.getClient(NotificationService.class);
        notificationService.deleteNotification(notification.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    notificationsList.remove(position);
                    adapter.notifyItemRemoved(position);

                    if (notificationsList.isEmpty()) {
                        emptyStateText.setVisibility(View.VISIBLE);
                        emptyStateText.setText(R.string.no_notifications);
                    }

                    Toast.makeText(requireContext(), "Notification deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to delete notification", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error deleting notification", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
