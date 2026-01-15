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

    private String activationRoute = "http://localhost:4200/activate/";
    private String resetPasswordRoute = "http://localhost:4200/reset-password/";

    public void sendActivationMail(String to, ActivationToken token) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject("Activate your RideNow account");
        mail.setText(
                "Welcome to RideNow!\n\n" +
                        "Your account is almost ready.\n" +
                        "Click the link below to activate it:\n\n" +
                        activationRoute + token.getToken() +
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
                        "If you did not request a password reset, please ignore this email. " +
                        "Your account will remain secure.\n\n" +
                        "Best regards,\n" +
                        "RideNow Team"
        );
        mailSender.send(mail);
    }
}