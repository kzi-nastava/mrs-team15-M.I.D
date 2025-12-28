package rs.ac.uns.ftn.asd.ridenow.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ForgotPasswordRequestDTO {
    private String email;

    public ForgotPasswordRequestDTO() {
        super();
    }

}
