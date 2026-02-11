package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "\"user\"")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Size(min = 6)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String address;

    private String profileImage;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean blocked = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRoles role = UserRoles.USER;;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<Notification> notifications = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "activation_token_id")
    private ActivationToken activationToken;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "forgot_password_token_id")
    private ForgotPasswordToken forgotPasswordToken;

    @Column(nullable = false)
    boolean jwtTokenValid;

    public User(String email, String password, String firstName, String lastName, String phoneNumber, String address,
                String profileImage, boolean active, boolean blocked, UserRoles role, boolean jwtTokenValid) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.profileImage = profileImage;
        this.active = active;
        this.blocked = blocked;
        this.role = role;
        this.jwtTokenValid = jwtTokenValid;
    }

    public User(){
        super();
    }

    public void addNotification(Notification notification){
        if(notification != null && !notifications.contains(notification)){
            notifications.add(notification);
            notification.assignUser(this);
        }
    }
}