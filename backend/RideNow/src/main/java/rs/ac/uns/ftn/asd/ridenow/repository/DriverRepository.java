package rs.ac.uns.ftn.asd.ridenow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    @Query("SELECT d FROM Driver d WHERE d.available = true AND d.vehicle.type = :type " +
            "AND d.vehicle.seatCount >= :seats " +
            "AND (:babyFriendly = false OR d.vehicle.childFriendly = true) " +
            "AND (:petFriendly = false OR d.vehicle.petFriendly = true) " +
            "ORDER BY d.workingHoursLast24 ASC")
    Optional<Driver> autoAssign(@Param("type") VehicleType type,
                                       @Param("seats") int seats,
                                       @Param("babyFriendly") boolean babyFriendly,
                                       @Param("petFriendly") boolean petFriendly);
}
