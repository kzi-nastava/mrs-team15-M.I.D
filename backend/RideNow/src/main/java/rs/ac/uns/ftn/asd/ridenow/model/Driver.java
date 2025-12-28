package rs.ac.uns.ftn.asd.ridenow.model;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;

@Getter @Setter
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
}