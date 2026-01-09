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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    private Passenger passenger;
    @Column(length = 300, nullable = false)
    private String description;

    public Inconsistency(Long id, Ride ride, Passenger passenger, String description) {
        this.id = id;
        this.ride = ride;
        this.passenger = passenger;
        this.description = description;
    }

    public Inconsistency() {

    }
}
