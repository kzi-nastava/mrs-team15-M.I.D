package com.example.ridenow.dto.ride;

public class RoutePointDTO {
    private double lat;
    private double lng;

    public RoutePointDTO(){}

    public RoutePointDTO(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}