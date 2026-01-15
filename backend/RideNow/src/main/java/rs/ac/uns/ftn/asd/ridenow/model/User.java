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

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<Message> messages = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "activation_token_id")
    private ActivationToken activationToken;

    public User(String email, String password, String firstName, String lastName, String phoneNumber, String address,
                String profileImage, boolean active, boolean blocked, UserRoles role) {
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

    public void addMessage(Message message) {
        if(message != null && !messages.contains(message)) {
            messages.add(message);
            message.setSender(this);
        }
    }
}