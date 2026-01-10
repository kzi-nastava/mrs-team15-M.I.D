package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private  int driverRating;

    @Column(nullable = false)
    private  int vehicleRating;

    @Lob
    private String comment;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToOne(optional = false)
    @JoinColumn(name = "ride_id", nullable = false, unique = true)
    private Ride ride;

    public Rating(Ride ride, String comment, int vehicleRating, int driverRating) {
        this.comment = comment;
        this.vehicleRating = vehicleRating;
        this.driverRating = driverRating;
        this.assignRide(ride);
    }

    public Rating() {

    }

    public void setDriverRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Driver rating must be between 1 and 5");
        }
        this.driverRating = rating;
    }

    public void setVehicleRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Vehicle rating must be between 1 and 5");
        }
        this.vehicleRating = rating;
    }

    public void assignRide(Ride ride) {
        this.ride = ride;
        if (ride != null && ride.getRating() != this) {
            ride.setRating(this);
        }
    }
}