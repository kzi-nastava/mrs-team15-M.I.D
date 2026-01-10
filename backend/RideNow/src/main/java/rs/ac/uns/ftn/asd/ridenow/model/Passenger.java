package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.PassengerRole;

@Setter
@Getter
@Entity
public class Passenger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PassengerRole role;

    @ManyToOne
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private RegisteredUser user;

    public Passenger(Ride ride, RegisteredUser user, PassengerRole role) {
        this.ride = ride;
        this.user = user;
        this.role = role;
    }

    public Passenger() {

    }
}