package rs.ac.uns.ftn.asd.ridenow.model;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus;

import java.time.LocalDateTime;

@Getter @Setter
public class Ride {
    private Long id;
    private double price;
    private double distanceKm;
    private RideStatus status;
    private LocalDateTime scheduledTime;
    private LocalDateTime startTime;
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
}