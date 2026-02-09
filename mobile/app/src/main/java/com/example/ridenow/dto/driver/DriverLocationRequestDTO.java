package com.example.ridenow.dto.driver;

public class DriverLocationRequestDTO {
    private double lat;
    private double lon;

    public DriverLocationRequestDTO() {}

    public DriverLocationRequestDTO(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
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
}
