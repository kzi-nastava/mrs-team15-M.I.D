package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class ForgotPasswordToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private String verificationCode;

    @OneToOne(mappedBy = "forgotPasswordToken")
    private User user;

    public ForgotPasswordToken(String token, LocalDateTime expiresAt, User user, String verificationCode) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.user = user;
        this.verificationCode = verificationCode;
    }

    public ForgotPasswordToken() {
    }
}