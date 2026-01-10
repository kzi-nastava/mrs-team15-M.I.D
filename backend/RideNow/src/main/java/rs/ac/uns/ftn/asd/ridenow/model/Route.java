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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_location_id", nullable = false)
    private Location startLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_location_id", nullable = false)
    private Location endLocation;

    @ManyToMany(fetch = FetchType.LAZY)
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