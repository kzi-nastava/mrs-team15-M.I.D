package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.Location;

@Getter @Setter
public class TrackVehicleDTO {
    @NotNull
    private Location location;
    @NotNull
    private Integer remainingTimeInMinutes;

    public TrackVehicleDTO() {
        super();
    }
}
