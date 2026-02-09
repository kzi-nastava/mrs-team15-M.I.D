package com.example.ridenow.dto.vehicle;

import com.example.ridenow.dto.model.LocationDTO;

public class VehicleResponseDTO {
    private String licencePlate;
    private LocationDTO location;
    private Boolean available;

    public VehicleResponseDTO() {
    }

    public String getLicencePlate() {
        return licencePlate;
    }

    public void setLicencePlate(String licencePlate) {
        this.licencePlate = licencePlate;
    }

    public LocationDTO getLocation() {
        return location;
    }

    public void setLocation(LocationDTO location) {
        this.location = location;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
