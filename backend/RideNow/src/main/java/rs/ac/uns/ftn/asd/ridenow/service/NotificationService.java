package rs.ac.uns.ftn.asd.ridenow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.asd.ridenow.dto.notification.NotificationResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.*;
import rs.ac.uns.ftn.asd.ridenow.model.enums.NotificationType;
import rs.ac.uns.ftn.asd.ridenow.repository.NotificationRepository;
import rs.ac.uns.ftn.asd.ridenow.websocket.NotificationWebSocketHandler;
import jakarta.persistence.EntityNotFoundException;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationWebSocketHandler webSocketHandler;

    public NotificationService(NotificationRepository notificationRepository,
                              NotificationWebSocketHandler webSocketHandler) {
        this.notificationRepository = notificationRepository;
        this.webSocketHandler = webSocketHandler;
    }

    public List<NotificationResponseDTO> getUserNotifications(User user) {
        try {
            return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                    .stream()
                    .map(NotificationResponseDTO::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting user notifications for user {}: {}", user.getId(), e.getMessage());
            return List.of(); // Return empty list instead of throwing exception
        }
    }

    public List<NotificationResponseDTO> getUnseenNotifications(User user) {
        try {
            return notificationRepository.findByUserAndSeenOrderByCreatedAtDesc(user, false)
                    .stream()
                    .map(NotificationResponseDTO::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting unseen notifications for user {}: {}", user.getId(), e.getMessage());
            return List.of();
        }
    }

    public long getUnseenNotificationCount(User user) {
        try {
            return notificationRepository.countUnseenByUser(user);
        } catch (Exception e) {
            logger.error("Error getting unseen notification count for user {}: {}", user.getId(), e.getMessage());
            return 0L;
        }
    }

    @Transactional
    public void markNotificationAsSeen(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Access denied - notification belongs to different user");
        }

        notification.setSeen(true);
        notificationRepository.save(notification);
        logger.debug("Marked notification {} as seen for user {}", notificationId, user.getId());
    }

    @Transactional
    public void markAllNotificationsAsSeen(User user) {
        try {
            notificationRepository.markAllAsSeenByUser(user);
            logger.debug("Marked all notifications as seen for user {}", user.getId());
        } catch (Exception e) {
            logger.error("Error marking all notifications as seen for user {}: {}", user.getId(), e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void deleteNotification(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Access denied - notification belongs to different user");
        }

        notificationRepository.delete(notification);
        logger.debug("Deleted notification {} for user {}", notificationId, user.getId());
    }

    // Create and send notifications
    public void createAndSendPassengerAddedNotification(RegisteredUser passenger, Ride ride) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
            String rideTime = ride.getScheduledTime().format(formatter);

            String message = String.format("You have been added as a passenger to a ride scheduled for %s", rideTime);

            Notification notification = new Notification();
            notification.setUser(passenger);
            notification.setMessage(message);
            notification.setType(NotificationType.ADDED_AS_PASSENGER);
            notification.setRelatedEntityId(ride.getId());
            notification.setSeen(false);

            notification = notificationRepository.save(notification);
            logger.info("Created passenger added notification for user {}", passenger.getId());

            // Send real-time notification via WebSocket
            NotificationResponseDTO dto = new NotificationResponseDTO(notification);
            webSocketHandler.broadcastToUser(passenger.getId(), "NEW_NOTIFICATION", dto);
        } catch (Exception e) {
            logger.error("Error creating passenger added notification: {}", e.getMessage(), e);
        }
    }

    public void createRideAssignedNotification(User driver, Ride ride) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
            String rideTime = ride.getScheduledTime().format(formatter);

            String message = String.format("You have been assigned a new ride scheduled for %s", rideTime);

            Notification notification = new Notification();
            notification.setUser(driver);
            notification.setMessage(message);
            notification.setType(NotificationType.RIDE_ASSIGNED);
            notification.setRelatedEntityId(ride.getId());
            notification.setSeen(false);

            notification = notificationRepository.save(notification);
            logger.info("Created ride assigned notification for driver {}", driver.getId());

            // Send real-time notification via WebSocket
            NotificationResponseDTO dto = new NotificationResponseDTO(notification);
            webSocketHandler.broadcastToUser(driver.getId(), "NEW_NOTIFICATION", dto);
        } catch (Exception e) {
            logger.error("Error creating ride assigned notification: {}", e.getMessage(), e);
        }
    }

    public void createRideStartedNotification(RegisteredUser passenger, Ride ride) {
        try {
            String message = "Your ride has started. Have a safe journey!";

            Notification notification = new Notification();
            notification.setUser(passenger);
            notification.setMessage(message);
            notification.setType(NotificationType.RIDE_STARTED);
            notification.setRelatedEntityId(ride.getId()); // Set ride ID so it can be deleted later
            notification.setSeen(false);

            notification = notificationRepository.save(notification);
            logger.info("Created ride started notification for passenger {} for ride {}", passenger.getId(), ride.getId());

            // Send real-time notification via WebSocket
            NotificationResponseDTO dto = new NotificationResponseDTO(notification);
            webSocketHandler.broadcastToUser(passenger.getId(), "NEW_NOTIFICATION", dto);
        } catch (Exception e) {
            logger.error("Error creating ride started notification: {}", e.getMessage(), e);
        }
    }

    public void createRideFinishedNotification(RegisteredUser passenger, Ride ride, boolean isCreator) {
        try {
            String message;
            if (isCreator) {
                message = "Your ride has been completed. Please rate your experience to help us improve our service.";
            } else {
                message = "Your ride has been completed. Thank you for choosing RideNow!";
            }

            Notification notification = new Notification();
            notification.setUser(passenger);
            notification.setMessage(message);
            notification.setType(NotificationType.RIDE_FINISHED);
            if (isCreator) {
                notification.setRelatedEntityId(ride.getId()); // Only set ride ID for creator so they can access rating page
            }
            notification.setSeen(false);

            notification = notificationRepository.save(notification);
            logger.info("Created ride finished notification for passenger {} for ride {} (isCreator: {})",
                passenger.getId(), ride.getId(), isCreator);

            // Send real-time notification via WebSocket
            NotificationResponseDTO dto = new NotificationResponseDTO(notification);
            webSocketHandler.broadcastToUser(passenger.getId(), "NEW_NOTIFICATION", dto);
        } catch (Exception e) {
            logger.error("Error creating ride finished notification: {}", e.getMessage(), e);
        }
    }


    @Transactional
    public void deleteRideRelatedNotifications(Long rideId) {
        try {
            // Delete old notifications (but keep RIDE_FINISHED so users can access rating page)
            List<NotificationType> typesToDelete = List.of(
                NotificationType.ADDED_AS_PASSENGER,
                NotificationType.RIDE_ASSIGNED,
                NotificationType.RIDE_STARTED
                // Note: RIDE_FINISHED is not deleted so users can still access the rating page
            );

            // First, delete notifications that have the ride ID set
            notificationRepository.deleteByRelatedEntityIdAndTypes(rideId, typesToDelete);
            logger.info("Deleted ride-related notifications with relatedEntityId for ride {}", rideId);

        } catch (Exception e) {
            logger.error("Error deleting ride-related notifications for ride {}: {}", rideId, e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteRideRelatedNotificationsForPassengers(List<RegisteredUser> passengers, Long rideId) {
        try {
            // Delete old notifications for specific passengers (fallback for notifications without relatedEntityId)
            List<NotificationType> typesToDelete = List.of(
                NotificationType.ADDED_AS_PASSENGER,
                NotificationType.RIDE_STARTED
            );

            List<User> users = passengers.stream().map(p -> (User) p).collect(Collectors.toList());
            notificationRepository.deleteByUsersAndTypes(users, typesToDelete);
            logger.info("Deleted ride-related notifications for {} passengers of ride {}", passengers.size(), rideId);

        } catch (Exception e) {
            logger.error("Error deleting ride-related notifications for passengers of ride {}: {}", rideId, e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteRideFinishedNotifications(Long rideId) {
        try {
            // Delete RIDE_FINISHED notifications for this ride (when someone rates the ride)
            notificationRepository.deleteByRelatedEntityIdAndType(rideId, NotificationType.RIDE_FINISHED);
            logger.info("Deleted RIDE_FINISHED notifications for ride {}", rideId);
        } catch (Exception e) {
            logger.error("Error deleting RIDE_FINISHED notifications for ride {}: {}", rideId, e.getMessage(), e);
        }
    }

    public void createNoDriversAvailableNotification(RegisteredUser passenger) {
        try {
            String message = "Unfortunately, there are currently no active drivers available. Please try again later.";

            Notification notification = new Notification();
            notification.setUser(passenger);
            notification.setMessage(message);
            notification.setType(NotificationType.NO_DRIVERS_AVAILABLE);
            notification.setSeen(false);

            notification = notificationRepository.save(notification);
            logger.info("Created no drivers available notification for user {}", passenger.getId());

            // Send real-time notification via WebSocket
            NotificationResponseDTO dto = new NotificationResponseDTO(notification);
            webSocketHandler.broadcastToUser(passenger.getId(), "NEW_NOTIFICATION", dto);
        } catch (Exception e) {
            logger.error("Error creating no drivers available notification: {}", e.getMessage(), e);
        }
    }

    public void createRideRequestRejectedNotification(RegisteredUser passenger) {
        try {
            String message = "Your ride request could not be processed at this time. All drivers are currently busy. Please try again later.";

            Notification notification = new Notification();
            notification.setUser(passenger);
            notification.setMessage(message);
            notification.setType(NotificationType.RIDE_REQUEST_REJECTED);
            notification.setSeen(false);

            notification = notificationRepository.save(notification);
            logger.info("Created ride request rejected notification for user {}", passenger.getId());

            // Send real-time notification via WebSocket
            NotificationResponseDTO dto = new NotificationResponseDTO(notification);
            webSocketHandler.broadcastToUser(passenger.getId(), "NEW_NOTIFICATION", dto);
        } catch (Exception e) {
            logger.error("Error creating ride request rejected notification: {}", e.getMessage(), e);
        }
    }

    public void createRideRequestAcceptedNotification(RegisteredUser passenger, Ride ride) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
            String rideTime = ride.getScheduledTime().format(formatter);

            String message = String.format("Your ride request has been accepted! Your ride is scheduled for %s", rideTime);

            Notification notification = new Notification();
            notification.setUser(passenger);
            notification.setMessage(message);
            notification.setType(NotificationType.RIDE_REQUEST_ACCEPTED);
            notification.setRelatedEntityId(ride.getId());
            notification.setSeen(false);

            notification = notificationRepository.save(notification);
            logger.info("Created ride request accepted notification for user {}", passenger.getId());

            // Send real-time notification via WebSocket
            NotificationResponseDTO dto = new NotificationResponseDTO(notification);
            webSocketHandler.broadcastToUser(passenger.getId(), "NEW_NOTIFICATION", dto);
        } catch (Exception e) {
            logger.error("Error creating ride request accepted notification: {}", e.getMessage(), e);
        }
    }

    public void createScheduledRideReminderNotification(RegisteredUser passenger, Ride ride) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
            String rideTime = ride.getScheduledTime().format(formatter);
            
            String startAddress = ride.getRoute().getStartLocation().getAddress();
            String endAddress = ride.getRoute().getEndLocation().getAddress();

            String message = String.format("Reminder: Your ride from %s to %s is scheduled for %s", 
                startAddress, endAddress, rideTime);

            Notification notification = new Notification();
            notification.setUser(passenger);
            notification.setMessage(message);
            notification.setType(NotificationType.SCHEDULED_RIDE_REMINDER);
            notification.setRelatedEntityId(ride.getId());
            notification.setSeen(false);

            notification = notificationRepository.save(notification);
            logger.info("Created scheduled ride reminder notification for user {} for ride {}", passenger.getId(), ride.getId());

            // Send real-time notification via WebSocket
            NotificationResponseDTO dto = new NotificationResponseDTO(notification);
            webSocketHandler.broadcastToUser(passenger.getId(), "NEW_NOTIFICATION", dto);
        } catch (Exception e) {
            logger.error("Error creating scheduled ride reminder notification: {}", e.getMessage(), e);
        }
    }
}
