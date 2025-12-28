package rs.ac.uns.ftn.asd.ridenow.dto.vehicle;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateVehicleRequest {
    @NotNull
    private Double lat;
    @NotNull
    private Double lon;
}
