package rs.ac.uns.ftn.asd.ridenow.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
public class Ride {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double price;

    private double distanceKm;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RideStatus status;

    @Column(nullable = false)
    private LocalDateTime scheduledTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Lob
    private String cancelReason;

    private  boolean cancelled = false;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    Driver driver;

    @OneToOne(mappedBy = "ride", cascade = CascadeType.ALL, orphanRemoval = true)
    private Rating rating;

    @OneToMany(mappedBy = "ride", cascade = CascadeType.ALL, orphanRemoval = true)
    List<PanicAlert> panicAlerts = new ArrayList<>();

    @OneToMany(mappedBy = "ride", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Passenger> passengers = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @OneToMany(mappedBy = "ride", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inconsistency> inconsistencies = new ArrayList<>();

    public Ride(String cancelReason, LocalDateTime endTime, LocalDateTime startTime, LocalDateTime scheduledTime,
                RideStatus status, double distanceKm, double price) {
        this.cancelReason = cancelReason;
        this.endTime = endTime;
        this.startTime = startTime;
        this.scheduledTime = scheduledTime;
        this.status = status;
        this.distanceKm = distanceKm;
        this.price = price;
    }

    public Ride(RideStatus status, LocalDateTime scheduledTime) {
        this.status = status;
        this.scheduledTime = scheduledTime;
    }

    public Ride() {

    }

    public void setRating(Rating rating){
        if(rating != null){
            this.rating = rating;
            rating.assignRide(this);
        }
    }

    public void addPanicAlert(PanicAlert panicAlert) {
        if(panicAlert != null && !panicAlerts.contains(panicAlert)){
            panicAlerts.add(panicAlert);
            panicAlert.assignRide(this);
        }
    }

    public void addPassenger(Passenger passenger) {
        if(passenger != null && !passengers.contains(passenger)){
            passengers.add(passenger);
            passenger.assignRide(this);
        }
    }

    public void addInconsistency(Inconsistency inconsistency){
        if(inconsistency != null && !inconsistencies.contains(inconsistency)){
            inconsistencies.add(inconsistency);
            inconsistency.setRide(this);
        }
    }
}