package rs.ac.uns.ftn.asd.ridenow.dto.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

@Getter @Setter
public class PriceConfigDTO {
    @NotNull
    private VehicleType vehicleType;
    @NotNull
    private double basePrice;
    @NotNull
    private double pricePerKm;
}
