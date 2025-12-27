package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TrackVehicleDTO {
    @NotNull
    private Double longitude;
    @NotNull
    private Integer remainingTimeInMinutes;

    public TrackVehicleDTO() {
        super();
    }
}
