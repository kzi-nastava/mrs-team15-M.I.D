package rs.ac.uns.ftn.asd.ridenow.model;

import java.util.List;

public class Route {

    private Long id;
    private Location startLocation;
    private Location endLocation;
    private List<Location> stopLocations;
    private double distanceKm;
    private double estimatedTimeMin;

    public Route(Long id, Location startLocation, Location endLocation, List<Location> stopLocations,
                 double distanceKm, double estimatedTimeMin) {
        this.id = id;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.stopLocations = stopLocations;
        this.distanceKm = distanceKm;
        this.estimatedTimeMin = estimatedTimeMin;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Location getStartLocation() { return startLocation; }

    public void setStartLocation(Location startLocation) { this.startLocation = startLocation; }

    public Location getEndLocation() { return endLocation; }

    public void setEndLocation(Location endLocation) { this.endLocation = endLocation; }

    public List<Location> getStopLocations() { return stopLocations; }

    public void setStopLocations(List<Location> stopLocations) { this.stopLocations = stopLocations; }

    public double getDistanceKm() { return distanceKm; }

    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public double getEstimatedTimeMin() { return estimatedTimeMin; }

    public void setEstimatedTimeMin(double estimatedTimeMin) { this.estimatedTimeMin = estimatedTimeMin; }

}
