package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InconsistencyRequestDTO {
    @NotNull (message = "Ride ID cannot be null")
    private Long rideId;
    @NotNull (message = "Description cannot be null") @NotEmpty (message = "Description cannot be empty")
    private String description;
}
