package com.example.ridenow.dto.model;

public class MessageDTO {
    private String content;
    private Boolean userSender;
    private String timestamp;

    public MessageDTO() {}

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Boolean getUserSender() { return userSender; }
    public void setUserSender(Boolean userSender) { this.userSender = userSender; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
