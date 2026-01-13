package rs.ac.uns.ftn.asd.ridenow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.asd.ridenow.model.Route;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
}

