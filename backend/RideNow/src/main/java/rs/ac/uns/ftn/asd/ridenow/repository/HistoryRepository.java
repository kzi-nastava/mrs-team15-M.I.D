package rs.ac.uns.ftn.asd.ridenow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.ac.uns.ftn.asd.ridenow.model.Ride;

import java.time.LocalDateTime;

public interface HistoryRepository extends JpaRepository<Ride, Long> {

    // ===================== DRIVER =====================

    // Driver - with date filter
    @Query(
            value = "SELECT DISTINCT r FROM Ride r " +
                    "LEFT JOIN FETCH r.passengers p " +
                    "LEFT JOIN FETCH p.user " +
                    "LEFT JOIN FETCH r.panicAlert " +
                    "LEFT JOIN FETCH r.route " +
                    "WHERE r.driver.id = :userId " +
                    "AND r.status IN ('FINISHED', 'CANCELLED') " +
                    "AND r.startTime >= :startDate AND r.startTime <= :endDate",
            countQuery = "SELECT COUNT(DISTINCT r) FROM Ride r " +
                    "WHERE r.driver.id = :userId " +
                    "AND r.status IN ('FINISHED', 'CANCELLED') " +
                    "AND r.startTime >= :startDate AND r.startTime <= :endDate"
    )
    Page<Ride> findDriverRidesWithAllRelationsAndDate(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // Driver - without date filter
    @Query(
            value = "SELECT DISTINCT r FROM Ride r " +
                    "LEFT JOIN FETCH r.passengers p " +
                    "LEFT JOIN FETCH p.user " +
                    "LEFT JOIN FETCH r.panicAlert " +
                    "LEFT JOIN FETCH r.route " +
                    "WHERE r.driver.id = :userId " +
                    "AND r.status IN ('FINISHED', 'CANCELLED')",
            countQuery = "SELECT COUNT(DISTINCT r) FROM Ride r " +
                    "WHERE r.driver.id = :userId " +
                    "AND r.status IN ('FINISHED', 'CANCELLED')"
    )
    Page<Ride> findDriverRidesWithAllRelations(
            @Param("userId") Long userId,
            Pageable pageable
    );

    // ===================== PASSENGER =====================

    // Passenger - with date filter
    @Query(
            value = "SELECT DISTINCT r FROM Ride r " +
                    "JOIN r.passengers pass " +
                    "LEFT JOIN FETCH r.passengers p " +
                    "LEFT JOIN FETCH p.user " +
                    "LEFT JOIN FETCH r.panicAlert " +
                    "LEFT JOIN FETCH r.route " +
                    "LEFT JOIN FETCH r.driver " +
                    "WHERE pass.user.id = :userId " +
                    "AND r.status IN ('FINISHED', 'CANCELLED') " +
                    "AND r.startTime >= :startDate AND r.startTime <= :endDate",
            countQuery = "SELECT COUNT(DISTINCT r) FROM Ride r " +
                    "JOIN r.passengers pass " +
                    "WHERE pass.user.id = :userId " +
                    "AND r.status IN ('FINISHED', 'CANCELLED') " +
                    "AND r.startTime >= :startDate AND r.startTime <= :endDate"
    )
    Page<Ride> findPassengerRidesWithAllRelationsAndDate(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // Passenger - without date filter
    @Query(
            value = "SELECT DISTINCT r FROM Ride r " +
                    "JOIN r.passengers pass " +
                    "LEFT JOIN FETCH r.passengers p " +
                    "LEFT JOIN FETCH p.user " +
                    "LEFT JOIN FETCH r.panicAlert " +
                    "LEFT JOIN FETCH r.route " +
                    "LEFT JOIN FETCH r.driver " +
                    "WHERE pass.user.id = :userId " +
                    "AND r.status IN ('FINISHED', 'CANCELLED')",
            countQuery = "SELECT COUNT(DISTINCT r) FROM Ride r " +
                    "JOIN r.passengers pass " +
                    "WHERE pass.user.id = :userId " +
                    "AND r.status IN ('FINISHED', 'CANCELLED')"
    )
    Page<Ride> findPassengerRidesWithAllRelations(
            @Param("userId") Long userId,
            Pageable pageable
    );
}