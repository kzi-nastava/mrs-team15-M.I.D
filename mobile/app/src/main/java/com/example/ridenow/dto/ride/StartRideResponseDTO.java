package com.example.ridenow.dto.ride;

import java.io.Serializable;
import java.util.List;

public class StartRideResponseDTO implements Serializable {
    private Long id;
    private String startAddress;
    private String endAddress;
    private List<Double> stopLats;
    private List<Double> stopLngs;
    private List<String> passengers;
    private List<RoutePointDTO> route;
    private List<String> passengerImages;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public List<Double> getStopLats() {
        return stopLats;
    }

    public void setStopLats(List<Double> stopLats) {
        this.stopLats = stopLats;
    }

    public List<Double> getStopLngs() {
        return stopLngs;
    }

    public void setStopLngs(List<Double> stopLngs) {
        this.stopLngs = stopLngs;
    }

    public List<String> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<String> passengers) {
        this.passengers = passengers;
    }

    public List<RoutePointDTO> getRoute() {
        return route;
    }

    public void setRoute(List<RoutePointDTO> route) {
        this.route = route;
    }

    public List<String> getPassengerImages() {
        return passengerImages;
    }

    public void setPassengerImages(List<String> passengerImages) {
        this.passengerImages = passengerImages;
    }
}