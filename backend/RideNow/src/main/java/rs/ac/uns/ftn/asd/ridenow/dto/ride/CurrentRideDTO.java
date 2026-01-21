package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CurrentRideDTO {
    private int estimatedDurationMin;
    private double distanceKm;
    private List<RoutePointDTO> route;
    private String startAddress;
    private String endAddress;
    private Long rideId;

    public  CurrentRideDTO(){ super(); }
}
