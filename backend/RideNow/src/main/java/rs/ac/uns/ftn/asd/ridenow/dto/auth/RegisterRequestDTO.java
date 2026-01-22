package rs.ac.uns.ftn.asd.ridenow.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.validator.PasswordMatch;

@Setter
@Getter
@PasswordMatch(password = "password", confirmPassword = "confirmPassword", message = "Passwords do not match")
public class RegisterRequestDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank(message = "Confirm password is required")
    @Size(min = 6, message = "Confirm password must be at least 6 characters long")
    private String confirmPassword;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 30, message = "First name must be between 2 and 30 characters")
    @Pattern(regexp = "^[A-ZČĆŠĐŽ][a-zčćšđž]+$",
            message = "First name must start with a capital letter and contain only letters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 30, message = "Last name must be between 2 and 30 characters")
    @Pattern(regexp = "^[A-ZČĆŠĐŽ][a-zčćšđž]+$",
            message = "Last name must start with a capital letter and contain only letters")
    private String lastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+381|0)[0-9]{9,10}$",message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    @Pattern(regexp = "^[A-Za-zČĆŠĐŽčćšđž0-9\\s.,/\\-]{5,}$",
            message = "Address must be at least 5 characters and contain only valid characters")
    private String address;

    public RegisterRequestDTO() {
        super();
    }
}
