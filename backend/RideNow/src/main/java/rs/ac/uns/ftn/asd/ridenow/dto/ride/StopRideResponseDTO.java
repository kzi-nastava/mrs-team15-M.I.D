package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StopRideResponseDTO {
    private int estimatedDurationMin;
    private double distanceKm;
    private double price;
    private String endAddress;
    private List<RoutePointDTO> route;
}
