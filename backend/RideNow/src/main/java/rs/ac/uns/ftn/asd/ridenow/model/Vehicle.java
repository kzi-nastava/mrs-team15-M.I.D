package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

@Getter
@Setter
@Entity
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String licencePlate;

    @Column(nullable = false)
    private String model;

    private double lat;

    private double lon;

    @Column(nullable = false)
    private boolean available;

    @Column(nullable = false)
    private double rating = 0.0;

    @Column(nullable = false)
    private boolean petFriendly = false;

    @Column(nullable = false)
    private boolean childFriendly = false;

    @Column(nullable = false)
    private int seatCount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VehicleType type;

    public Vehicle(String licencePlate, double lat, double lon, boolean available, double rating) {
        this.licencePlate = licencePlate;
        this.lat = lat;
        this.lon = lon;
        this.available = available;
        this.rating = rating;
    }

    public Vehicle(String licencePlate, String model, boolean available, double rating,
                   boolean petFriendly, boolean childFriendly, int seatCount, VehicleType type) {
        this.licencePlate = licencePlate;
        this.model = model;
        this.available = available;
        this.rating = rating;
        this.petFriendly = petFriendly;
        this.childFriendly = childFriendly;
        this.seatCount = seatCount;
        this.type = type;
    }

    public Vehicle() {

    }
}
