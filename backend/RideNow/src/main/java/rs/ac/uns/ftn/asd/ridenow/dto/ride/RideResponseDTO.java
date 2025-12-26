package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class RideResponseDTO {
    @Getter @Setter
    @NotNull
    private Long rideId;

    @Getter @Setter
    private List<@NotBlank @NotNull String> passengerEmails;

    @Getter @Setter
    @NotNull @NotNull
    private String startLocation;

    @Getter @Setter
    @NotNull
    private Double startLatitude;

    @Getter @Setter
    @NotNull
    private  Double startLongitude;

    @Getter @Setter
    @NotNull @NotNull
    private String endLocation;

    @Getter @Setter
    @NotNull
    private Double endLatitude;

    @Getter @Setter
    @NotNull
    private Double endLongitude;

    @Getter @Setter
    private List<@NotBlank @NotNull String> stopAddresses;

    @Getter @Setter
    private List<@NotNull Double> stopLatitudes;

    @Getter @Setter
    private List<@NotNull Double> stopLongitudes;

    @Getter @Setter
    @NotBlank @NotNull
    private String startTime;
}
