package rs.ac.uns.ftn.asd.ridenow.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Driver extends User {
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DriverStatus status;

    @Column(nullable = false)
    private Boolean available;

    @Column(nullable = false)
    @Min(0)
    private Double workingHoursLast24 = 0.0;

    @Min(0)
    private Double rating = 0.0;

    @OneToOne(mappedBy = "driver", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Vehicle vehicle;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<Ride> rideHistory = new ArrayList<>();

    public Driver(String email, String password, String firstName, String lastName, String phoneNumber, String address,
                  String profileImage, boolean active, boolean blocked, DriverStatus status, boolean available,
                  double workingHoursLast24, double rating, Vehicle vehicle) {
        super(email, password, firstName, lastName, phoneNumber, address, profileImage, active, blocked, UserRoles.DRIVER);
        this.status = status;
        this.available = available;
        this.workingHoursLast24 = workingHoursLast24;
        this.rating = rating;
        this.assignVehicle(vehicle);
    }

    public Driver(String email, String password, String firstName, String lastName, String phoneNumber, String address,
                  DriverStatus status, Vehicle vehicle) {
        super(email, password, firstName, lastName, phoneNumber, address, null, true, false, UserRoles.DRIVER);
        this.status = status;
        this.rideHistory = new ArrayList<>();
        this.assignVehicle(vehicle);
        this.rating = 0.0;
        this.workingHoursLast24 = 0.0;
        this.available = true;
    }

    public Driver() {
        super();
    }

    public void addRide(Ride ride){
        if (!rideHistory.contains(ride)) {
            rideHistory.add(ride);
            ride.setDriver(this);
        }
    }

    public void removeRide(Ride ride){
        if (rideHistory.contains(ride)) {
            rideHistory.remove(ride);
            ride.setDriver(null);
        }
    }

    public void assignVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        if (vehicle.getDriver() != this) {
            vehicle.setDriver(this);
        }
    }
}