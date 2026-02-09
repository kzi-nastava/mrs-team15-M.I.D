package com.example.ridenow.dto.rating;

public class RatingRequestDTO {
        private Integer driverRating;
        private Integer vehicleRating;
        private String driverComment;
        private String vehicleComment;

    public RatingRequestDTO(int driverRating, int vehicleRating, String driverComment, String vehicleComment) {
        this.driverRating = driverRating;
        this.vehicleRating = vehicleRating;
        this.driverComment = driverComment;
        this.vehicleComment = vehicleComment;
    }

    public int getDriverRating() {
        return driverRating;
    }

    public void setDriverRating(int driverRating) {
        this.driverRating = driverRating;
    }

    public int getVehicleRating() {
        return vehicleRating;
    }

    public void setVehicleRating(int vehicleRating) {
        this.vehicleRating = vehicleRating;
    }

    public String getDriverComment() {
        return driverComment;
    }

    public void setDriverComment(String driverComment) {
        this.driverComment = driverComment;
    }

    public String getVehicleComment() {
        return vehicleComment;
    }

    public void setVehicleComment(String vehicleComment) {
        this.vehicleComment = vehicleComment;
    }
}
