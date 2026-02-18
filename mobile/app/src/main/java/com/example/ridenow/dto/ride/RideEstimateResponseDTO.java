package com.example.ridenow.dto.ride;

import java.util.List;

public class RideEstimateResponseDTO {
    private Integer estimatedDurationMin;
    private Integer estimatedTimeMinutes;
    private Integer estimatedTime;
    private double distanceKm;
    private List<RoutePointDTO> route;

    public RideEstimateResponseDTO() {}

    public int getEstimatedDurationMin() {
        if (estimatedDurationMin != null && estimatedDurationMin != 0) return estimatedDurationMin;
        if (estimatedTimeMinutes != null && estimatedTimeMinutes != 0) return estimatedTimeMinutes;
        if (estimatedTime != null && estimatedTime != 0) return estimatedTime;
        return 0;
    }

    public void setEstimatedDurationMin(int estimatedDurationMin) {
        this.estimatedDurationMin = estimatedDurationMin;
    }

    public Integer getEstimatedTimeMinutes() { return estimatedTimeMinutes; }
    public void setEstimatedTimeMinutes(Integer estimatedTimeMinutes) { this.estimatedTimeMinutes = estimatedTimeMinutes; }

    public Integer getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(Integer estimatedTime) { this.estimatedTime = estimatedTime; }

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