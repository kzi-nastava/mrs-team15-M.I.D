package rs.ac.uns.ftn.asd.ridenow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.Ride;

import java.time.LocalDateTime;
import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByDriver(Driver driver);
    @Query("SELECT r FROM Ride r " +
            "LEFT JOIN FETCH r.panicAlert " +
            "LEFT JOIN FETCH r.passengers p " +
            "LEFT JOIN FETCH p.user " +
            "WHERE r.driver = :driver")
    List<Ride> findByDriverWithAllRelations(@Param("driver") Driver driver);

    @Query("SELECT r FROM Ride r " +
            "WHERE r.driver = :driver AND r.status = rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus.ACCEPTED")
    List<Ride> findScheduledRidesByDriver(@Param("driver") Driver driver);

    @Query("SELECT r FROM Ride r " +
            "WHERE r.driver.id = :driverId " +
            "AND r.status IN ('ACCEPTED', 'ACTIVE') " +
            "AND r.scheduledTime BETWEEN :now AND :nextHour " +
            "ORDER BY r.scheduledTime ASC")
    List<Ride> findScheduledRidesForDriverInNextHour(
            @Param("driverId") Long driverId,
            @Param("now") LocalDateTime now,
            @Param("nextHour") LocalDateTime nextHour
    );
}
