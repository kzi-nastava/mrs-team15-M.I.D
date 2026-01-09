package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    public Route(double distanceKm, double estimatedTimeMin) {
        this.distanceKm = distanceKm;
        this.estimatedTimeMin = estimatedTimeMin;
    }

    public Route() {
    }
}