package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InconsistencyRequestDTO {
    @NotNull
    private Long rideId;
    @NotNull @NotEmpty
    private String description;
}
