package rs.ac.uns.ftn.asd.ridenow.dto.vehicle;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.Location;

@Getter @Setter
public class VehicleResponseDTO {
    @NotNull @NotEmpty
    private String licencePlate;
    @NotNull
    private Location location;
    @NotNull
    private Boolean available;
}