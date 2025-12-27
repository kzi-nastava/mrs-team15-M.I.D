package rs.ac.uns.ftn.asd.ridenow.dto.vehicle;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class VehicleResponseDTO {
    @NotNull @NotEmpty
    private String licencePlate;
    @NotNull
    private Double lat;
    @NotNull
    private Double lon;
    @NotNull
    private Boolean available;


    public VehicleResponseDTO() {
    }

}
