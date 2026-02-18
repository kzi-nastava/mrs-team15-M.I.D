package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.notification.NotificationResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.Map;

@Tag(name = "Notifications", description = "Notification management endpoints")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Get all notifications", description = "Retrieve all notifications for the current user")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getAllNotifications() {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<NotificationResponseDTO> notifications = notificationService.getUserNotifications(user);
            return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get unseen notifications", description = "Retrieve only unseen notifications for the current user")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER') or hasRole('ADMIN')")
    @GetMapping("/unseen")
    public ResponseEntity<List<NotificationResponseDTO>> getUnseenNotifications() {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<NotificationResponseDTO> notifications = notificationService.getUnseenNotifications(user);
            return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Get unseen notification count", description = "Retrieve the count of unseen notifications for the current user")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER') or hasRole('ADMIN')")
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnseenCount() {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            long count = notificationService.getUnseenNotificationCount(user);
            return ResponseEntity.ok(Map.of("count", count));
    }

    @Operation(summary = "Mark notification as seen", description = "Mark a specific notification as seen by ID")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER') or hasRole('ADMIN')")
    @PutMapping("/{id}/seen")
    public ResponseEntity<Void> markNotificationAsSeen(@PathVariable Long id) {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            notificationService.markNotificationAsSeen(id, user);
            return ResponseEntity.ok().build();
    }

    @Operation(summary = "Mark all notifications as seen", description = "Mark all notifications for the current user as seen")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER') or hasRole('ADMIN')")
    @PutMapping("/mark-all-seen")
    public ResponseEntity<Void> markAllNotificationsAsSeen() {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            notificationService.markAllNotificationsAsSeen(user);
            return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete notification", description = "Delete a specific notification by ID")
    @PreAuthorize("hasRole('USER') or hasRole('DRIVER') or hasRole('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            notificationService.deleteNotification(id, user);
            return ResponseEntity.ok().build();
    }
}
