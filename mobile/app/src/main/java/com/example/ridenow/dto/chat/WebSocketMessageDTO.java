package com.example.ridenow.dto.chat;

import java.util.List;

public class WebSocketMessageDTO {
    private String type;
    private Object data; // Can be either MessageData or List<MessageData>
    private String error;

    public WebSocketMessageDTO() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public boolean hasError() { return error != null && !error.isEmpty(); }

    // Helper methods to access message content directly (for single messages)
    public String getContent() {
        MessageData msgData = getSingleMessageData();
        return msgData != null ? msgData.getContent() : null;
    }

    public String getSender() {
        MessageData msgData = getSingleMessageData();
        if (msgData != null) {
            return msgData.isUserSender() ? "user" : "support";
        }
        return null;
    }

    public String getTimestamp() {
        MessageData msgData = getSingleMessageData();
        return msgData != null ? msgData.getTimestamp() : null;
    }

    private MessageData getSingleMessageData() {
        if (data instanceof MessageData) {
            return (MessageData) data;
        }
        return null;
    }

    public static class MessageData {
        private String content;
        private boolean userSender;
        private String timestamp;

        public MessageData() {}

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public boolean isUserSender() { return userSender; }
        public void setUserSender(boolean userSender) { this.userSender = userSender; }

        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }
}
