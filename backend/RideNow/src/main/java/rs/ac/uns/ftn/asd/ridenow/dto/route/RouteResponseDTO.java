package rs.ac.uns.ftn.asd.ridenow.dto.route;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class RouteResponseDTO {
    @Getter @Setter
    private Long id;

    @Getter @Setter
    private String startAddress;

    @Getter @Setter
    private String endAddress;

    @Getter @Setter
    private List<String> stopAddresses;

    @Getter @Setter
    private double totalDistanceKm;

    @Getter @Setter
    private int estimatedDurationMin;

    @Getter @Setter
    private boolean favorite;

    public RouteResponseDTO() { super(); }

}

