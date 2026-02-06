package com.example.ridenow.dto.ride;

public class InconsistencyRequestDTO {
    private Long rideId;
    private String description;

    public InconsistencyRequestDTO() {}

    public InconsistencyRequestDTO(Long rideId, String description) {
        this.rideId = rideId;
        this.description = description;
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
