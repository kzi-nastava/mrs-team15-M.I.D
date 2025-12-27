package rs.ac.uns.ftn.asd.ridenow.dto.vehicle;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

@Getter @Setter
public class VehicleResponseDTO {
    private String licencePlate;
    private double lat;
    private double lon;
    private boolean available;
    private double rating;
    private boolean petFriendly;
    private boolean childFriendly;
    private VehicleType type;


    public VehicleResponseDTO() {
    }

}
