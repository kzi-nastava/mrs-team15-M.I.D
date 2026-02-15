package rs.ac.uns.ftn.asd.ridenow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.asd.ridenow.model.FavoriteRoute;

@Repository
public interface FavoriteRouteRepository extends JpaRepository<FavoriteRoute, Long> {

}
