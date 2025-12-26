package rs.ac.uns.ftn.asd.ridenow.model;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;

public class Driver extends User {
    private DriverStatus status;
    private boolean available;
    private double workingHoursLast24;
    private double rating;
    private Vehicle vehicle;

    public Driver(String email, String password, String firstName, String lastName, String phoneNumber, String address,Long id,
                  String profileImage, boolean active, boolean blocked, DriverStatus status, boolean available,
                  double workingHoursLast24, double rating, Vehicle vehicle) {
        super(email, password, firstName, lastName, phoneNumber, address,id, profileImage, active, blocked);
        this.status = status;
        this.available = available;
        this.workingHoursLast24 = workingHoursLast24;
        this.rating = rating;
        this.vehicle = vehicle;
    }

    public DriverStatus getStatus() {
        return status;
    }

    public void setStatus(DriverStatus status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public double getWorkingHoursLast24() {
        return workingHoursLast24;
    }

    public void setWorkingHoursLast24(double workingHoursLast24) {
        this.workingHoursLast24 = workingHoursLast24;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }
}