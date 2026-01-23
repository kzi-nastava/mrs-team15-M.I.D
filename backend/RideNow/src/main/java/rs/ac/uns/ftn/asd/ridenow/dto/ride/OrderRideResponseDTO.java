package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class OrderRideResponseDTO {

    private Long id;
    private String mainPassengerEmail;
    private String startAddress;
    private String endAddress;
    private List<String> stopAddresses;
    private VehicleType vehicleType;
    private boolean babyFriendly;
    private boolean petFriendly;
    private List<String> linkedPassengers;
    private LocalDateTime scheduledTime;
    private RideStatus status;
    private double distanceKm;
    private int estimatedTimeMinutes;
    private double priceEstimate;
    private Long driverId;

    public OrderRideResponseDTO() { super(); }

}
