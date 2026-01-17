package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RouteDTO;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class RideResponseDTO {
    @NotNull
    private Long rideId;

    private List<@NotBlank @NotNull String> passengerEmails;

    @NotNull @NotNull
    private RouteDTO route;

    @NotBlank @NotNull
    private LocalDateTime startTime;
}
