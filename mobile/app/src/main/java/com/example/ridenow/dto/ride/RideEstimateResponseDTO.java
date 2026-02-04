package com.example.ridenow.dto.ride;

import java.util.List;

public class RideEstimateResponseDTO {
    private int estimatedDurationMin;
    private double distanceKm;
    private List<RoutePointDTO> route;

    public RideEstimateResponseDTO() {}

    public RideEstimateResponseDTO(int estimatedDurationMin, double distanceKm, List<RoutePointDTO> route) {
        this.estimatedDurationMin = estimatedDurationMin;
        this.distanceKm = distanceKm;
        this.route = route;
    }

    public int getEstimatedDurationMin() {
        return estimatedDurationMin;
    }

    public void setEstimatedDurationMin(int estimatedDurationMin) {
        this.estimatedDurationMin = estimatedDurationMin;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public List<RoutePointDTO> getRoute() {
        return route;
    }

    public void setRoute(List<RoutePointDTO> route) {
        this.route = route;
    }
}