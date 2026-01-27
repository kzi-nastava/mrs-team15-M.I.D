package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RouteDTO;

import java.util.List;

@Getter
@Setter
public class CurrentRideDTO {
    private int estimatedDurationMin;
    private RouteDTO route;
    private Long rideId;

    public  CurrentRideDTO(){ super(); }
}
