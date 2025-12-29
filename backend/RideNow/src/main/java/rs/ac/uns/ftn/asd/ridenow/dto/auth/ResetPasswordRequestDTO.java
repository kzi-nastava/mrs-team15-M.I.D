package rs.ac.uns.ftn.asd.ridenow.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResetPasswordRequestDTO {
    private String newPassword;
    private  String confirmNewPassword;

    public ResetPasswordRequestDTO() {
        super();
    }

}
