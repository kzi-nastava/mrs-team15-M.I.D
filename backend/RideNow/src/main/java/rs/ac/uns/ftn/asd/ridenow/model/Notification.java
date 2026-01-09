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

    @Column(nullable = false)
    @Lob
    private String message;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean seen = false;

    public Notification(String message, NotificationType type, LocalDateTime createdAt, boolean seen) {
        this.message = message;
        this.type = type;
        this.createdAt = createdAt;
        this.seen = seen;
    }

    public Notification(String message, NotificationType type) {
        this.message = message;
        this.type = type;
        this.seen = false;
    }

    public Notification() {
    }
}