package rs.ac.uns.ftn.asd.ridenow.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class User {
    private Long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String profileImage;
    private boolean active;
    private boolean blocked;

    public User(){
        super();
    }

    public User(String email, String password, String firstName, String lastName, String phoneNumber, String address,Long id,
                String profileImage, boolean active, boolean blocked) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.id = id;
        this.profileImage = profileImage;
        this.active = active;
        this.blocked = blocked;
    }

}
