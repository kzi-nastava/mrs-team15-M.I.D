package rs.ac.uns.ftn.asd.ridenow.dto.vehicles;

import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

public class VehicleDTO {
    private String licencePlate;
    private double lat;
    private double lon;
    private boolean available;
    private double rating;
    private boolean petFriendly;
    private boolean childFriendly;
    private VehicleType type;


    public VehicleDTO() {
    }

    public String getLicencePlate() {
        return licencePlate;
    }
    public void setLicencePlate(String licencePlate) {
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

    public boolean isAvailable() {
        return available;
    }
    public void setAvailable(boolean available) {
        this.available = available;
    }

    public double getRating() {
        return rating;
    }
    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean isPetFriendly() {
        return petFriendly;
    }
    public void setPetFriendly(boolean petFriendly) {
        this.petFriendly = petFriendly;
    }

    public boolean isChildFriendly() {
        return childFriendly;
    }
    public void setChildFriendly(boolean childFriendly) {
        this.childFriendly = childFriendly;
    }

    public VehicleType getType() {
        return type;
    }
    public void setType(VehicleType type) {
        this.type = type;
    }
}
