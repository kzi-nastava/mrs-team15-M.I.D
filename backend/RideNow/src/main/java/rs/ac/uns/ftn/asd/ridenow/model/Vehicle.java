package rs.ac.uns.ftn.asd.ridenow.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

@Getter @Setter
@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    @Column(nullable = false, unique = true)
    private String licencePlate;
    private double lat;
    private double lon;
    @Column(nullable = false)
    private boolean available;
    private double rating;
    @Column(nullable = false)
    private boolean petFriendly;
    @Column(nullable = false)
    private boolean childFriendly;
    @Column(nullable = false)
    private VehicleType type;

    public Vehicle() {}

    public Vehicle(String licencePlate, double lat, double lon, boolean available, double rating) {
        this.licencePlate = licencePlate;
        this.lat = lat;
        this.lon = lon;
        this.available = available;
        this.rating = rating;
    }
}
