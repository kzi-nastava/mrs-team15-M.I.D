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

    @Query(value = """
            SELECT r.id, cp.user_id AS creator_id, route.start_address || ' → ' || route.end_address AS route,
                   to_char(r.scheduled_time, 'DD-MM-YYYY, HH24:MI') AS "startTime",
                   STRING_AGG(u.first_name || ' ' || u.last_name, ', ') AS passengers
            FROM public.ride r
            JOIN public.passenger cp
            ON cp.ride_id = r.id AND cp.role = 'CREATOR'
            JOIN public.passenger mp ON mp.ride_id = r.id
            JOIN public.passenger p  ON p.ride_id = r.id
            JOIN public."user" u ON u.id = p.user_id
            JOIN public.route route ON route.id = r.route_id
            WHERE mp.user_id = :userId  AND r.status = 'ACCEPTED' AND r.scheduled_time >= now()
            GROUP BY r.id, route.start_address, route.end_address, r.scheduled_time, cp.user_id
            ORDER BY r.scheduled_time ASC;
       \s""", nativeQuery = true)
    List<Object[]> findUpcomingRidesByUser(@Param("userId") Long userId);
}
