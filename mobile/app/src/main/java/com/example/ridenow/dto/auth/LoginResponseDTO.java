package com.example.ridenow.dto.auth;

import com.google.gson.annotations.SerializedName;

public class LoginResponseDTO {
    private String token;
    private String role;
    private long expiresAt;

    @SerializedName("hasCurrentRide")
    private boolean hasCurrentRide;

    public LoginResponseDTO() {}
    public LoginResponseDTO(String token, String role, long expiresAt, boolean hasCurrentRide) {
        this.token = token;
        this.role = role;
        this.expiresAt = expiresAt;
        this.hasCurrentRide = hasCurrentRide;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
    public boolean getHasCurrentRide() {
        return hasCurrentRide;
    }
    public void setHasCurrentRide(boolean hasCurrentRide) {
        this.hasCurrentRide = hasCurrentRide;
    }
}