package rs.ac.uns.ftn.asd.ridenow.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterResponseDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean active;

    public RegisterResponseDTO(){
        super();
    }

}
