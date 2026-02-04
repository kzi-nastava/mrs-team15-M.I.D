package com.example.ridenow.dto.ride;

import com.example.ridenow.dto.model.Location;

public class TrackVehicleResponse {
    private Location location;
    private Integer remainingTimeInMinutes;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Integer getRemainingTimeInMinutes() {
        return remainingTimeInMinutes;
    }

    public void setRemainingTimeInMinutes(Integer remainingTimeInMinutes) {
        this.remainingTimeInMinutes = remainingTimeInMinutes;
    }
}
