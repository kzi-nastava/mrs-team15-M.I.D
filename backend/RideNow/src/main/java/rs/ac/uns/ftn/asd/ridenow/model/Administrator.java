package rs.ac.uns.ftn.asd.ridenow.model;

public class Administrator extends User {

    public Administrator(String email, String password, String firstName, String lastName, String phoneNumber,
                         Long id, String profileImage, boolean active, boolean blocked) {
        super(email, password, firstName, lastName, phoneNumber, id, profileImage, active, blocked);
    }
}
