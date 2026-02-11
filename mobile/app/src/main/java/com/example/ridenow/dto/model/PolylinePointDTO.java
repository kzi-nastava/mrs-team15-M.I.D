package com.example.ridenow.dto.model;

import java.io.Serializable;

public class PolylinePointDTO implements Serializable {
    private double latitude;
    private double longitude;


    public PolylinePointDTO() {}

    public PolylinePointDTO(Double aDouble, Double aDouble1) {
        this.latitude = aDouble;
        this.longitude = aDouble1;
    }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
