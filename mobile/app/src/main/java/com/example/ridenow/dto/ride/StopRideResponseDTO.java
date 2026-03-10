package com.example.ridenow.dto.ride;

import com.example.ridenow.dto.model.LocationDTO;
import com.example.ridenow.dto.model.PolylinePointDTO;

import java.util.List;

public class StopRideResponseDTO {
    private int estimatedDurationMin;
    private double distanceKm;
    private double price;
    private String endAddress;
    private List<RoutePointDTO> route;
    private List<LocationDTO> passedStops;

    private double endLatitude;
    private double endLongitude;

    public List<LocationDTO> getPassedStops() {
        return passedStops;
    }

    public void setPassedStops(List<LocationDTO> passedStops) {
        this.passedStops = passedStops;
    }

    public List<RoutePointDTO> getRoute() {
        return route;
    }

    public void setRoute(List<RoutePointDTO> route) {
        this.route = route;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public int getEstimatedDurationMin() {
        return estimatedDurationMin;
    }

    public void setEstimatedDurationMin(int estimatedDurationMin) {
        this.estimatedDurationMin = estimatedDurationMin;
    }

    public double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(double endLongitude) {
        this.endLongitude = endLongitude;
    }
}
