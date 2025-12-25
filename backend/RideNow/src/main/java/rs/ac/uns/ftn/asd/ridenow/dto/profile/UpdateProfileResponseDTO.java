package rs.ac.uns.ftn.asd.ridenow.dto.profile;

import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;


public class UpdateProfileResponseDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean active;
    private String address;
    private String phoneNumber;
    private String profileImage;

    private String licensePlate;
    private String vehicleModel;
    private VehicleType vehicleType;
    private int numberOfSeats ;
    private boolean babyFriendly;
    private boolean petFriendly;
}

