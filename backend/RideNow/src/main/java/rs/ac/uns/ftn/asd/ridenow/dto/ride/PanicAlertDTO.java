package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PanicAlertDTO {
    private Long id;
    private Long rideId;
    private String panicBy;
    private String panicByRole;
    private String createdAt;
    private boolean resolved;
    private String resolvedAt;
    private Long resolvedBy;
    private String resolvedByEmail;
    private RoutePointDTO location;

    private String driverEmail;
    private String passengerEmail;

    public PanicAlertDTO() {}
}