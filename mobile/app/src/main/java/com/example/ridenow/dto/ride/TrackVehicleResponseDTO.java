package com.example.ridenow.dto.ride;

import com.example.ridenow.dto.model.LocationDTO;

public class TrackVehicleResponseDTO {
    private LocationDTO location;
    private Integer remainingTimeInMinutes;

    public LocationDTO getLocation() {
        return location;
    }

    public void setLocation(LocationDTO location) {
        this.location = location;
    }

    public Integer getRemainingTimeInMinutes() {
        return remainingTimeInMinutes;
    }

    public void setRemainingTimeInMinutes(Integer remainingTimeInMinutes) {
        this.remainingTimeInMinutes = remainingTimeInMinutes;
    }
}
