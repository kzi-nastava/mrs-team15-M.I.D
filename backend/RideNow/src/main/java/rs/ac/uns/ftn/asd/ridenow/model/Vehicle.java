package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    private boolean available = false;

    @Min(0)
    @Max(5)
    @Column(nullable = false)
    private double rating = 0.0;

    @Column(nullable = false)
    private boolean petFriendly = false;

    @Column(nullable = false)
    private boolean childFriendly = false;

    @Column(nullable = false)
    @Min(0)
    private int seatCount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VehicleType type;

    @OneToOne
    @JoinColumn(name = "driver_id", nullable = false, unique = true)
    private Driver driver;

    public Vehicle(String licencePlate, String model, boolean petFriendly,
                   boolean childFriendly, int seatCount, VehicleType type, Driver driver) {
        this.licencePlate = licencePlate;
        this.model = model;
        this.petFriendly = petFriendly;
        this.childFriendly = childFriendly;
        this.seatCount = seatCount;
        this.type = type;
        this.driver = driver;
    }

    public Vehicle() {

    }

    public void markAsAvailable(){
        available = true;
    }

    public void markAsUnavailable(){
        available = false;
    }

    public void assignDriver(Driver driver) {
        this.driver = driver;
        if (driver != null && driver.getVehicle() != this) {
            driver.setVehicle(this);
        }
    }
}
