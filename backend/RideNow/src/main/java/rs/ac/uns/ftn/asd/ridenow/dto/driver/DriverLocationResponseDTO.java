package rs.ac.uns.ftn.asd.ridenow.dto.driver;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DriverLocationResponseDTO {
    private double lat;
    private double lon;
    private String licencePlate;

    public DriverLocationResponseDTO(double lat, double lon, String licencePlate) {
        this.licencePlate = licencePlate;
        this.lat = lat;
        this.lon = lon;
    }

    public DriverLocationResponseDTO() {}
}
