package rs.ac.uns.ftn.asd.ridenow.dto.profile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;


public class ChangePasswordDTO {

    @NotBlank
    private String currentPassword;

    @NotBlank
    private String newPassword;

    @NotBlank
    private String confirmNewPassword;

    public String getCurrentPassword() { return currentPassword; }

}

