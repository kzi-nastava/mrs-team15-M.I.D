package rs.ac.uns.ftn.asd.ridenow.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequestDTO {
    private String email;
    private String password;

    public LoginRequestDTO() {
        super();
    }

}