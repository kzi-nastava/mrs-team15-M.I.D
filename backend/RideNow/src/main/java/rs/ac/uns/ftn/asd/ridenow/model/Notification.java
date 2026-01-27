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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    public Notification(String message, NotificationType type, User user) {
        this.message = message;
        this.type = type;
        this.assignUser(user);
    }

    public Notification() {

    }

    public void markSeen(){
        this.seen = true;
    }

    public void markUnseen(){
        this.seen = false;
    }

    public void assignUser(User user) {
        this.user = user;
        if(user != null &&  !user.getNotifications().contains(this)){
            user.addNotification(this);
        }
    }
}