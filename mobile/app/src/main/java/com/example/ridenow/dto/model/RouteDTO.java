package com.example.ridenow.dto.model;

import java.io.Serializable;
import java.util.List;

public class RouteDTO implements Serializable {
        private double distanceKm;
        private double estimatedTimeMin;
        private LocationDTO startLocation;
        private LocationDTO endLocation;
        private List<LocationDTO> stopLocations;
        private List<PolylinePointDTO> polylinePoints;

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public double getEstimatedTimeMin() { return estimatedTimeMin; }
    public void setEstimatedTimeMin(double estimatedTimeMin) { this.estimatedTimeMin = estimatedTimeMin; }

    public LocationDTO getStartLocation() { return startLocation; }
    public void setStartLocation(LocationDTO startLocation) { this.startLocation = startLocation; }

    public LocationDTO getEndLocation() { return endLocation; }
    public void setEndLocation(LocationDTO endLocation) { this.endLocation = endLocation; }

    public List<LocationDTO> getStopLocations() { return stopLocations; }
    public void setStopLocations(List<LocationDTO> stopLocations) { this.stopLocations = stopLocations; }

    public List<PolylinePointDTO> getPolylinePoints() { return polylinePoints; }
    public void setPolylinePoints(List<PolylinePointDTO> polylinePoints) { this.polylinePoints = polylinePoints; }

}
