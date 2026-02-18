package com.example.ridenow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ridenow.R;
import com.example.ridenow.dto.chat.WebSocketMessageDTO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {
    private final List<WebSocketMessageDTO> messages = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final boolean isAdmin;

    public ChatMessageAdapter(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void addMessage(WebSocketMessageDTO message) {
        // Validate message before adding
        if (message != null && message.getContent() != null && !message.getContent().trim().isEmpty()) {
            messages.add(message);
            notifyItemInserted(messages.size() - 1);
        }
    }


    public void clearMessages() {
        int size = messages.size();
        messages.clear();
        notifyItemRangeRemoved(0, size);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        WebSocketMessageDTO message = messages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout userMessageCard;
        private final LinearLayout supportMessageCard;
        private final TextView userMessageText;
        private final TextView userMessageTime;
        private final TextView supportMessageText;
        private final TextView supportMessageTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            userMessageCard = itemView.findViewById(R.id.userMessageCard);
            supportMessageCard = itemView.findViewById(R.id.supportMessageCard);
            userMessageText = itemView.findViewById(R.id.userMessageText);
            userMessageTime = itemView.findViewById(R.id.userMessageTime);
            supportMessageText = itemView.findViewById(R.id.supportMessageText);
            supportMessageTime = itemView.findViewById(R.id.supportMessageTime);
        }

        public void bind(WebSocketMessageDTO message) {
            // Skip empty or null content messages
            if (message.getContent() == null || message.getContent().trim().isEmpty()) {
                // Hide both cards for empty messages
                userMessageCard.setVisibility(View.GONE);
                supportMessageCard.setVisibility(View.GONE);
                return;
            }

            // Determine if this is a sent message or received message
            // For admin: user messages are received (left), support messages are sent (right)
            // For user: user messages are sent (right), support messages are received (left)
            String sender = message.getSender();
            boolean isUserMessage = "user".equals(sender);
            boolean isSentMessage = isAdmin ? !isUserMessage : isUserMessage;

            if (isSentMessage) {
                // Sent message - show on right with black background
                userMessageCard.setVisibility(View.VISIBLE);
                supportMessageCard.setVisibility(View.GONE);
                userMessageText.setText(message.getContent());
                userMessageTime.setText(formatTime(message.getTimestamp()));
            } else {
                // Received message - show on left with white background
                userMessageCard.setVisibility(View.GONE);
                supportMessageCard.setVisibility(View.VISIBLE);
                supportMessageText.setText(message.getContent());
                supportMessageTime.setText(formatTime(message.getTimestamp()));
            }
        }

        private String formatTime(String timestamp) {
            if (timestamp == null || timestamp.isEmpty()) {
                return timeFormat.format(new Date());
            }

            try {
                // Try to parse ISO timestamp format: 2026-02-11T15:14:21.860301
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
                Date date = isoFormat.parse(timestamp);
                return timeFormat.format(date);
            } catch (Exception e1) {
                try {
                    // Try to parse ISO timestamp without microseconds: 2026-02-11T15:14:21.860
                    SimpleDateFormat isoFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
                    Date date = isoFormat2.parse(timestamp);
                    return timeFormat.format(date);
                } catch (Exception e2) {
                    try {
                        // Try to parse ISO timestamp without milliseconds: 2026-02-11T15:14:21
                        SimpleDateFormat isoFormat3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                        Date date = isoFormat3.parse(timestamp);
                        return timeFormat.format(date);
                    } catch (Exception e3) {
                        // If all parsing fails, return current time
                        return timeFormat.format(new Date());
                    }
                }
            }
        }
    }
}
