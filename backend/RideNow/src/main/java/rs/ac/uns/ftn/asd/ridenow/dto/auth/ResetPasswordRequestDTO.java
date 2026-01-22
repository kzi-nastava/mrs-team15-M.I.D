package rs.ac.uns.ftn.asd.ridenow.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.validator.PasswordMatch;

@Setter
@Getter
@PasswordMatch(password = "newPassword", confirmPassword = "confirmNewPassword", message = "Passwords do not match")
public class ResetPasswordRequestDTO {
    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "New password must be at least 6 characters long")
    private String newPassword;

    @NotBlank(message = "Confirm new password is required")
    @Size(min = 6, message = "Confirm new password must be at least 6 characters long")
    private  String confirmNewPassword;

    public ResetPasswordRequestDTO() {
        super();
    }

}
