package rs.ac.uns.ftn.asd.ridenow.model;

public class RegisteredUser extends User {
    public RegisteredUser(String email, String password, String firstName, String lastName, String phoneNumber, Long id,
                          String profileImage, boolean active, boolean blocked) {
        super(email, password, firstName, lastName, phoneNumber, id, profileImage, active, blocked);
    }
}
