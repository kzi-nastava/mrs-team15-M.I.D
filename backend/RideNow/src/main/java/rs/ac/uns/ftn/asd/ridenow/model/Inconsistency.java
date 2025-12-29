package rs.ac.uns.ftn.asd.ridenow.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Inconsistency {
    private Long id;
    private Long rideId;
    private Long passengerId;
    private String description;

    public Inconsistency(Long id, Long rideId, Long passengerId, String description) {
        this.id = id;
        this.rideId = rideId;
        this.passengerId = passengerId;
        this.description = description;
    }
}
