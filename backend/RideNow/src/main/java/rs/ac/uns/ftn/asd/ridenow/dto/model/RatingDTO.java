package rs.ac.uns.ftn.asd.ridenow.dto.model;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.Rating;

@Getter
@Setter
public class RatingDTO {
    private Integer driverRating;
    private Integer vehicleRating;
    private String driverComment;
    private String vehicleComment;

    public RatingDTO(Rating rating) {
        this.driverRating = rating.getDriverRating();
        this.vehicleRating = rating.getVehicleRating();
        this.driverComment = rating.getDriverComment();
        this.vehicleComment = rating.getVehicleComment();
    }
}
