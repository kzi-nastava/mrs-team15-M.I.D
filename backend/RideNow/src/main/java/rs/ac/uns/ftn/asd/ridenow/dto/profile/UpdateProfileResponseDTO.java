package rs.ac.uns.ftn.asd.ridenow.dto.profile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;


public class UpdateProfileRequestDTO {
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

