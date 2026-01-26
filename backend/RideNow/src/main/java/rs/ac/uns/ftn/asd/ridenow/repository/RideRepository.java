package rs.ac.uns.ftn.asd.ridenow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.Ride;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    // Date-filtered passengers sorting - ascending
    @Query("SELECT r FROM Ride r " +
            "WHERE r.driver.id = :driverId " +
            "AND r.scheduledTime >= :startDate AND r.scheduledTime <= :endDate " +
            "ORDER BY " +
            "(SELECT COUNT(p) FROM r.passengers p) ASC, " +
            "(SELECT MIN(p.user.firstName) FROM r.passengers p) ASC")
    Page<Ride> findRidesSortedByFirstPassengerNameAscWithDate(@Param("driverId") Long driverId,
                                                              @Param("startDate") LocalDateTime startDate,
                                                              @Param("endDate") LocalDateTime endDate,
                                                              Pageable pageable);

    // Date-filtered passengers sorting - descending
    @Query("SELECT r FROM Ride r " +
            "WHERE r.driver.id = :driverId " +
            "AND r.scheduledTime >= :startDate AND r.scheduledTime <= :endDate " +
            "ORDER BY " +
            "(SELECT COUNT(p) FROM r.passengers p) DESC, " +
            "(SELECT MIN(p.user.firstName) FROM r.passengers p) DESC")
    Page<Ride> findRidesSortedByFirstPassengerNameDescWithDate(@Param("driverId") Long driverId,
                                                               @Param("startDate") LocalDateTime startDate,
                                                               @Param("endDate") LocalDateTime endDate,
                                                               Pageable pageable);

    // Date-filtered duration sorting - ascending
    @Query("SELECT r FROM Ride r " +
            "WHERE r.driver.id = :driverId " +
            "AND r.scheduledTime >= :startDate AND r.scheduledTime <= :endDate " +
            "ORDER BY CASE WHEN r.startTime IS NULL OR r.endTime IS NULL THEN 0 ELSE 1 END ASC, " +
            "CASE WHEN r.startTime IS NOT NULL AND r.endTime IS NOT NULL THEN (r.endTime - r.startTime) ELSE 0 END ASC")
    Page<Ride> findRidesSortedByDurationAscWithDate(@Param("driverId") Long driverId,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate,
                                                    Pageable pageable);

    // Date-filtered duration sorting - descending
    @Query("SELECT r FROM Ride r " +
            "WHERE r.driver.id = :driverId " +
            "AND r.scheduledTime >= :startDate AND r.scheduledTime <= :endDate " +
            "ORDER BY CASE WHEN r.startTime IS NULL OR r.endTime IS NULL THEN 1 ELSE 0 END ASC, " +
            "CASE WHEN r.startTime IS NOT NULL AND r.endTime IS NOT NULL THEN (r.endTime - r.startTime) ELSE 0 END DESC")
    Page<Ride> findRidesSortedByDurationDescWithDate(@Param("driverId") Long driverId,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate,
                                                     Pageable pageable);

    // Date-filtered standard sorting with relations
    @Query("SELECT r FROM Ride r " +
            "LEFT JOIN FETCH r.passengers p " +
            "LEFT JOIN FETCH p.user " +
            "LEFT JOIN FETCH r.panicAlert " +
            "WHERE r.driver = :driver " +
            "AND r.scheduledTime >= :startDate AND r.scheduledTime <= :endDate")
    Page<Ride> findByDriverWithAllRelationsAndDate(@Param("driver") Driver driver,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate,
                                                   Pageable pageable);

    @Query("SELECT r FROM Ride r " +
            "WHERE r.driver = :driver AND r.status = 'REQUESTED'")
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
            WHERE mp.user_id = :userId  AND r.status = 'REQUESTED' AND r.scheduled_time >= now()
            GROUP BY r.id, route.start_address, route.end_address, r.scheduled_time, cp.user_id
            ORDER BY r.scheduled_time ASC;
       \s""", nativeQuery = true)
    List<Object[]> findUpcomingRidesByUser(@Param("userId") Long userId);

    @Query(value = "SELECT r.* " +
            "FROM public.ride r " +
            "JOIN public.passenger p ON r.id = p.ride_id " +
            "WHERE r.status = 'IN_PROGRESS' AND p.user_id = :userId",
            nativeQuery = true)
    Optional<Ride> findCurrentRideByUser(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM public.ride " +
            "WHERE driver_id = :driverId AND status = 'IN_PROGRESS'",
            nativeQuery = true)
    Optional<Ride> findCurrentRideByDriver(@Param("driverId") Long driverId);



}
