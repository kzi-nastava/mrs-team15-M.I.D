package rs.ac.uns.ftn.asd.ridenow.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
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
}
