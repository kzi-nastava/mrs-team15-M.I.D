package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class PanicAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime time;

    @Column(nullable = false)
    private boolean resolved = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false)
    Ride ride;

    public PanicAlert(Ride ride) {
        this.assignRide(ride);
        this.resolved = false;
    }

    public PanicAlert() {

    }

    public void markResolved(){ this.resolved = true; }

    public void markUnresolved(){
        this.resolved = false;
    }

    public void assignRide(Ride ride) {
        this.ride = ride;
        if(ride != null && !this.ride.getPanicAlerts().contains(this)){
            ride.addPanicAlert(this);
        }
    }
}