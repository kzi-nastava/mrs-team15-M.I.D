package com.example.ridenow.dto.ride;

import com.example.ridenow.dto.model.Route;

public class CurrentRideResponse {
    private int estimatedDurationMin;
    private Route route;
    private Long rideId;

    public int getEstimatedDurationMin() {
        return estimatedDurationMin;
    }

    public void setEstimatedDurationMin(int estimatedDurationMin) {
        this.estimatedDurationMin = estimatedDurationMin;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }
}
