package rs.ac.uns.ftn.asd.ridenow.dto.driver;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

public class DriverChangeRequestDTO {


    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String address;

    private String profileImage;

    @NotBlank
    private String vehicleModel;

    @Min(1)
    private int numberOfSeats;

    @NotNull
    private VehicleType vehicleType;

    private Boolean babyFriendly;
    private Boolean petFriendly;

    public DriverChangeRequestDTO() { super(); }

}
