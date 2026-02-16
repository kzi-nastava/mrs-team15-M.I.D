package com.example.ridenow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ridenow.R;
import com.example.ridenow.dto.notification.NotificationResponseDTO;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private final List<NotificationResponseDTO> notifications;
    private final OnNotificationClickListener listener;
    private final OnNotificationDeleteListener deleteListener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationResponseDTO notification);
    }

    public interface OnNotificationDeleteListener {
        void onNotificationDelete(NotificationResponseDTO notification, int position);
    }

    public NotificationsAdapter(List<NotificationResponseDTO> notifications, OnNotificationClickListener listener, OnNotificationDeleteListener deleteListener) {
        this.notifications = notifications;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationResponseDTO notification = notifications.get(position);
        holder.bind(notification, listener, deleteListener, position);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageTextView;
        private final TextView typeTextView;
        private final TextView timeTextView;
        private final CardView cardView;
        private final View unreadIndicator;
        private final ImageButton deleteButton;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.notification_message);
            typeTextView = itemView.findViewById(R.id.notification_type);
            timeTextView = itemView.findViewById(R.id.notification_time);
            cardView = itemView.findViewById(R.id.notification_card);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
            deleteButton = itemView.findViewById(R.id.notification_delete_btn);
        }

        public void bind(NotificationResponseDTO notification, OnNotificationClickListener listener, OnNotificationDeleteListener deleteListener, int position) {
            messageTextView.setText(notification.getMessage());
            typeTextView.setText(notification.getType().toString());

            // Display createdAt as String directly (already formatted from backend)
            String createdAtStr = notification.getCreatedAt();
            if (createdAtStr != null && !createdAtStr.isEmpty()) {
                timeTextView.setText(formatDateTime(createdAtStr));
            } else {
                timeTextView.setText("");
            }

            // Show unread indicator
            if (!notification.isSeen()) {
                unreadIndicator.setVisibility(View.VISIBLE);
            } else {
                unreadIndicator.setVisibility(View.GONE);
            }

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onNotificationDelete(notification, position);
                }
            });
        }

        private String formatDateTime(String dateTimeString) {
            // Expected format from backend: "2026-02-16T14:30:00" or similar ISO format
            // Extract just the date and time parts
            if (dateTimeString == null || dateTimeString.isEmpty()) {
                return "";
            }

            try {
                // If it's in ISO format like "2026-02-16T14:30:00", extract and reformat
                if (dateTimeString.contains("T")) {
                    String[] parts = dateTimeString.split("T");
                    String datePart = parts[0]; // "2026-02-16"
                    String timePart = parts.length > 1 ? parts[1].substring(0, 5) : ""; // "14:30"

                    // Reformat date from YYYY-MM-DD to DD/MM/YYYY
                    String[] dateComponents = datePart.split("-");
                    if (dateComponents.length == 3) {
                        return dateComponents[2] + "/" + dateComponents[1] + "/" + dateComponents[0] + " " + timePart;
                    }
                }
            } catch (Exception e) {
                // If parsing fails, just return the string as-is
                return dateTimeString;
            }

            return dateTimeString;
        }
    }
}
