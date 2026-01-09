package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@DiscriminatorValue("PASSENGER")
public class Passenger extends User {

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "passenger_id")
    private List<Route> favoriteRoutes;

    public Passenger(String email, String password, String firstName, String lastName, String phoneNumber, String address, Long id,
                     String profileImage, boolean active, boolean blocked, List<Route> favoriteRoutes) {
        super(email, password, firstName, lastName, phoneNumber, address,id, profileImage, active, blocked);
        this.favoriteRoutes = favoriteRoutes;
    }

    public Passenger() {

    }
}
