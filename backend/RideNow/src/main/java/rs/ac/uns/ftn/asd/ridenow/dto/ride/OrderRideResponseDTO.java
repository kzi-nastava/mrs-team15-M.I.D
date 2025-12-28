package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class OrderRideResponseDTO {

    private Long id;
    private Long routeId;
    private String vehicleType;
    private boolean babyFriendly;
    private boolean petFriendly;
    private List<String> linkedPassengers;
    private LocalDateTime scheduledTime;
    private RideStatus status;

    public OrderRideResponseDTO() { super(); }

}
