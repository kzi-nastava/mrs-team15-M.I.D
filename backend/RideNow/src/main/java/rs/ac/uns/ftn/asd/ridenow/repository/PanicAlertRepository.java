package rs.ac.uns.ftn.asd.ridenow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.asd.ridenow.model.PanicAlert;
import rs.ac.uns.ftn.asd.ridenow.model.Ride;

import java.util.List;

public interface PanicAlertRepository extends JpaRepository<PanicAlert, Long> {
    List<PanicAlert> findByRide(Ride ride);
}
