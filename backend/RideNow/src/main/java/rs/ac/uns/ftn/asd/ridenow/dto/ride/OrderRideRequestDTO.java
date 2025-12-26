package rs.ac.uns.ftn.asd.ridenow.dto.ride;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

public class OrderRideRequestDTO {

    @NotNull
    private Long routeId;

    @NotBlank
    private String vehicleType;

    private boolean babyFriendly;
    private boolean petFriendly;

    private List<@Email String> linkedPassengers;

    @FutureOrPresent
    private LocalDateTime scheduledTime;


    public String getVehicleType() { return vehicleType; }

    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public boolean isBabyFriendly() { return babyFriendly; }

    public void setBabyFriendly(boolean babyFriendly) { this.babyFriendly = babyFriendly; }

    public boolean isPetFriendly() { return petFriendly; }

    public void setPetFriendly(boolean petFriendly) { this.petFriendly = petFriendly; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }

    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public Long getRouteId() { return routeId; }

    public void setRouteId(Long routeId) { this.routeId = routeId; }

    public List<String> getLinkedPassengers() { return linkedPassengers; }

    public void setLinkedPassengers(List<String> linkedPassengers) { this.linkedPassengers = linkedPassengers; }

}
