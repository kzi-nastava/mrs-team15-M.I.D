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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RideReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RideReminderScheduler.class);

    private final RideRepository rideRepository;
    private final NotificationService notificationService;
    
    // Track which reminders have been sent for each ride (15min, 10min, 5min)
    private final Map<Long, Set<Integer>> sentReminders = new HashMap<>();

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
            
            logger.info("[SCHEDULER] Running at {}, looking for rides between {} and {}", now, now, fifteenMinutesFromNow);
            
            // Find scheduled rides that are within 15 minutes
            List<Ride> upcomingRides = rideRepository.findUpcomingScheduledRides(now, fifteenMinutesFromNow);

            logger.info("[SCHEDULER] Found {} upcoming scheduled rides", upcomingRides.size());

            for (Ride ride : upcomingRides) {
                long minutesUntilRide = java.time.Duration.between(now, ride.getScheduledTime()).toMinutes();
                
                logger.debug("Ride {} scheduled at {}, minutes until ride: {}", 
                    ride.getId(), ride.getScheduledTime(), minutesUntilRide);
                
                // Get or create the set of sent reminders for this ride
                Set<Integer> sent = sentReminders.computeIfAbsent(ride.getId(), k -> new HashSet<>());
                
                // Send 5-minute reminder (check first, highest priority)
                if (minutesUntilRide <= 5 && !sent.contains(5)) {
                    sendRemindersToPassengers(ride);
                    sent.add(5);
                    logger.info("Sent 5-minute reminder for ride {} scheduled at {}", ride.getId(), ride.getScheduledTime());
                }
                // Send 10-minute reminder (only if 5-min not due yet)
                else if (minutesUntilRide <= 10 && minutesUntilRide > 5 && !sent.contains(10)) {
                    sendRemindersToPassengers(ride);
                    sent.add(10);
                    logger.info("Sent 10-minute reminder for ride {} scheduled at {}", ride.getId(), ride.getScheduledTime());
                }
                // Send 15-minute reminder (only if 10-min not due yet)
                else if (minutesUntilRide <= 15 && minutesUntilRide > 10 && !sent.contains(15)) {
                    sendRemindersToPassengers(ride);
                    sent.add(15);
                    logger.info("Sent 15-minute reminder for ride {} scheduled at {}", ride.getId(), ride.getScheduledTime());
                }
            }
            
            // Clean up old entries from the tracking map
            cleanupOldReminders();
            
        } catch (Exception e) {
            logger.error("Error in ride reminder scheduler: {}", e.getMessage(), e);
        }
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

    private void cleanupOldReminders() {
        // Remove entries for rides that are more than 1 hour old
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
        
        // Check all rides in the map, fetch their scheduled time, and remove if too old
        sentReminders.entrySet().removeIf(entry -> {
            try {
                Long rideId = entry.getKey();
                Ride ride = rideRepository.findById(rideId).orElse(null);
                
                // Remove if ride doesn't exist or scheduled time has passed by more than 1 hour
                if (ride == null || ride.getScheduledTime() == null) {
                    return true;
                }
                
                return ride.getScheduledTime().isBefore(cutoffTime);
            } catch (Exception e) {
                logger.debug("Error checking ride {} for cleanup, removing from map", entry.getKey());
                return true;
            }
        });
    }
}
