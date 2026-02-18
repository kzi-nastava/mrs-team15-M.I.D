package rs.ac.uns.ftn.asd.ridenow.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserItemDTO {
    private Long id;
    private String Name;
    private String Surname;
    private String Email;
    private String Role;

    public UserItemDTO(){
        super();
    }
}
