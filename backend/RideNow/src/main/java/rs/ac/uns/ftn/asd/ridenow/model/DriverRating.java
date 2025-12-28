package rs.ac.uns.ftn.asd.ridenow.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DriverRating {
    private Long id;
    private Long driverId;
    private Long userId;
    private int rating;
    private String comment;
    private Long rideId;

    public DriverRating(Long id, Long driverId, Long userId, int rating, String comment, Long rideId) {
        this.id = id;
        this.driverId = driverId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.rideId = rideId;
    }
}
