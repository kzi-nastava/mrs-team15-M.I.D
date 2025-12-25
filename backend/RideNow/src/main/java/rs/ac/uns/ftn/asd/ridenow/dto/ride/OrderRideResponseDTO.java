package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus;

import java.time.LocalDateTime;
import java.util.List;

public class OrderRideResponseDTO {

    private Long id;
    private Long routeId;
    private String vehicleType;
    private boolean babyFriendly;
    private boolean petFriendly;
    private List<String> linkedPassengers;
    private LocalDateTime scheduledTime;
    private RideStatus status;

    public OrderRideResponseDTO() { super(); }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Long getRouteId() { return routeId; }

    public void setRouteId(Long routeId) { this.routeId = routeId; }

    public String getVehicleType() { return vehicleType; }

    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public boolean isBabyFriendly() { return babyFriendly; }

    public void setBabyFriendly(boolean babyFriendly) { this.babyFriendly = babyFriendly; }

    public boolean isPetFriendly() { return petFriendly; }

    public void setPetFriendly(boolean petFriendly) { this.petFriendly = petFriendly; }

    public List<String> getLinkedPassengers() { return linkedPassengers; }

    public void setLinkedPassengers(List<String> linkedPassengers) { this.linkedPassengers = linkedPassengers; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }

    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public  RideStatus getStatus() { return status; }

    public void setStatus(RideStatus status) { this.status = status; }
}
