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
    @Column(nullable = false)
    private Long vehicleId;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private int rating;
    @Column(length = 500)
    private String comment;
    @Column(nullable = false)
    private Long rideId;

    public VehicleRating() {}

    public VehicleRating(Long id, Long vehicleId, Long userId, int rating, String comment, Long rideId) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.rideId = rideId;
    }
}
