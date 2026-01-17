package rs.ac.uns.ftn.asd.ridenow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.asd.ridenow.model.Passenger;
import rs.ac.uns.ftn.asd.ridenow.model.RegisteredUser;

import java.util.Optional;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    Optional<Object> findByUser(RegisteredUser user);
}
