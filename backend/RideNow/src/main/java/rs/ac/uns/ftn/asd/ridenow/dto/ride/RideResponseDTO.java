package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.Route;

import java.util.List;

@Getter @Setter
public class RideResponseDTO {
    @NotNull
    private Long rideId;

    private List<@NotBlank @NotNull String> passengerEmails;

    @NotNull @NotNull
    private Route route;

    @NotBlank @NotNull
    private String startTime;
}
