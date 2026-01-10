package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
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
    private double distanceKm;

    @Column(nullable = false)
    private double estimatedTimeMin;

    @ManyToOne
    @JoinColumn(name = "start_location_id", nullable = false)
    private Location startLocation;

    @ManyToOne
    @JoinColumn(name = "end_location_id", nullable = false)
    private Location endLocation;

    @ManyToMany
    @JoinTable(name = "route_stop_locations",
               joinColumns = @JoinColumn(name = "route_id"),inverseJoinColumns = @JoinColumn(name = "location_id"))
    private List<Location> stopLocations = new ArrayList<>();

    public Route(double distanceKm, double estimatedTimeMin, Location startLocation, Location endLocation) {
        this.distanceKm = distanceKm;
        this.estimatedTimeMin = estimatedTimeMin;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    public Route() {
    }
}
