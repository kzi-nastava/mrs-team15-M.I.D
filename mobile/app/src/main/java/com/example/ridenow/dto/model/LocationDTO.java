package com.example.ridenow.dto.model;

import java.io.Serializable;

public class LocationDTO implements Serializable {
    private double latitude;
    private double longitude;
    private String address;

    public LocationDTO() {}

    public LocationDTO(double startLat, double startLon, String startAddress) {
        this.latitude = startLat;
        this.longitude = startLon;
        this.address = startAddress;
    }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
