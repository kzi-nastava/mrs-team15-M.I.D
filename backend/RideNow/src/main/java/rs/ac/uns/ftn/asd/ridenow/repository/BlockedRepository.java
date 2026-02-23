package rs.ac.uns.ftn.asd.ridenow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.asd.ridenow.model.Blocked;
import rs.ac.uns.ftn.asd.ridenow.model.User;

import java.util.Optional;

@Repository
public interface BlockedRepository extends JpaRepository<Blocked, Long> {
    Optional<Blocked> findByUser(User user);
    void deleteByUser(User user);
}
