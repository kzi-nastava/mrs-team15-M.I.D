package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "inconsistencies")
public class Inconsistency {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long rideId;
    @Column(nullable = false)
    private Long passengerId;
    @Column(length = 300, nullable = false)
    private String description;

    public Inconsistency(Long id, Long rideId, Long passengerId, String description) {
        this.id = id;
        this.rideId = rideId;
        this.passengerId = passengerId;
        this.description = description;
    }

    public Inconsistency() {

    }
}
