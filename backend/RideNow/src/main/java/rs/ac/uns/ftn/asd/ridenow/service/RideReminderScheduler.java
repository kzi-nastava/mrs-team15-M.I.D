package rs.ac.uns.ftn.asd.ridenow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.model.Passenger;
import rs.ac.uns.ftn.asd.ridenow.model.RegisteredUser;
import rs.ac.uns.ftn.asd.ridenow.model.Ride;
import rs.ac.uns.ftn.asd.ridenow.model.enums.PassengerRole;
import rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus;
import rs.ac.uns.ftn.asd.ridenow.repository.RideRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RideReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RideReminderScheduler.class);

    private final RideRepository rideRepository;
    private final NotificationService notificationService;
    
    // Track when we last sent a reminder for each ride to avoid duplicates
    private final Map<Long, LocalDateTime> lastReminderSent = new HashMap<>();

    public RideReminderScheduler(RideRepository rideRepository, NotificationService notificationService) {
        this.rideRepository = rideRepository;
        this.notificationService = notificationService;
    }

    /**
     * Runs every minute to check for scheduled rides that need reminders.
     * Sends reminders at:
     * - 15 minutes before scheduled time
     * - 10 minutes before scheduled time
     * - 5 minutes before scheduled time
     * 
     * Stops sending reminders once the ride starts.
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void checkAndSendRideReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime fifteenMinutesFromNow = now.plusMinutes(15);
            LocalDateTime oneMinuteAgo = now.minusMinutes(1);
            
            // Find scheduled rides that are within 15 minutes
            List<Ride> upcomingRides = rideRepository.findUpcomingScheduledRides(now, fifteenMinutesFromNow);

            for (Ride ride : upcomingRides) {
                long minutesUntilRide = java.time.Duration.between(now, ride.getScheduledTime()).toMinutes();
                
                // Send reminders at specific intervals: 15, 10, 5 minutes
                if (shouldSendReminder(ride, minutesUntilRide, now)) {
                    sendRemindersToPassengers(ride);
                    lastReminderSent.put(ride.getId(), now);
                    logger.info("Sent reminders for ride {} scheduled at {}", ride.getId(), ride.getScheduledTime());
                }
            }
            
            // Clean up old entries from the tracking map
            cleanupOldReminders(oneMinuteAgo);
            
        } catch (Exception e) {
            logger.error("Error in ride reminder scheduler: {}", e.getMessage(), e);
        }
    }

    private boolean shouldSendReminder(Ride ride, long minutesUntilRide, LocalDateTime now) {
        // Check if we should send reminder at 15, 10, or 5 minutes before
        boolean isReminderTime = (minutesUntilRide <= 15 && minutesUntilRide >= 14) ||
                                  (minutesUntilRide <= 10 && minutesUntilRide >= 9) ||
                                  (minutesUntilRide <= 5 && minutesUntilRide >= 4);
        
        if (!isReminderTime) {
            return false;
        }
        
        // Check if we already sent a reminder recently (within last 2 minutes)
        LocalDateTime lastSent = lastReminderSent.get(ride.getId());
        if (lastSent != null) {
            long minutesSinceLastReminder = java.time.Duration.between(lastSent, now).toMinutes();
            return minutesSinceLastReminder >= 2; // Only send if at least 2 minutes passed
        }
        
        return true;
    }

    private void sendRemindersToPassengers(Ride ride) {
        try {
            for (Passenger passenger : ride.getPassengers()) {
                if (passenger.getUser() instanceof RegisteredUser) {
                    RegisteredUser registeredUser = (RegisteredUser) passenger.getUser();
                    notificationService.createScheduledRideReminderNotification(registeredUser, ride);
                }
            }
        } catch (Exception e) {
            logger.error("Error sending reminders for ride {}: {}", ride.getId(), e.getMessage(), e);
        }
    }

    private void cleanupOldReminders(LocalDateTime cutoffTime) {
        // Remove entries older than the cutoff time to prevent memory leak
        lastReminderSent.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoffTime.minusHours(1)));
    }
}
