package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpcomingRideDTO {
    private Long id;
    private String route;
    private String startTime;
    private String passengers;
    private boolean canCancel;
}
