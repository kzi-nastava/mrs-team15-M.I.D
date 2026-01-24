package rs.ac.uns.ftn.asd.ridenow.repository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import rs.ac.uns.ftn.asd.ridenow.model.RegisteredUser;

import java.util.Optional;

public interface RegisteredUserRepository extends JpaRepository<RegisteredUser, Long> {
    Optional<Object> findByEmail(@Email @NotBlank String mainPassengerEmail);
}
