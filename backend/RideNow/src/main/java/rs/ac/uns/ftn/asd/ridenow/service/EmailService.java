package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.model.ActivationToken;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    private String activationRoute = "http://localhost:4200/activate/";

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
}