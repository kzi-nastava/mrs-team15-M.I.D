package com.example.ridenow.dto.model;

import java.io.Serializable;

public class RatingDTO implements Serializable {
    private Integer driverRating;
    private Integer vehicleRating;
    private String driverComment;
    private String vehicleComment;
    public int getDriverRating() { return driverRating; }
    public void setDriverRating(int driverRating) { this.driverRating = driverRating; }

    public int getVehicleRating() { return vehicleRating; }
    public void setVehicleRating(int vehicleRating) { this.vehicleRating = vehicleRating; }

    public String getDriverComment() { return driverComment; }
    public void setDriverComment(String driverComment) { this.driverComment = driverComment; }

    public String getVehicleComment() { return vehicleComment; }
    public void setVehicleComment(String vehicleComment) { this.vehicleComment = vehicleComment; }

}
