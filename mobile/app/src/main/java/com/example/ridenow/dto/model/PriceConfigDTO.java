package com.example.ridenow.dto.model;

import com.example.ridenow.dto.enums.VehicleType;

public class PriceConfigDTO {
    private VehicleType vehicleType;
    private double basePrice;
    private double pricePerKm;

    public PriceConfigDTO() {}

    public PriceConfigDTO(VehicleType vehicleType, double basePrice, double pricePerKm) {
        this.vehicleType = vehicleType;
        this.basePrice = basePrice;
        this.pricePerKm = pricePerKm;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getPricePerKm() {
        return pricePerKm;
    }

    public void setPricePerKm(double pricePerKm) {
        this.pricePerKm = pricePerKm;
    }
}
