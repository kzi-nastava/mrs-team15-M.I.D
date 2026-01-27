package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Min(0)
    private double distanceKm;

    @Column(nullable = false)
    @Min(0)
    private double estimatedTimeMin;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "start_latitude", nullable = false)),
            @AttributeOverride(name = "longitude", column = @Column(name = "start_longitude", nullable = false)),
            @AttributeOverride(name = "address", column = @Column(name = "start_address"))
    })
    private Location startLocation;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "end_latitude", nullable = false)),
            @AttributeOverride(name = "longitude", column = @Column(name = "end_longitude", nullable = false)),
            @AttributeOverride(name = "address", column = @Column(name = "end_address"))
    })
    private Location endLocation;

    @ElementCollection
    @CollectionTable(name = "route_stop_locations",
            joinColumns = @JoinColumn(name = "route_id"))
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "stop_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "stop_longitude")),
            @AttributeOverride(name = "address", column = @Column(name = "stop_address"))
    })
    private List<Location> stopLocations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "route_polyline_points",
            joinColumns = @JoinColumn(name = "route_id"))
    @OrderColumn(name = "point_order")
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "point_latitude", nullable = false)),
            @AttributeOverride(name = "longitude", column = @Column(name = "point_longitude", nullable = false))
    })
    private List<PolylinePoint> polylinePoints = new ArrayList<>();

    public Route(double distanceKm, double estimatedTimeMin, Location startLocation, Location endLocation) {
        this.distanceKm = distanceKm;
        this.estimatedTimeMin = estimatedTimeMin;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    public Route() {
    }

    public void addStopLocation(Location location) {
        // todo : change condition if it's allowed to have same stop location multiple time in route
        if (location != null && !stopLocations.contains(location)) {
            stopLocations.add(location);
        }
    }

    public void removeStopLocation(Location location) {
        if (location != null && !stopLocations.contains(location)) {
            stopLocations.remove(location);
        }
    }
}