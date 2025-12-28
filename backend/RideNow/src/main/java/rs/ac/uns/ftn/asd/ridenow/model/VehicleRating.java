package rs.ac.uns.ftn.asd.ridenow.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class VehicleRating {
    private Long id;
    private Long vehicleId;
    private Long userId;
    private int rating;
    private String comment;
    private Long rideId;

    public VehicleRating(Long id, Long vehicleId, Long userId, int rating, String comment, Long rideId) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.rideId = rideId;
    }
}
