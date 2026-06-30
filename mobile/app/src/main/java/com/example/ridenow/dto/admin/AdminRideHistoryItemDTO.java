package com.example.ridenow.dto.admin;

import com.example.ridenow.dto.model.RatingDTO;
import com.example.ridenow.dto.model.RouteDTO;

import java.util.List;

public class AdminRideHistoryItemDTO {
    private Long rideId;
    private RouteDTO route;
    private String startTime;
    private String endTime;
    private boolean cancelled;
    private String cancelledBy;
    private Double price;
    private Boolean panic;
    private String panicBy;
    private Long routeId;
    private List<String> passengers;
    private RatingDTO rating;
    private List<String> inconsistencies;
    private String driver;

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public List<String> getInconsistencies() {
        return inconsistencies;
    }

    public void setInconsistencies(List<String> inconsistencies) {
        this.inconsistencies = inconsistencies;
    }

    public RatingDTO getRating() {
        return rating;
    }

    public void setRating(RatingDTO rating) {
        this.rating = rating;
    }

    public List<String> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<String> passengers) {
        this.passengers = passengers;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public String getPanicBy() {
        return panicBy;
    }

    public void setPanicBy(String panicBy) {
        this.panicBy = panicBy;
    }

    public Boolean getPanic() {
        return panic;
    }

    public void setPanic(Boolean panic) {
        this.panic = panic;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
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
}