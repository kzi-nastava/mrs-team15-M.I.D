package rs.ac.uns.ftn.asd.ridenow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.asd.ridenow.model.PriceConfig;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

import java.util.Optional;

public interface PriceRepository extends JpaRepository<PriceConfig, Long> {
    Optional<PriceConfig> findByVehicleType(VehicleType vehicleType);
}
