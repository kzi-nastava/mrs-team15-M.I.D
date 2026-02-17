package rs.ac.uns.ftn.asd.ridenow.dto.admin;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Setter
@Getter
public class RegisterDriverRequestDTO {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String address;

    @NotBlank
    private String licensePlate;

    @NotBlank
    private String vehicleModel;

    @NotNull
    private VehicleType vehicleType;

    @Min(1)
    private int numberOfSeats;

    private boolean babyFriendly;
    private boolean petFriendly;

    public RegisterDriverRequestDTO() {
        super();
    }

}
