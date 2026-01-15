package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class RegisteredUser extends User {

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Passenger> rideParticipation = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FavoriteRoute> favoriteRoutes = new ArrayList<>();

    public RegisteredUser(String email, String password, String firstName, String lastName, String phoneNumber,
                          String address, String profileImage, boolean active, boolean blocked) {
        super(email, password, firstName, lastName, phoneNumber, address, profileImage, active, blocked, UserRoles.USER);
    }

    public RegisteredUser() {
        super();
    }

    public void addParticipation(Passenger passenger) {
        if(passenger!= null && !this.rideParticipation.contains(passenger)){
            rideParticipation.add(passenger);
            passenger.assignUser(this);
        }
    }

    public void removeParticipation(Passenger passenger) {
        if(passenger!= null && this.rideParticipation.contains(passenger)){
            rideParticipation.remove(passenger);
            passenger.setUser(null);
        }
    }
}
