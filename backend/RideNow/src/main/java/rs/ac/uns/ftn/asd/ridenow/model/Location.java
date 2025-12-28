package rs.ac.uns.ftn.asd.ridenow.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Location {

    private Long id;
    private double latitude;
    private double longitude;
    private String address;

    public Location(Long id, double latitude, double longitude, String address) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }
}
