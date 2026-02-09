package rs.ac.uns.ftn.asd.ridenow.dto.user;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

@Setter
@Getter
public class UserResponseDTO {

    private Long id;
    private UserRoles role;
    private String email;
    private String firstName;
    private String lastName;
    private boolean active;
    private String address;
    private String phoneNumber;
    private String profileImage;
    private boolean blocked;

    private String licensePlate;
    private String vehicleModel;
    private VehicleType vehicleType;
    private int numberOfSeats ;
    private boolean babyFriendly;
    private boolean petFriendly;
    private double hoursWorkedLast24;
    public UserResponseDTO() { super(); }

}
