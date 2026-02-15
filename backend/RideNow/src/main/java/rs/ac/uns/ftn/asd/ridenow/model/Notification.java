package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import rs.ac.uns.ftn.asd.ridenow.model.enums.NotificationType;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String message;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean seen = false;

    @Column(name = "related_entity_id")
    private Long relatedEntityId; // For storing ride ID, driver ID, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Notification() {
    }

    public Notification(User user, String message, NotificationType type) {
        this.user = user;
        this.message = message;
        this.type = type;
        this.seen = false;
    }

    public Notification(User user, String message, NotificationType type, Long relatedEntityId) {
        this.user = user;
        this.message = message;
        this.type = type;
        this.relatedEntityId = relatedEntityId;
        this.seen = false;
    }
}