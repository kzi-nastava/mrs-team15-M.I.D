package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InconsistencyResponseDTO {
    @NotNull
    private Long rideId;
    @NotNull @NotEmpty
    private String description;
    @NotNull
    private Long passengerId;
    @NotNull
    private Long driverId;
}
