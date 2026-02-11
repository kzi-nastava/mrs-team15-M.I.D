package rs.ac.uns.ftn.asd.ridenow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rs.ac.uns.ftn.asd.ridenow.model.Chat;
import rs.ac.uns.ftn.asd.ridenow.model.User;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByUser(User user);

    @Query("SELECT c FROM Chat c WHERE c.taken IS false")
    List<Chat> findNotTaken();
}
