package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
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

    public User(){
        super();
    }

    public User(String email, String password, String firstName, String lastName, String phoneNumber, String address,
                String profileImage, boolean active, boolean blocked) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.profileImage = profileImage;
        this.active = active;
        this.blocked = blocked;
    }
}