package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RouteDTO;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class RideResponseDTO {
    private Long rideId;

    private List<String> passengerEmails;

    private RouteDTO route;

    private LocalDateTime startTime;
}
