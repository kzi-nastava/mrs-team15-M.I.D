package com.example.ridenow.service;

import com.example.ridenow.dto.chat.ChatResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ChatService {
    @GET("/api/chats/")
    Call<List<ChatResponseDTO>> getAllChats();

    @POST("/api/chats/user")
    Call<ChatResponseDTO> openChatForUser();

    @PUT("/api/chats/{id}")
    Call<Void> markChatAsTaken(@Path("id") Long chatId);

    @PUT("/api/chats/{id}/close")
    Call<Void> closeChat(@Path("id") Long chatId);
}
