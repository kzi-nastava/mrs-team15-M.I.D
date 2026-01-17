package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.Inconsistency;

@Getter @Setter
public class InconsistencyResponseDTO {
    private Long id;
    private Long rideId;
    private String description;

    public InconsistencyResponseDTO(Inconsistency inconsistency) {
        this.id = inconsistency.getId();
        this.rideId = inconsistency.getRide().getId();
        this.description = inconsistency.getDescription();
    }
}
