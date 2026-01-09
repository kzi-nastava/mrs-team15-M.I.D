package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "vehicle_ratings")
public class VehicleRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    private Passenger passenger;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;
    @Column(nullable = false)
    private int rating;
    @Column(length = 500)
    private String comment;

    public VehicleRating() {}

    public VehicleRating(Long id, Vehicle vehicle, Passenger passenger, int rating, String comment, Ride ride) {
        this.id = id;
        this.vehicle = vehicle;
        this.passenger = passenger;
        this.rating = rating;
        this.comment = comment;
        this.ride = ride;
    }
}
