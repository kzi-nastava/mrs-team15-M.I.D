package rs.ac.uns.ftn.asd.ridenow.dto.user;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RateRequestDTO {
    @NotNull
    @Max(5)
    @Min(1)
    private Integer driverRating;
    @NotNull
    @Max(5)
    @Min(1)
    private Integer vehicleRating;
    private String driverComment;
    private String vehicleComment;
}
