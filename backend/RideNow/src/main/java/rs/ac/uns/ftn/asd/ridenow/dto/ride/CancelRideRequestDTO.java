package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CancelRideRequestDTO {
    private String reason;

    public CancelRideRequestDTO() {
        super();
    }

}
