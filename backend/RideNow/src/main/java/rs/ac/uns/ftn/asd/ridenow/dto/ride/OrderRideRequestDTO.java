package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class OrderRideRequestDTO {

    @NotBlank
    private String startAddress;

    @NotNull
    private Double startLatitude;

    @NotNull
    private Double startLongitude;

    @NotBlank
    private String endAddress;

    @NotNull
    private Double endLatitude;

    @NotNull
    private Double endLongitude;

    private List<@NotBlank String> stopAddresses;

    private List<@NotNull Double> stopLatitudes;

    private List<@NotNull Double> stopLongitudes;

    @NotBlank
    private String vehicleType;

    private boolean babyFriendly;
    private boolean petFriendly;

    private List<@Email String> linkedPassengers;

    @FutureOrPresent
    private LocalDateTime scheduledTime;

    @NotNull
    private double distanceKm;

    @NotNull
    private int estimatedTimeMinutes;

    @NotNull
    private double priceEstimate;

    @NotEmpty
    private List<Double> routeLattitudes;

    @NotEmpty
    private List<Double> routeLongitudes;
}