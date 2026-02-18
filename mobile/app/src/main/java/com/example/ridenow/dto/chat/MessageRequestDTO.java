package com.example.ridenow.dto.chat;

public class MessageRequestDTO {
    private String content;

    public MessageRequestDTO() {}

    public MessageRequestDTO(String content) {
        this.content = content;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
