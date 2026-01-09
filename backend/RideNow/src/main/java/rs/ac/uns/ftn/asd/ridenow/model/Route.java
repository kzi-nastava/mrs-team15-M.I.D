package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@Entity
@Table(name = "routes")
public class Route {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "start_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "start_longitude")),
            @AttributeOverride(name = "address", column = @Column(name = "start_address"))
    })
    private Location startLocation;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "end_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "end_longitude")),
            @AttributeOverride(name = "address", column = @Column(name = "end_address"))
    })
    private Location endLocation;
    @ElementCollection
    @CollectionTable(name = "route_stop_locations", joinColumns = @JoinColumn(name = "route_id"))
    private List<Location> stopLocations;
    private double distanceKm;
    private double estimatedTimeMin;

    public Route() {}

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
