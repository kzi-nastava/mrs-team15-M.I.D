package rs.ac.uns.ftn.asd.ridenow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.asd.ridenow.model.Passenger;
import rs.ac.uns.ftn.asd.ridenow.model.RegisteredUser;
import rs.ac.uns.ftn.asd.ridenow.model.Ride;
import rs.ac.uns.ftn.asd.ridenow.model.enums.NotificationType;
import rs.ac.uns.ftn.asd.ridenow.repository.NotificationRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.RideRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RideReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RideReminderScheduler.class);

    private final RideRepository rideRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    public RideReminderScheduler(RideRepository rideRepository, NotificationService notificationService, NotificationRepository notificationRepository) {
        this.rideRepository = rideRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
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
    @Transactional
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
                
                // Send 5-minute reminder (check first, highest priority)
                if (minutesUntilRide <= 5 && !isReminderAlreadySent(ride.getId(), 5)) {
                    sendRemindersToPassengers(ride, 5);
                    logger.info("Sent 5-minute reminder for ride {} scheduled at {}", ride.getId(), ride.getScheduledTime());
                }
                // Send 10-minute reminder (only if 5-min not due yet)
                else if (minutesUntilRide <= 10 && minutesUntilRide > 5 && !isReminderAlreadySent(ride.getId(), 10)) {
                    sendRemindersToPassengers(ride, 10);
                    logger.info("Sent 10-minute reminder for ride {} scheduled at {}", ride.getId(), ride.getScheduledTime());
                }
                // Send 15-minute reminder (only if 10-min not due yet)
                else if (minutesUntilRide <= 15 && minutesUntilRide > 10 && !isReminderAlreadySent(ride.getId(), 15)) {
                    sendRemindersToPassengers(ride, 15);
                    logger.info("Sent 15-minute reminder for ride {} scheduled at {}", ride.getId(), ride.getScheduledTime());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error in ride reminder scheduler: {}", e.getMessage(), e);
        }
    }

    private void sendRemindersToPassengers(Ride ride, int minutesUntilRide) {
        try {
            for (Passenger passenger : ride.getPassengers()) {
                if (passenger.getUser() instanceof RegisteredUser) {
                    RegisteredUser registeredUser = (RegisteredUser) passenger.getUser();
                    notificationService.createScheduledRideReminderNotification(registeredUser, ride, minutesUntilRide);
                }
            }
        } catch (Exception e) {
            logger.error("Error sending reminders for ride {}: {}", ride.getId(), e.getMessage(), e);
        }
    }

    /**
     * Check if a reminder was already sent for this ride at the specified time.
     */
    private boolean isReminderAlreadySent(Long rideId, int minutes) {
        try {
            String minutePattern = "in " + minutes + " minutes";
            return notificationRepository.existsByRideIdAndTypeAndMinutes(
                rideId, 
                NotificationType.SCHEDULED_RIDE_REMINDER, 
                minutePattern
            );
        } catch (Exception e) {
            logger.error("Error checking if reminder was sent for ride {} at {} minutes: {}", 
                rideId, minutes, e.getMessage());
            return false;
        }
    }
}
