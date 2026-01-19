package rs.ac.uns.ftn.asd.ridenow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.Ride;

import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByDriver(Driver driver);
    @Query("SELECT r FROM Ride r " +
            "LEFT JOIN FETCH r.passengers p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH r.panicAlert " +
            "WHERE r.driver = :driver")
    Page<Ride> findByDriverWithAllRelations(@Param("driver") Driver driver, Pageable pageable);

    // Passengers sorting - ascending (rides with no passengers come first)
    @Query("SELECT r FROM Ride r " +
            "WHERE r.driver.id = :driverId " +
            "ORDER BY " +
            "(SELECT COUNT(p) FROM r.passengers p) ASC, " +
            "(SELECT MIN(p.user.firstName) FROM r.passengers p) ASC")
    Page<Ride> findRidesSortedByFirstPassengerNameAsc(@Param("driverId") Long driverId, Pageable pageable);

    // Passengers sorting - descending (rides with no passengers come last)
    @Query("SELECT r FROM Ride r " +
            "WHERE r.driver.id = :driverId " +
            "ORDER BY " +
            "(SELECT COUNT(p) FROM r.passengers p) DESC, " +
            "(SELECT MIN(p.user.firstName) FROM r.passengers p) DESC")
    Page<Ride> findRidesSortedByFirstPassengerNameDesc(@Param("driverId") Long driverId, Pageable pageable);

    // Duration sorting - ascending (rides with no duration come first)
    @Query("SELECT r FROM Ride r " +
            "WHERE r.driver.id = :driverId " +
            "ORDER BY CASE WHEN r.startTime IS NULL OR r.endTime IS NULL THEN 0 ELSE 1 END ASC, " +
            "CASE WHEN r.startTime IS NOT NULL AND r.endTime IS NOT NULL THEN (r.endTime - r.startTime) ELSE 0 END ASC")
    Page<Ride> findRidesSortedByDurationAsc(@Param("driverId") Long driverId, Pageable pageable);

    // Duration sorting - descending (rides with no duration come last)
    @Query("SELECT r FROM Ride r " +
            "WHERE r.driver.id = :driverId " +
            "ORDER BY CASE WHEN r.startTime IS NULL OR r.endTime IS NULL THEN 1 ELSE 0 END ASC, " +
            "CASE WHEN r.startTime IS NOT NULL AND r.endTime IS NOT NULL THEN (r.endTime - r.startTime) ELSE 0 END DESC")
    Page<Ride> findRidesSortedByDurationDesc(@Param("driverId") Long driverId, Pageable pageable);
}
