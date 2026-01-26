package rs.ac.uns.ftn.asd.ridenow.dto.driver;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DriverLocationRequestDTO {
    @NotNull(message = "Latitude cannot be null")
    private double lat;
    @NotNull(message = "Longitude cannot be null")
    private double lon;
}
