package rs.ac.uns.ftn.asd.ridenow.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus;

import java.time.LocalDateTime;

@Getter @Setter
@Entity
@Table(name = "rides")
public class Ride {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private double price;
    private double distanceKm;
    @Column(nullable = false)
    private RideStatus status;
    @Column(nullable = false)
    private LocalDateTime scheduledTime;
    @Column(nullable = false)
    private LocalDateTime startTime;
    @Column(nullable = false)
    private LocalDateTime endTime;
    private String cancelReason;

    public Ride(Long id, String cancelReason, LocalDateTime endTime, LocalDateTime startTime, LocalDateTime scheduledTime,
                RideStatus status, double distanceKm, double price) {
        this.id = id;
        this.cancelReason = cancelReason;
        this.endTime = endTime;
        this.startTime = startTime;
        this.scheduledTime = scheduledTime;
        this.status = status;
        this.distanceKm = distanceKm;
        this.price = price;
    }

    public Ride() {}
}