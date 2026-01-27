package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoutePointDTO {
    private double lat;
    private double lng;

    public RoutePointDTO(){ super();}
}
