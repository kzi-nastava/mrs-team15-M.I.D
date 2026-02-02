package rs.ac.uns.ftn.asd.ridenow.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyCodeRequestDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank
    @Pattern(regexp = "\\d{6}")
    private String code;

    public VerifyCodeRequestDTO() {}
}
