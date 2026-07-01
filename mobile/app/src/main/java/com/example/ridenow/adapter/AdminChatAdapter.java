package com.example.ridenow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ridenow.R;
import com.example.ridenow.dto.chat.ChatResponseDTO;

import java.util.ArrayList;
import java.util.List;

public class AdminChatAdapter extends RecyclerView.Adapter<AdminChatAdapter.ChatViewHolder> {
    private final List<ChatResponseDTO> chats = new ArrayList<>();
    private OnChatClickListener onChatClickListener;

    public interface OnChatClickListener {
        void onChatClick(ChatResponseDTO chat);
    }

    public void setOnChatClickListener(OnChatClickListener listener) {
        this.onChatClickListener = listener;
    }

    public void updateChats(List<ChatResponseDTO> newChats) {
        chats.clear();
        chats.addAll(newChats);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatResponseDTO chat = chats.get(position);
        holder.bind(chat);
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {
        private final TextView chatUserText;
        private final TextView chatIdText;
        private final TextView statusBadge;
        private final TextView newBadge;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            chatUserText = itemView.findViewById(R.id.chatUserText);
            chatIdText = itemView.findViewById(R.id.chatIdText);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            newBadge = itemView.findViewById(R.id.newBadge);


            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onChatClickListener != null) {
                    onChatClickListener.onChatClick(chats.get(position));
                }
            });
        }

        public void bind(ChatResponseDTO chat) {
            chatUserText.setText(chat.getUser() != null ? chat.getUser() : "Anonymous User");
            chatIdText.setText("Chat #" + chat.getId());

            if (chat.getHasNewMessages()) {
                newBadge.setVisibility(View.VISIBLE);

                // Highlight the background of the entire row (e.g., using a subtle color from your colors.xml)
                itemView.setBackgroundColor(
                        itemView.getContext().getColor(R.color.black) // Replace with your actual highlight color
                );
            } else {
                newBadge.setVisibility(View.GONE);

                // Reset background to default transparent when there are no new messages
                itemView.setBackgroundColor(
                        itemView.getContext().getColor(android.R.color.transparent)
                );
            }
        }
    }
}
