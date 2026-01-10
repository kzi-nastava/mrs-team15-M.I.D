package rs.ac.uns.ftn.asd.ridenow.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OldPassanger extends User {

    private List<Route> favoriteRoutes;

    public OldPassanger(String email, String password, String firstName, String lastName, String phoneNumber, String address, Long id,
                        String profileImage, boolean active, boolean blocked, List<Route> favoriteRoutes) {
        super(email, password, firstName, lastName, phoneNumber, address, profileImage, active, blocked);
        this.favoriteRoutes = favoriteRoutes;
    }
}
