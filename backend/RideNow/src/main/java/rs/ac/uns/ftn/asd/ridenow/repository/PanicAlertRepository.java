package rs.ac.uns.ftn.asd.ridenow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.asd.ridenow.model.PanicAlert;
import rs.ac.uns.ftn.asd.ridenow.model.Ride;

import java.util.List;
import java.util.Optional;

@Repository
public interface PanicAlertRepository extends JpaRepository<PanicAlert, Long> {

    List<PanicAlert> findByRide(Ride ride);

    @Query("SELECT DISTINCT pa FROM PanicAlert pa " +
            "LEFT JOIN FETCH pa.ride r " +
            "LEFT JOIN FETCH r.driver d " +
            "LEFT JOIN FETCH r.passengers p " +
            "LEFT JOIN FETCH p.user " +
            "WHERE pa.resolved = false " +
            "ORDER BY pa.createdAt DESC")
    List<PanicAlert> findAllUnresolved();

    Optional<PanicAlert> findByIdAndResolvedFalse(Long id);

    @Query("SELECT pa FROM PanicAlert pa WHERE pa.ride.id = :rideId")
    Optional<PanicAlert> findByRideId(Long rideId);

    @Query("SELECT DISTINCT pa FROM PanicAlert pa " +
            "LEFT JOIN FETCH pa.ride r " +
            "LEFT JOIN FETCH r.driver d " +
            "LEFT JOIN FETCH r.passengers p " +
            "LEFT JOIN FETCH p.user " +
            "ORDER BY pa.createdAt DESC")
    List<PanicAlert> findAllWithDetails();

    @Override
    @Query("SELECT DISTINCT pa FROM PanicAlert pa " +
            "LEFT JOIN FETCH pa.ride r " +
            "LEFT JOIN FETCH r.driver " +
            "LEFT JOIN FETCH r.passengers " +
            "ORDER BY pa.createdAt DESC")
    List<PanicAlert> findAll();
}