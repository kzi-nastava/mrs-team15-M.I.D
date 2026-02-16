package rs.ac.uns.ftn.asd.ridenow.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.model.Ride;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.model.enums.NotificationType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Firebase Cloud Messaging Service
 *
 * Handles sending push notifications to mobile devices via Firebase Cloud Messaging.
 * This service complements the WebSocket notification system for web and provides
 * reliable push notifications for mobile apps.
 *
 * If Firebase is not initialized (credentials missing), notifications are skipped gracefully.
 */
@Service
public class FcmService {

    private static final Logger logger = LoggerFactory.getLogger(FcmService.class);

    private final FirebaseMessaging firebaseMessaging;

    public FcmService(Optional<FirebaseMessaging> firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging.orElse(null);
        if (this.firebaseMessaging == null) {
            logger.warn("FirebaseMessaging not initialized - FCM notifications will be skipped");
        }
    }

    /**
     * Send a push notification to a mobile device via FCM
     *
     * @param user The recipient user
     * @param title Notification title
     * @param body Notification body
     * @param notificationType Type of notification (for data payload)
     * @param relatedEntityId ID of related entity (e.g., ride ID)
     * @return true if sent successfully, false otherwise
     */
    public boolean sendPushNotification(User user, String title, String body,
                                        NotificationType notificationType, Long relatedEntityId) {
        // Check if Firebase is initialized
        if (firebaseMessaging == null) {
            logger.debug("Firebase not initialized - skipping FCM notification");
            return false;
        }

        if (user == null || user.getFcmDeviceToken() == null || user.getFcmDeviceToken().isEmpty()) {
            logger.debug("User {} does not have a valid FCM token", user != null ? user.getId() : "null");
            return false;
        }

        try {
            // Build data payload
            Map<String, String> data = new HashMap<>();
            data.put("type", notificationType.name());
            if (relatedEntityId != null) {
                data.put("relatedEntityId", relatedEntityId.toString());
            }

            // Build notification
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // Build message
            Message message = Message.builder()
                    .setNotification(notification)
                    .putAllData(data)
                    .setToken(user.getFcmDeviceToken())
                    .build();

            // Send message
            String messageId = firebaseMessaging.send(message);
            logger.info("Push notification sent successfully to user {} with messageId: {}", user.getId(), messageId);
            return true;

        } catch (FirebaseMessagingException e) {
            logger.error("Error sending push notification to user {}: {}", user.getId(), e.getMessage());
            // If token is invalid, it should be cleared and user should re-register
            if (e.getMessage().contains("Invalid registration token")) {
                clearInvalidToken(user);
            }
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error sending push notification to user {}: {}", user.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send passenger added notification
     */
    public boolean sendPassengerAddedNotification(User passenger, Ride ride) {
        String title = "Added to Ride";
        String body = String.format("You've been added to a ride scheduled for %s",
            ride.getScheduledTime().toLocalTime());
        return sendPushNotification(passenger, title, body,
            NotificationType.ADDED_AS_PASSENGER, ride.getId());
    }

    /**
     * Send ride assigned notification
     */
    public boolean sendRideAssignedNotification(User driver, Ride ride) {
        String title = "New Ride Assigned";
        String body = String.format("You have been assigned a new ride scheduled for %s",
            ride.getScheduledTime().toLocalTime());
        return sendPushNotification(driver, title, body,
            NotificationType.RIDE_ASSIGNED, ride.getId());
    }

    /**
     * Send ride started notification
     */
    public boolean sendRideStartedNotification(User passenger, Ride ride) {
        String title = "Ride Started";
        String body = "Your ride has started. Have a safe journey!";
        return sendPushNotification(passenger, title, body,
            NotificationType.RIDE_STARTED, ride.getId());
    }

    /**
     * Send ride finished notification
     */
    public boolean sendRideFinishedNotification(User passenger, Ride ride, boolean isCreator) {
        String title = "Ride Completed";
        String body;
        if (isCreator) {
            body = "Your ride is complete. Please rate your experience!";
        } else {
            body = "Your ride is complete. Thank you for choosing RideNow!";
        }
        return sendPushNotification(passenger, title, body,
            NotificationType.RIDE_FINISHED, ride.getId());
    }

    /**
     * Send panic alert notification
     */
    public boolean sendPanicAlertNotification(User admin, Ride ride, String panicInitiator) {
        String title = "PANIC ALERT";
        String body = String.format("Panic alert triggered by %s in ride ID: %d",
            panicInitiator, ride.getId());
        return sendPushNotification(admin, title, body,
            NotificationType.PANIC, ride.getId());
    }

    /**
     * Clear invalid FCM token from user
     * This is called when FCM returns an "Invalid registration token" error
     */
    private void clearInvalidToken(User user) {
        user.setFcmDeviceToken(null);
        logger.warn("Cleared invalid FCM token for user {}", user.getId());
    }

    /**
     * Check if Firebase is properly initialized
     */
    public boolean isInitialized() {
        return firebaseMessaging != null;
    }
}

