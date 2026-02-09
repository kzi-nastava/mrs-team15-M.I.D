package com.example.ridenow.dto.driver;

import java.io.Serializable;
import java.util.List;

import com.example.ridenow.dto.model.RatingDTO;
import com.example.ridenow.dto.model.RouteDTO;

public class RideHistoryDTO implements Serializable {
        private RouteDTO route;
        private List<String> passengers;
        private String date;
        private String startTime;
        private String endTime;
        private Double durationMinutes;
        private Double cost;
        private Boolean cancelled;
        private String cancelledBy;
        private Boolean panic;
        private String panicBy;
        private RatingDTO rating;
        private List<String> inconsistencies;
    public RideHistoryDTO() {}

    // Getters and setters
    public RouteDTO getRoute() { return route; }
    public void setRoute(RouteDTO route) { this.route = route; }

    public List<String> getPassengers() { return passengers; }
    public void setPassengers(List<String> passengers) { this.passengers = passengers; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public double getDurationMinutes() {
        return durationMinutes != null ? durationMinutes : 0.0;
    }
    public void setDurationMinutes(double durationMinutes) { this.durationMinutes = durationMinutes; }

    public double getCost() {
        return cost != null ? cost : 0.0;
    }
    public void setCost(double cost) { this.cost = cost; }

    public boolean isCancelled() {
        return cancelled != null ? cancelled : false;
    }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    public String getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(String cancelledBy) { this.cancelledBy = cancelledBy; }

    public Boolean getPanic() { return panic; }
    public void setPanic(Boolean panic) { this.panic = panic; }

    public String getPanicBy() { return panicBy; }
    public void setPanicBy(String panicBy) { this.panicBy = panicBy; }

    public RatingDTO getRating() { return rating; }
    public void setRating(RatingDTO rating) { this.rating = rating; }

    public List<String> getInconsistencies() { return inconsistencies; }
    public void setInconsistencies(List<String> inconsistencies) { this.inconsistencies = inconsistencies; }

}
