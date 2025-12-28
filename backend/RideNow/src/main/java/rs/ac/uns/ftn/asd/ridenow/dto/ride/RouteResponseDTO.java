package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class RouteResponseDTO {

    private Long routeId;
    private String startAddress;
    private Double startLatitude;
    private Double startLongitude;
    private String endAddress;
    private Double endLatitude;
    private Double endLongitude;
    private List<String> stopAddresses;
    private List<Double> stopLatitudes;
    private List<Double> stopLongitudes;

    private double distanceKm;
    private int estimatedTimeMinutes;
    private double priceEstimate;

    public RouteResponseDTO() { super(); }

}
