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
}
