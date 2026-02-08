package rs.ac.uns.ftn.asd.ridenow.dto.model;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

@Getter @Setter
public class PriceConfigDTO {
    private VehicleType vehicleType;
    private double basePrice;
    private double pricePerKm;
}
