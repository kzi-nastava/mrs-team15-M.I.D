package rs.ac.uns.ftn.asd.ridenow.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginResponseDTO {
    private String token;
    private String role;

    public LoginResponseDTO() {
        super();
    }
}
