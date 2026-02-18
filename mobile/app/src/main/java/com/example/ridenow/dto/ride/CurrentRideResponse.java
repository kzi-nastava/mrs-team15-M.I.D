package com.example.ridenow.dto.ride;

import com.example.ridenow.dto.model.RouteDTO;

public class CurrentRideResponse {
    private int estimatedDurationMin;
    private RouteDTO route;
    private Long rideId;
    private Boolean isMainPassenger;

    public int getEstimatedDurationMin() {
        return estimatedDurationMin;
    }

    public void setEstimatedDurationMin(int estimatedDurationMin) {
        this.estimatedDurationMin = estimatedDurationMin;
    }

    public RouteDTO getRoute() {
        return route;
    }

    public void setRoute(RouteDTO route) {
        this.route = route;
    }

    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public Boolean getMainPassenger() {
        return isMainPassenger;
    }

    public void setMainPassenger(Boolean mainPassenger) {
        isMainPassenger = mainPassenger;
    }
}
