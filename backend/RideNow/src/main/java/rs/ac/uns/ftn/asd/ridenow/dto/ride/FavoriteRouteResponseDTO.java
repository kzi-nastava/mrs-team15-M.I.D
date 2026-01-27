package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class FavoriteRouteResponseDTO {

    private Long routeId;
    private String startAddress;
    private String endAddress;
    private List<String> stopAddresses;

    public FavoriteRouteResponseDTO() { super(); }

}
