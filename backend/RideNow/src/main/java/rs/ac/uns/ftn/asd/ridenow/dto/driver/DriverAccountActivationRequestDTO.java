package rs.ac.uns.ftn.asd.ridenow.dto.driver;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverAccountActivationRequestDTO {
    @NotBlank
    private String password;

    @NotBlank
    private String passwordConfirmation;

    private String token;

    public DriverAccountActivationRequestDTO() {
        super();
    }
}
