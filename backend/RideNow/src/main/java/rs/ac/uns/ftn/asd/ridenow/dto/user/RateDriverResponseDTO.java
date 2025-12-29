package rs.ac.uns.ftn.asd.ridenow.dto.user;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RateDriverResponseDTO {
    @NotNull
    @DecimalMax("5.0")
    @DecimalMin("1.0")
    private Double rating;
    @NotNull
    private Long rideId;
    private String comment;
    @NotNull
    private Long passengerId;
    @NotNull
    private Long driverId;
}
