package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RideEstimateResponseDTO {

    private int estimatedDurationMin;

    public RideEstimateResponseDTO(){
        super();
    }

}