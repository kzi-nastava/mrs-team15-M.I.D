package com.example.ridenow.util;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import java.util.concurrent.TimeUnit;

public class RideWebSocketManager extends WebSocketListener {
    private static final String TAG = "RideWebSocketManager";
    private static final String WS_BASE_URL = "ws://192.168.1.144:8081/api/notifications/websocket";

    private WebSocket webSocket;
    private final OkHttpClient client;
    private final Gson gson;
    private RideWebSocketCallback callback;

    public interface RideWebSocketCallback {
        void onRidePanic(JsonObject data);
        void onRideStopped(JsonObject data);
        void onRideCompleted(JsonObject data);
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }

    public RideWebSocketManager() {
        this.client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS).build();
        this.gson = new Gson();
    }

    public void setCallback(RideWebSocketCallback callback) {
        this.callback = callback;
    }

    public void connect(String token) {
        String url = WS_BASE_URL + "?token=" + token;
        Log.d(TAG, "Connecting: " + url);
        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, this);
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Normal closure");
            webSocket = null;
        }
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Log.d(TAG, "Connected");
        if (callback != null) callback.onConnected();
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Log.d(TAG, "Received: " + text);
        try {
            JsonObject jsonObject = JsonParser.parseString(text).getAsJsonObject();
            String type = jsonObject.get("action").getAsString();
            JsonObject data = jsonObject.has("data") && jsonObject.get("data").isJsonObject()
                    ? jsonObject.getAsJsonObject("data") : null;

            if (callback == null) {
                return;
            }

            switch (type) {
                case "RIDE_PANIC":
                    callback.onRidePanic(data);
                    break;
                case "RIDE_STOPPED":
                    callback.onRideStopped(data);
                    break;
                case "RIDE_COMPLETED":
                    callback.onRideCompleted(data);
                    break;
                default:
                    Log.d(TAG, "Unhandled type: " + type);
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse error", e);
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        Log.d(TAG, "Closed: " + reason);
        if (callback != null) callback.onDisconnected();
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Log.e(TAG, "Failure", t);
        if (callback != null) callback.onError(t.getMessage());
    }
}