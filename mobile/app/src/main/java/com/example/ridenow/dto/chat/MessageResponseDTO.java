package com.example.ridenow.dto.chat;

public class MessageResponseDTO {
    private Long id;
    private String content;

    public MessageResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
