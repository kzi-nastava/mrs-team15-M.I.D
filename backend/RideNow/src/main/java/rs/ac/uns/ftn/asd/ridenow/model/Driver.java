package rs.ac.uns.ftn.asd.ridenow.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;

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
    private boolean available;

    @Column(nullable = false)
    private double workingHoursLast24 = 0;

    private double rating = 0.0;

    @OneToOne(mappedBy = "driver", cascade = CascadeType.ALL)
    private Vehicle vehicle;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Ride> rideHistory = new ArrayList<>();

    public Driver(String email, String password, String firstName, String lastName, String phoneNumber, String address,
                  String profileImage, boolean active, boolean blocked, DriverStatus status, boolean available,
                  double workingHoursLast24, double rating) {
        super(email, password, firstName, lastName, phoneNumber, address, profileImage, active, blocked);
        this.status = status;
        this.available = available;
        this.workingHoursLast24 = workingHoursLast24;
        this.rating = rating;
    }

    public Driver() {
        super();
    }
}