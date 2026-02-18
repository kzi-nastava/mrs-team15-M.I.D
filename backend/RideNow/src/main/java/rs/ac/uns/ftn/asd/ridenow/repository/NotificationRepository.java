package rs.ac.uns.ftn.asd.ridenow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import rs.ac.uns.ftn.asd.ridenow.model.Notification;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.model.enums.NotificationType;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndSeenOrderByCreatedAtDesc(User user, boolean seen);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.seen = false")
    long countUnseenByUser(User user);

    @Modifying
    @Query("UPDATE Notification n SET n.seen = true WHERE n.user = :user AND n.seen = false")
    void markAllAsSeenByUser(User user);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.relatedEntityId = :relatedEntityId AND n.type = :type")
    void deleteByRelatedEntityIdAndType(Long relatedEntityId, NotificationType type);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.relatedEntityId = :relatedEntityId AND n.type IN :types")
    void deleteByRelatedEntityIdAndTypes(Long relatedEntityId, List<NotificationType> types);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user = :user AND n.type = :type")
    void deleteByUserAndType(User user, NotificationType type);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user IN :users AND n.type IN :types")
    void deleteByUsersAndTypes(List<User> users, List<NotificationType> types);

    @Query("SELECT COUNT(n) > 0 FROM Notification n WHERE n.relatedEntityId = :rideId AND n.type = :type AND n.message LIKE %:minutePattern%")
    boolean existsByRideIdAndTypeAndMinutes(Long rideId, NotificationType type, String minutePattern);
}
