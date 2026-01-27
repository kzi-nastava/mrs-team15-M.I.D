package rs.ac.uns.ftn.asd.ridenow.dto.user;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.Rating;

import java.time.LocalDateTime;

@Getter @Setter
public class RateResponseDTO {
    private Long id;
    private Integer driverRating;
    private Integer vehicleRating;
    private String driverComment;
    private String vehicleComment;
    private LocalDateTime createdAt;
    private Long rideId;

    public RateResponseDTO(Rating rating) {
        this.id = rating.getId();
        this.driverRating = rating.getDriverRating();
        this.vehicleRating = rating.getVehicleRating();
        this.driverComment = rating.getDriverComment();
        this.vehicleComment = rating.getVehicleComment();
        this.createdAt = rating.getCreatedAt();
        this.rideId = rating.getRide().getId();
    }
}
