package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class StartRideResponseDTO {
    private Long id;
    private String startAddress;
    private String endAddress;
    private List<String> passengers;
    private List<RoutePointDTO> route;
    private List<String> passengerImages;

    public StartRideResponseDTO() {
        super();
    }
}
