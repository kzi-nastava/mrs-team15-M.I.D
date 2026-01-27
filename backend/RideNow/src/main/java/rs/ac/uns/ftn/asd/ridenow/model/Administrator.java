package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.Entity;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;

@Entity
public class Administrator extends User {

    public Administrator(String email, String password, String firstName, String lastName, String phoneNumber,
                         String address, String profileImage, boolean active, boolean blocked, boolean jwtTokenValid) {
        super(email, password, firstName, lastName, phoneNumber, address, profileImage, active, blocked, UserRoles.ADMIN, jwtTokenValid);
    }

    public Administrator(String email, String password, String firstName, String lastName, String phoneNumber,
                         String address) {
        super(email, password, firstName, lastName, phoneNumber, address, null, true, false, UserRoles.ADMIN, false);
    }

    public Administrator() {
        super();
    }
}