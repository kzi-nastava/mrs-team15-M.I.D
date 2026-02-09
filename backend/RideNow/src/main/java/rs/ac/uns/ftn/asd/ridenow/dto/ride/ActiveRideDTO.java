package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RouteDTO;

import java.time.LocalDateTime;

@Getter @Setter
public class ActiveRideDTO {
    private Long rideId;
    private LocalDateTime startTime;
    private String driverName;
    private String passengerNames;
    private Boolean panic;
    private String panicBy;
    private RouteDTO route;
}
