package rs.ac.uns.ftn.asd.ridenow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.asd.ridenow.model.Route;

import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    @Query("SELECT r FROM Route r WHERE r.startLocation.address = :startAddress AND r.endLocation.address = :endAddress")
    Optional<Route> findByStartAndEndAddress(
            @Param("startAddress") String startAddress,
            @Param("endAddress") String endAddress
    );
}