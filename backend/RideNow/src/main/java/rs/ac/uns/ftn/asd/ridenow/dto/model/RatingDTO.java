package rs.ac.uns.ftn.asd.ridenow.dto.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingDTO {
    private Integer driverRating;
    private Integer vehicleRating;
    private String driverComment;
    private String vehicleComment;

    public RatingDTO(Integer driverRating, Integer vehicleRating, String driverComment, String vehicleComment) {
        this.driverRating = driverRating;
        this.vehicleRating = vehicleRating;
        this.driverComment = driverComment;
        this.vehicleComment = vehicleComment;
    }
}
