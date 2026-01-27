package rs.ac.uns.ftn.asd.ridenow.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class FavoriteRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private RegisteredUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    public FavoriteRoute(RegisteredUser user, Route route) {
        this.user = user;
        this.route = route;
    }

    public FavoriteRoute() {}

    public void assignUser(RegisteredUser user){
        this.user = user;
        if(!user.getFavoriteRoutes().contains(this)){
            user.getFavoriteRoutes().add(this);
        }
    }
}
