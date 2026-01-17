package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class RideEstimateResponseDTO {

    private int estimatedDurationMin;
    private double distanceKm;
    private List<RoutePointDTO> route;

    public RideEstimateResponseDTO(){
        super();
    }

}