package com.example.ridenow.dto.ride;

public class CancelRideRequestDTO {
    private String reason;
    public CancelRideRequestDTO() {}
    public CancelRideRequestDTO(String reason) {
        this.reason = reason;
    }
    public String getReason() {
        return reason;
    }
    public void setReason(String reason) {
        this.reason = reason;
    }
}