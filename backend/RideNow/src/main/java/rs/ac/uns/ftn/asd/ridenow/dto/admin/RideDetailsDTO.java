package rs.ac.uns.ftn.asd.ridenow.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RideDetailsDTO {
    private Long rideId;
    private String route;
    private String driver;
    private String passenger;
    private double price;
    private boolean panicTriggered;
    private String inconsistencies;
    private double rating;

    public RideDetailsDTO() {
        super();
    }

}
