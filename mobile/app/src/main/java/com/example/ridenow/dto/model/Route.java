package com.example.ridenow.dto.model;

import java.io.Serializable;
import java.util.List;

public class Route implements Serializable {
        private double distanceKm;
        private double estimatedTimeMin;
        private Location startLocation;
        private Location endLocation;
        private List<Location> stopLocations;

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public double getEstimatedTimeMin() { return estimatedTimeMin; }
    public void setEstimatedTimeMin(double estimatedTimeMin) { this.estimatedTimeMin = estimatedTimeMin; }

    public Location getStartLocation() { return startLocation; }
    public void setStartLocation(Location startLocation) { this.startLocation = startLocation; }

    public Location getEndLocation() { return endLocation; }
    public void setEndLocation(Location endLocation) { this.endLocation = endLocation; }

    public List<Location> getStopLocations() { return stopLocations; }
    public void setStopLocations(List<Location> stopLocations) { this.stopLocations = stopLocations; }

}
