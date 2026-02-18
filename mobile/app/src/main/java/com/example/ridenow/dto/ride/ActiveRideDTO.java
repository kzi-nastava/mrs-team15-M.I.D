package com.example.ridenow.dto.ride;

import com.example.ridenow.dto.model.RouteDTO;

import java.time.LocalDateTime;

public class ActiveRideDTO {
    private Long rideId;
    private String startTime;
    private String driverName;
    private String passengerNames;
    private Boolean panic;
    private String panicBy;
    private RouteDTO route;

    // Getters and setters
    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getPassengerNames() {
        return passengerNames;
    }

    public void setPassengerNames(String passengerNames) {
        this.passengerNames = passengerNames;
    }

    public Boolean getPanic() {
        return panic;
    }

    public void setPanic(Boolean panic) {
        this.panic = panic;
    }

    public String getPanicBy() {
        return panicBy;
    }

    public void setPanicBy(String panicBy) {
        this.panicBy = panicBy;
    }

    public RouteDTO getRoute() {
        return route;
    }

    public void setRoute(RouteDTO route) {
        this.route = route;
    }
}
