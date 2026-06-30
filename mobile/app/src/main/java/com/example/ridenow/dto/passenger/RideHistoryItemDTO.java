package com.example.ridenow.dto.passenger;

import com.example.ridenow.dto.model.RatingDTO;
import com.example.ridenow.dto.model.RouteDTO;

import java.util.List;

public class RideHistoryItemDTO {
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
    private boolean favoriteRoute;
    private List<String> passengers;
    private RatingDTO rating;
    private List<String> inconsistencies;
    private String driver;


    public Long getRideId() {
        return rideId;
    }

    public void setRideId(Long rideId) {
        this.rideId = rideId;
    }

    public RouteDTO getRoute() {
        return route;
    }

    public void setRoute(RouteDTO route) {
        this.route = route;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
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

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public boolean isFavoriteRoute() {
        return favoriteRoute;
    }

    public void setFavoriteRoute(boolean favoriteRoute) {
        this.favoriteRoute = favoriteRoute;
    }

    public List<String> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<String> passengers) {
        this.passengers = passengers;
    }

    public RatingDTO getRating() {
        return rating;
    }

    public void setRating(RatingDTO rating) {
        this.rating = rating;
    }

    public List<String> getInconsistencies() {
        return inconsistencies;
    }

    public void setInconsistencies(List<String> inconsistencies) {
        this.inconsistencies = inconsistencies;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }
}
