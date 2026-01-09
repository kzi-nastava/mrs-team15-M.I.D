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
    @Column(nullable = false)
    private Long driverId;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private int rating;
    @Column(length = 500)
    private String comment;
    @Column(nullable = false)
    private Long rideId;

    public DriverRating(Long id, Long driverId, Long userId, int rating, String comment, Long rideId) {
        this.id = id;
        this.driverId = driverId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.rideId = rideId;
    }

    public DriverRating() {

    }
}
