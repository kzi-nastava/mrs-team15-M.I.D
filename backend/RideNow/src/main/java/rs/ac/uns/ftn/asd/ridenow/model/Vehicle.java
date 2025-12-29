package rs.ac.uns.ftn.asd.ridenow.model;


import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

@Getter @Setter
public class Vehicle {
    private String licencePlate;
    private double lat;
    private double lon;
    private boolean available;
    private double rating;
    private boolean petFriendly;
    private boolean childFriendly;
    private VehicleType type;


    public Vehicle(String licencePlate, double lat, double lon, boolean available, double rating) {
        this.licencePlate = licencePlate;
        this.lat = lat;
        this.lon = lon;
        this.available = available;
        this.rating = rating;
    }
}
