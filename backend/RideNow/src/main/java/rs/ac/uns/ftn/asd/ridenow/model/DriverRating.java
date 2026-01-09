package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "driver_ratings")
public class DriverRating {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

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

    public DriverRating(Long id, Driver driver, Passenger passenger, int rating, String comment, Ride ride) {
        this.id = id;
        this.driver = driver;
        this.passenger = passenger;
        this.rating = rating;
        this.comment = comment;
        this.ride = ride;
    }

    public DriverRating() {

    }
}
