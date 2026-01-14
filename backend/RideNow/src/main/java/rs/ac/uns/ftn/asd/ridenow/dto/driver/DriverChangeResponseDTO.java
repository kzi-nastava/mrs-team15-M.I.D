package rs.ac.uns.ftn.asd.ridenow.dto.driver;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

@Getter
@Setter
public class DriverChangeResponseDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String profileImage;
    private String vehicleModel;
    private int numberOfSeats;
    private VehicleType vehicleType;
    private String licensePlate;
    private Boolean babyFriendly;
    private Boolean petFriendly;

    public DriverChangeResponseDTO() { super(); }

}
