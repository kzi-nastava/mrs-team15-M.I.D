package com.example.ridenow.dto.chat;

import com.example.ridenow.dto.model.MessageDTO;
import java.util.List;

public class ChatWithMessagesResponseDTO {
    private Long id;
    private String user;
    private List<MessageDTO> messages;

    public ChatWithMessagesResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public List<MessageDTO> getMessages() { return messages; }
    public void setMessages(List<MessageDTO> messages) { this.messages = messages; }
}
