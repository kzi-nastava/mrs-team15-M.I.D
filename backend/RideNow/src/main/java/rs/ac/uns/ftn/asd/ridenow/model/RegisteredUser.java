package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.Entity;

@Entity
public class RegisteredUser extends User {

    public RegisteredUser(String email, String password, String firstName, String lastName, String phoneNumber,
                          String address, String profileImage, boolean active, boolean blocked) {
        super(email, password, firstName, lastName, phoneNumber, address, profileImage, active, blocked);
    }

    public RegisteredUser() {
        super();
    }
}
