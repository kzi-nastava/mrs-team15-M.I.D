package com.example.ridenow.dto.driver;

public class DriverCanStartRideResponseDTO {
    private Boolean canStartRide;

    // Constructors
    public DriverCanStartRideResponseDTO() {
    }

    public DriverCanStartRideResponseDTO(Boolean canStartRide) {
        this.canStartRide = canStartRide;
    }

    // Getters and Setters
    public Boolean getCanStartRide() {
        return canStartRide;
    }

    public void setCanStartRide(Boolean canStartRide) {
        this.canStartRide = canStartRide;
    }

    // Convenience methods
    public boolean isCanStart() {
        return canStartRide != null && canStartRide;
    }
}
