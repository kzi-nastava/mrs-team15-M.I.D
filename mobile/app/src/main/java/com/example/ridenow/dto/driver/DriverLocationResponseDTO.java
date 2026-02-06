package com.example.ridenow.dto.driver;

public class DriverLocationResponseDTO {
    private double lat;
    private double lon;
    private String licencePlate;

    public DriverLocationResponseDTO() {}

    public DriverLocationResponseDTO(double lat, double lon, String licencePlate) {
        this.lat = lat;
        this.lon = lon;
        this.licencePlate = licencePlate;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getLicencePlate() {
        return licencePlate;
    }

    public void setLicencePlate(String licencePlate) {
        this.licencePlate = licencePlate;
    }
}
