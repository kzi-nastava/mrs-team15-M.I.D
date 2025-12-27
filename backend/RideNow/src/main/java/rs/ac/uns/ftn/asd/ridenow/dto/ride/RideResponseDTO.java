package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class RideResponseDTO {
    @NotNull
    private Long rideId;

    private List<@NotBlank @NotNull String> passengerEmails;

    @NotNull @NotNull
    private String startLocation;

    @NotNull
    private Double startLatitude;

    @NotNull
    private  Double startLongitude;

    @NotNull @NotNull
    private String endLocation;

    @NotNull
    private Double endLatitude;

    @NotNull
    private Double endLongitude;

    private List<@NotBlank @NotNull String> stopAddresses;

    private List<@NotNull Double> stopLatitudes;

    private List<@NotNull Double> stopLongitudes;

    @NotBlank @NotNull
    private String startTime;
}
