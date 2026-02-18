package com.example.ridenow.ui.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ridenow.R;
import com.example.ridenow.adapter.ChatMessageAdapter;
import com.example.ridenow.dto.chat.ChatResponseDTO;
import com.example.ridenow.dto.chat.WebSocketMessageDTO;
import com.example.ridenow.service.ChatService;
import com.example.ridenow.util.ClientUtils;
import com.example.ridenow.util.TokenUtils;
import com.example.ridenow.util.WebSocketManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment implements WebSocketManager.WebSocketCallback {
    private static final String TAG = "ChatFragment";
    private static final String ARG_CHAT_ID = "chat_id";
    private static final String ARG_IS_ADMIN = "is_admin";

    private TextView connectionStatus;
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private Button sendButton;

    private ChatMessageAdapter messageAdapter;
    private WebSocketManager webSocketManager;
    private ChatService chatService;
    private TokenUtils tokenUtils;

    private Long chatId;
    private boolean isAdmin = false;

    public static ChatFragment newInstance(Long chatId, boolean isAdmin) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CHAT_ID, chatId != null ? chatId : -1L);
        args.putBoolean(ARG_IS_ADMIN, isAdmin);
        fragment.setArguments(args);
        return fragment;
    }

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            long argChatId = getArguments().getLong(ARG_CHAT_ID, -1L);
            chatId = argChatId != -1L ? argChatId : null;
            isAdmin = getArguments().getBoolean(ARG_IS_ADMIN, false);
        }

        chatService = ClientUtils.getClient(ChatService.class);
        tokenUtils = ClientUtils.getTokenUtils();
        webSocketManager = new WebSocketManager();
        webSocketManager.setCallback(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();

        if (chatId != null) {
            // Admin opening existing chat
            if (isAdmin) {
                markChatAsTaken();
            }
            connectToChat();
        } else {
            // User creating new chat
            openChatForUser();
        }
    }

    private void initViews(View view) {
        TextView chatTitle = view.findViewById(R.id.chatTitle);
        connectionStatus = view.findViewById(R.id.connectionStatus);
        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);

        // Set different titles for admin vs user
        if (isAdmin) {
            chatTitle.setText("Support Chat");
        } else {
            chatTitle.setText(R.string.chat_title);
        }
    }

    private void setupRecyclerView() {
        messageAdapter = new ChatMessageAdapter(isAdmin);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void setupListeners() {
        sendButton.setOnClickListener(v -> sendMessage());
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void openChatForUser() {
        connectionStatus.setText(R.string.chat_connecting);

        chatService.openChatForUser().enqueue(new Callback<ChatResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponseDTO> call, @NonNull Response<ChatResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    chatId = response.body().getId();
                    Log.d(TAG, "Chat opened with ID: " + chatId);
                    connectToChat();
                } else {
                    Log.e(TAG, "Failed to open chat: " + response.code());
                    connectionStatus.setText("Failed to connect to support");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponseDTO> call, @NonNull Throwable t) {
                Log.e(TAG, "Error opening chat", t);
                connectionStatus.setText("Failed to connect to support");
            }
        });
    }

    private void markChatAsTaken() {
        if (chatId == null) return;

        chatService.markChatAsTaken(chatId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Chat marked as taken");
                } else {
                    Log.e(TAG, "Failed to mark chat as taken: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Error marking chat as taken", t);
            }
        });
    }

    private void connectToChat() {
        if (chatId == null) {
            Log.e(TAG, "Cannot connect: chat ID is null");
            return;
        }

        String token = tokenUtils.getToken();
        if (token == null) {
            Log.e(TAG, "Cannot connect: no auth token");
            connectionStatus.setText("Authentication error");
            return;
        }

        webSocketManager.connectToChat(chatId, token);
    }

    private void sendMessage() {
        String content = messageInput.getText() != null ? messageInput.getText().toString().trim() : "";
        if (TextUtils.isEmpty(content)) {
            return;
        }

        // Send via WebSocket - the server will echo back the message which we'll display
        webSocketManager.sendMessage(content);

        // Clear input
        messageInput.setText("");
    }

    private void scrollToBottom() {
        if (messageAdapter.getItemCount() > 0) {
            messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void onConnected() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                connectionStatus.setText(R.string.chat_connected);
                sendButton.setEnabled(true);
            });
        }
    }

    @Override
    public void onMessage(WebSocketMessageDTO message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Log.d(TAG, "Received message: content='" + (message.getContent() != null ? message.getContent() : "null") +
                           "', sender='" + (message.getSender() != null ? message.getSender() : "null") +
                           "', timestamp='" + (message.getTimestamp() != null ? message.getTimestamp() : "null") + "'");

                // Validate message before adding
                if (message != null && message.getContent() != null && !message.getContent().trim().isEmpty()) {
                    messageAdapter.addMessage(message);
                    scrollToBottom();
                } else {
                    Log.w(TAG, "Received empty or invalid message, skipping");
                }
            });
        }
    }

    @Override
    public void onDisconnected() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                connectionStatus.setText(R.string.chat_disconnected);
                sendButton.setEnabled(false);
            });
        }
    }

    @Override
    public void onError(String error) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                connectionStatus.setText(getString(R.string.chat_error, error));
                sendButton.setEnabled(false);
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private boolean chatCleanedUp = false;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView called");
        cleanupChat();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
        cleanupChat();
    }


    private void cleanupChat() {
        // Only cleanup once to prevent multiple API calls
        if (chatCleanedUp) {
            Log.d(TAG, "Chat already cleaned up, skipping");
            return;
        }

        chatCleanedUp = true;
        Log.d(TAG, "Performing chat cleanup");

        // Disconnect WebSocket
        if (webSocketManager != null) {
            Log.d(TAG, "Disconnecting WebSocket");
            webSocketManager.disconnect();
        }

        // Close chat if admin
        if (isAdmin && chatId != null) {
            Log.d(TAG, "Closing admin chat: " + chatId);
            chatService.closeChat(chatId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Admin chat closed successfully");
                    } else {
                        Log.w(TAG, "Failed to close admin chat: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Log.e(TAG, "Error closing admin chat", t);
                }
            });
        }
    }
}
