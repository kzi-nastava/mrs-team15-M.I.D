package rs.ac.uns.ftn.asd.ridenow.dto.model;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.Location;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class RouteDTO {
    private double distanceKm;
    private double estimatedTimeMin;
    private Location startLocation;
    private Location endLocation;
    private List<Location> stopLocations;

    public RouteDTO(double distanceKm, double estimatedTimeMin, Location startLocation, Location endLocation, List<Location> stopLocations) {
        this.distanceKm = distanceKm;
        this.estimatedTimeMin = estimatedTimeMin;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.stopLocations = stopLocations;
    }
}
