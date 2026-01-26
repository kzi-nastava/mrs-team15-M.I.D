package rs.ac.uns.ftn.asd.ridenow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.Vehicle;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    @Query(value = """
        SELECT * FROM vehicle v
        WHERE v.lat BETWEEN :minLat AND :maxLat
        AND v.lon BETWEEN :minLng AND :maxLng
        """, nativeQuery = true)
    List<Vehicle> findVehiclesWithinBounds(
            @Param("minLat") Double minLatitude,
            @Param("maxLat") Double maxLatitude,
            @Param("minLng") Double minLongitude,
            @Param("maxLng") Double maxLongitude
    );

    Vehicle findByLicencePlate(String licencePlate);

    Vehicle findByDriver(Driver driver);
}
