package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.PassengerRole;

import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private RegisteredUser user;

    @OneToMany(mappedBy = "passenger", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inconsistency> inconsistencies = new ArrayList<>();

    public Passenger(Ride ride, RegisteredUser user, PassengerRole role) {
        this.assignRide(ride);
        this.assignUser(user);
        this.role = role;
    }

    public Passenger() {

    }

    public void assignUser(RegisteredUser user) {
        if(user != null && !user.getRideParticipation().contains(this)){
            user.addParticipation(this);
        }
    }

    public void assignRide(Ride ride) {
        if(ride != null && !ride.getPassengers().contains(this)){
            ride.addPassenger(this);
        }
    }

    public void addInconsistency(Inconsistency inconsistency){
        if(inconsistency != null && !inconsistencies.contains(inconsistency)){
            inconsistencies.add(inconsistency);
            inconsistency.setPassenger(this);
        }
    }
}