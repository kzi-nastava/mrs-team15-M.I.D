package rs.ac.uns.ftn.asd.ridenow.dto.model;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.Location;
import rs.ac.uns.ftn.asd.ridenow.model.PolylinePoint;
import rs.ac.uns.ftn.asd.ridenow.model.Route;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class RouteDTO {
    private double distanceKm;
    private double estimatedTimeMin;
    private Location startLocation;
    private Location endLocation;
    private List<Location> stopLocations;
    private List<PolylinePoint> polylinePoints;

    public RouteDTO(Route route) {
        this.distanceKm = route.getDistanceKm();
        this.estimatedTimeMin = route.getEstimatedTimeMin();
        this.startLocation = route.getStartLocation();
        this.endLocation = route.getEndLocation();
        this.stopLocations = route.getStopLocations();
        this.polylinePoints = route.getPolylinePoints();
    }
}
