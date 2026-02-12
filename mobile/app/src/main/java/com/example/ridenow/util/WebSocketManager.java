package com.example.ridenow.util;

import android.util.Log;
import com.example.ridenow.dto.chat.MessageRequestDTO;
import com.example.ridenow.dto.chat.WebSocketMessageDTO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import java.util.concurrent.TimeUnit;

public class WebSocketManager extends WebSocketListener {
    private static final String TAG = "WebSocketManager";
    private static final String WS_BASE_URL = "ws://10.0.2.2:8081/api/chat/websocket";

    private WebSocket webSocket;
    private final OkHttpClient client;
    private final Gson gson;
    private WebSocketCallback callback;
    private int authAttempt = 0;
    private Long currentChatId;
    private String currentToken;

    public interface WebSocketCallback {
        void onConnected();
        void onMessage(WebSocketMessageDTO message);
        void onDisconnected();
        void onError(String error);
    }

    public WebSocketManager() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    public void setCallback(WebSocketCallback callback) {
        this.callback = callback;
    }

    public void connectToChat(Long chatId, String token) {
        if (webSocket != null) {
            disconnect();
        }

        this.currentChatId = chatId;
        this.currentToken = token;
        this.authAttempt = 0;

        // Try multiple authentication methods
        connectWithQueryParam(chatId, token);
    }

    private void connectWithQueryParam(Long chatId, String token) {
        String url = WS_BASE_URL + "/" + chatId + "?token=" + token;
        Log.d(TAG, "Connecting to WebSocket with query param: " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        webSocket = client.newWebSocket(request, this);
    }

    public void sendMessage(String content) {
        if (webSocket != null && isConnected()) {
            MessageRequestDTO message = new MessageRequestDTO(content);
            String json = gson.toJson(message);
            Log.d(TAG, "Sending WebSocket message: " + json);
            webSocket.send(json);
            Log.d(TAG, "Sent message with content: " + content);
        } else {
            Log.e(TAG, "WebSocket is not connected");
            if (callback != null) {
                callback.onError("WebSocket is not connected");
            }
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Normal closure");
            webSocket = null;
        }
    }

    public boolean isConnected() {
        return webSocket != null;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Log.d(TAG, "WebSocket connected");
        if (callback != null) {
            callback.onConnected();
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.d(TAG, "Received raw WebSocket message: " + text);
        try {
            JsonElement jsonElement = JsonParser.parseString(text);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            String messageType = jsonObject.get("type").getAsString();
            Log.d(TAG, "Message type: " + messageType);

            if (jsonObject.has("error")) {
                String error = jsonObject.get("error").getAsString();
                Log.e(TAG, "WebSocket message contains error: " + error);
                if (callback != null) {
                    callback.onError(error);
                }
                return;
            }

            if ("message".equals(messageType)) {
                // Handle single message
                JsonObject dataObject = jsonObject.getAsJsonObject("data");
                WebSocketMessageDTO.MessageData messageData = gson.fromJson(dataObject, WebSocketMessageDTO.MessageData.class);

                WebSocketMessageDTO message = new WebSocketMessageDTO();
                message.setType(messageType);
                message.setData(messageData);

                Log.d(TAG, "Parsed single message: content='" + message.getContent() +
                           "', sender='" + message.getSender() +
                           "', timestamp='" + message.getTimestamp() + "'");

                if (callback != null) {
                    callback.onMessage(message);
                }

            } else if ("existing_messages".equals(messageType)) {
                // Handle array of existing messages
                JsonArray dataArray = jsonObject.getAsJsonArray("data");
                Log.d(TAG, "Received " + dataArray.size() + " existing messages");

                for (JsonElement element : dataArray) {
                    WebSocketMessageDTO.MessageData messageData = gson.fromJson(element, WebSocketMessageDTO.MessageData.class);

                    WebSocketMessageDTO message = new WebSocketMessageDTO();
                    message.setType("message"); // Treat each existing message as a regular message
                    message.setData(messageData);

                    Log.d(TAG, "Parsed existing message: content='" + message.getContent() +
                               "', sender='" + message.getSender() +
                               "', timestamp='" + message.getTimestamp() + "'");

                    if (callback != null) {
                        callback.onMessage(message);
                    }
                }
            } else {
                Log.w(TAG, "Unknown message type: " + messageType);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing WebSocket message: " + text, e);
            if (callback != null) {
                callback.onError("Error parsing message: " + e.getMessage());
            }
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        Log.d(TAG, "WebSocket closing. Code: " + code + ", Reason: " + reason);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        Log.d(TAG, "WebSocket closed. Code: " + code + ", Reason: " + reason);
        if (callback != null) {
            callback.onDisconnected();
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.e(TAG, "WebSocket error", t);

        String errorMsg = "Connection failed";
        if (response != null) {
            errorMsg += ": HTTP " + response.code() + " " + response.message();
            Log.e(TAG, "WebSocket failure response: " + response.code() + " " + response.message());

            // Log response body if available
            try {
                if (response.body() != null) {
                    String responseBody = response.body().string();
                    Log.e(TAG, "WebSocket failure response body: " + responseBody);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reading response body", e);
            }
        }

        if (t != null) {
            errorMsg += " - " + t.getMessage();
            Log.e(TAG, "WebSocket failure throwable: " + t.getClass().getSimpleName() + ": " + t.getMessage());
        }

        if (callback != null) {
            callback.onError(errorMsg);
        }
    }
}
