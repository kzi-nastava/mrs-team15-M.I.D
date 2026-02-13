package com.example.ridenow.ui.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ridenow.R;
import com.example.ridenow.adapter.AdminChatAdapter;
import com.example.ridenow.dto.chat.ChatResponseDTO;
import com.example.ridenow.service.ChatService;
import com.example.ridenow.util.ClientUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminChatsFragment extends Fragment {
    private static final String TAG = "AdminChatsFragment";

    private RecyclerView chatsRecyclerView;
    private LinearLayout emptyStateLayout;

    private AdminChatAdapter chatAdapter;
    private ChatService chatService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatService = ClientUtils.getClient(ChatService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_chats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        loadChats();
    }

    private void initViews(View view) {
        chatsRecyclerView = view.findViewById(R.id.chatsRecyclerView);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
    }

    private void setupRecyclerView() {
        chatAdapter = new AdminChatAdapter();
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsRecyclerView.setAdapter(chatAdapter);

        chatAdapter.setOnChatClickListener(this::openChat);
    }

    private void loadChats() {
        chatService.getAllChats().enqueue(new Callback<List<ChatResponseDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ChatResponseDTO>> call,
                                 @NonNull Response<List<ChatResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChatResponseDTO> chats = response.body();
                    Log.d(TAG, "Loaded " + chats.size() + " chats");

                    if (chats.isEmpty()) {
                        showEmptyState();
                    } else {
                        showChats(chats);
                    }
                } else {
                    Log.e(TAG, "Failed to load chats: " + response.code());
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ChatResponseDTO>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loading chats", t);
                showEmptyState();
            }
        });
    }

    private void showChats(List<ChatResponseDTO> chats) {
        chatsRecyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
        chatAdapter.updateChats(chats);
    }

    private void showEmptyState() {
        chatsRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    private void openChat(ChatResponseDTO chat) {
        Log.d(TAG, "Opening chat: " + chat.getId());

        Bundle args = new Bundle();
        args.putLong("chat_id", chat.getId());
        args.putBoolean("is_admin", true);

        Navigation.findNavController(requireView())
                .navigate(R.id.action_adminChats_to_chat, args);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload chats when returning to this fragment
        loadChats();
    }
}
