package com.example.ridenow.dto.vehicle;

import com.example.ridenow.dto.model.Location;

public class VehicleResponse {
    private String licencePlate;
    private Location location;
    private Boolean available;

    public VehicleResponse() {
    }

    public String getLicencePlate() {
        return licencePlate;
    }

    public void setLicencePlate(String licencePlate) {
        this.licencePlate = licencePlate;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
