package rs.ac.uns.ftn.asd.ridenow.dto.user;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RateRequestDTO {
    @NotNull
    @Max(value = 5, message = "Rating must be between 1 and 5")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    private Integer driverRating;
    @NotNull
    @Max(value = 5, message = "Rating must be between 1 and 5")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    private Integer vehicleRating;
    private String driverComment;
    private String vehicleComment;
}
