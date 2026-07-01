package com.example.ridenow.dto.chat;

public class ChatResponseDTO {
    private Long id;
    private String user;
    private boolean hasNewMessages;

    public ChatResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public boolean getHasNewMessages() { return hasNewMessages; }
    public void setHasNewMessages(boolean hasNewMessages) { this.hasNewMessages = hasNewMessages; }
}
