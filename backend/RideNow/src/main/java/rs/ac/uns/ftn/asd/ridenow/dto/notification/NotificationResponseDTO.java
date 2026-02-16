package rs.ac.uns.ftn.asd.ridenow.dto.notification;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.Notification;
import rs.ac.uns.ftn.asd.ridenow.model.enums.NotificationType;

import java.time.LocalDateTime;

@Getter @Setter
public class NotificationResponseDTO {
    private Long id;
    private String message;
    private NotificationType type;
    private LocalDateTime createdAt;
    private boolean seen;
    private Long relatedEntityId; // For ride ID, etc.

    public NotificationResponseDTO(Notification notification) {
        this.id = notification.getId();
        this.message = notification.getMessage();
        this.type = notification.getType();
        this.createdAt = notification.getCreatedAt();
        this.seen = notification.isSeen();
        this.relatedEntityId = notification.getRelatedEntityId();
    }

    public NotificationResponseDTO() {}
}
