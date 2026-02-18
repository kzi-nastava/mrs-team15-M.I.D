package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.model.ActivationToken;
import rs.ac.uns.ftn.asd.ridenow.model.ForgotPasswordToken;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    private static final String FRONTEND_URL = "http://localhost:4200";
    private static String activationRoute = FRONTEND_URL + "/activate/";
    private static String driverActivationRoute = FRONTEND_URL + "/driver-activation/";
    private static String resetPasswordRoute = FRONTEND_URL + "/reset-password/";
    private static final String CURRENT_RIDE_URL = FRONTEND_URL + "/current-ride";
    private static final String RATING_URL = FRONTEND_URL + "/rating/";
    private static final String UPCOMING_RIDES_URL = FRONTEND_URL + "/upcoming-rides";
    private static final String CURRENCY = "DIN";

    public void sendActivationMail(String to, ActivationToken token) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject("Activate your RideNow account");
        mail.setText(
                "Welcome to RideNow!\n\n" +
                        "Your account is almost ready.\n" +
                        "Click the link below to activate it:\n\n"
                        + activationRoute + token.getToken() + "\n\n" +
                        "Or use this verification code in the mobile app: " + token.getVerificationCode() + "\n\n" +
                        "\n\nSee you on the road,\nRideNow Team"
        );
        mailSender.send(mail);
    }

    public void sendDriverActivationMail(String to, ActivationToken token) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject("Activate your RideNow account");
        mail.setText(
                "Welcome to RideNow!\n\n" +
                        "Your account is almost ready.\n" +
                        "Click the link below to activate it:\n\n" +
                        driverActivationRoute + token.getToken() +
                        "\n\nSee you on the road,\nRideNow Team"
        );
        mailSender.send(mail);
    }



    public void sendForgotPasswordMail(String to, ForgotPasswordToken token) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject("Reset your RideNow password");
        mail.setText(
                "Hello,\n\n" +
                        "We received a request to reset your RideNow account password.\n\n" +
                        "To create a new password, click the link below:\n\n" +
                        resetPasswordRoute + token.getToken() + "\n\n" +
                        "Or use this verification code in the mobile app: " + token.getVerificationCode() + "\n\n" +
                        "If you did not request a password reset, please ignore this email. " +
                        "Your account will remain secure.\n\n" +
                        "Best regards,\n" +
                        "RideNow Team"
        );
        mailSender.send(mail);
    }

    /**
     * Send a simple email with custom subject and body
     * Used for general notifications (ride notifications, etc.)
     */
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(body);
            mailSender.send(mail);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    /**
     * Send passenger added notification email
     */
    public void sendPassengerAddedEmail(String passengerName, String passengerEmail, String startAddress,
                                        String endAddress, String rideTime, double distanceKm, double price) {
        try {
            String subject = "You've been added to a RideNow ride!";
            String emailBody = String.format(
                "Hello %s,\n\n" +
                "You have been added as a passenger to a RideNow ride!\n\n" +
                "Ride Details:\n" +
                "From: %s\n" +
                "To: %s\n" +
                "Scheduled Time: %s\n" +
                "Distance: %.1f km\n" +
                "Price: %.2f %s\n\n" +
                "Please make sure you are ready for your ride. Your driver will pick you up shortly after the scheduled time.\n\n" +
                "View your upcoming rides: %s\n\n" +
                "If you have any questions or need to cancel, please contact our support team.\n\n" +
                "Safe travels,\n" +
                "RideNow Team",
                passengerName, startAddress, endAddress, rideTime, distanceKm, price, CURRENCY, UPCOMING_RIDES_URL
            );

            sendSimpleEmail(passengerEmail, subject, emailBody);
        } catch (Exception e) {
            System.err.println("Failed to send passenger added email: " + e.getMessage());
        }
    }

    /**
     * Send ride started notification email
     */
    public void sendRideStartedEmail(String passengerName, String passengerEmail, String driverName, String endAddress) {
        try {
            String subject = "Your RideNow ride has started!";
            String emailBody = String.format(
                "Hello %s,\n\n" +
                "Your RideNow ride is on the way!\n\n" +
                "Driver: %s\n" +
                "Destination: %s\n\n" +
                "Your driver is currently en route to your destination. " +
                "You can track your ride in real-time here: %s\n\n" +
                "Have a safe journey!\n\n" +
                "RideNow Team",
                passengerName, driverName, endAddress, CURRENT_RIDE_URL
            );

            sendSimpleEmail(passengerEmail, subject, emailBody);
        } catch (Exception e) {
            System.err.println("Failed to send ride started email: " + e.getMessage());
        }
    }

    /**
     * Send ride finished notification email for ride creator (with rating request)
     */
    public void sendRideFinishedEmailForCreator(String passengerName, String passengerEmail, String startAddress,
                                                String endAddress, double distanceKm, double price, Long rideId) {
        try {
            String subject = "Your RideNow ride is complete!";
            String ratingLink = RATING_URL + rideId;
            String emailBody = String.format(
                "Hello %s,\n\n" +
                "Thank you for riding with RideNow!\n\n" +
                "Ride Summary:\n" +
                "From: %s\n" +
                "To: %s\n" +
                "Distance: %.1f km\n" +
                "Total Cost: %.2f %s\n\n" +
                "We'd love to hear about your experience! Please take a moment to rate your ride: %s\n" +
                "Your feedback helps us improve our service and assist our drivers.\n\n" +
                "Safe travels,\n" +
                "RideNow Team",
                passengerName, startAddress, endAddress, distanceKm, price, CURRENCY, ratingLink
            );

            sendSimpleEmail(passengerEmail, subject, emailBody);
        } catch (Exception e) {
            System.err.println("Failed to send ride finished email: " + e.getMessage());
        }
    }

    /**
     * Send ride finished notification email for added passengers
     */
    public void sendRideFinishedEmailForPassenger(String passengerName, String passengerEmail, String startAddress,
                                                  String endAddress, double distanceKm, double price) {
        try {
            String subject = "Your RideNow ride is complete!";
            String emailBody = String.format(
                "Hello %s,\n\n" +
                "Thank you for riding with RideNow!\n\n" +
                "Ride Summary:\n" +
                "From: %s\n" +
                "To: %s\n" +
                "Distance: %.1f km\n" +
                "Total Cost: %.2f %s\n\n" +
                "We appreciate your continued trust in RideNow. See you on your next ride!\n\n" +
                "Safe travels,\n" +
                "RideNow Team",
                passengerName, startAddress, endAddress, distanceKm, price, CURRENCY
            );

            sendSimpleEmail(passengerEmail, subject, emailBody);
        } catch (Exception e) {
            System.err.println("Failed to send ride finished email: " + e.getMessage());
        }
    }
}