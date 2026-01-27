package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverChangesStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

import java.sql.Date;
@Getter
@Setter
@Entity
public class DriverRequest{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Date submissionDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DriverChangesStatus requestStatus;

    private String message;


    @Column(nullable = false)
    private Long driverId;

    private String email;

    private String password;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String address;

    private String profileImage;

    @Column(nullable = false)
    private Long vehicleId;

    private String licensePlate;

    private String vehicleModel;

    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    @Min(1)
    private int numberOfSeats;

    private boolean babyFriendly;

    private boolean petFriendly;

    private Date adminResponseDate;

    public DriverRequest() {}

    public DriverRequest(Date submissionDate,
                         DriverChangesStatus requestStatus,
                         Long driverId,
                         String email,
                         String password,
                         String firstName,
                         String lastName,
                         String phoneNumber,
                         String address,
                         String profileImage,
                         Long vehicleId,
                         String licensePlate,
                         String vehicleModel,
                         VehicleType vehicleType,
                         int numberOfSeats,
                         boolean babyFriendly,
                         boolean petFriendly,
                         Date adminResponseDate) {
        this.submissionDate = submissionDate;
        this.requestStatus = requestStatus;
        this.driverId = driverId;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.profileImage = profileImage;
        this.vehicleId = vehicleId;
        this.licensePlate = licensePlate;
        this.vehicleModel = vehicleModel;
        this.vehicleType = vehicleType;
        this.numberOfSeats = numberOfSeats;
        this.babyFriendly = babyFriendly;
        this.petFriendly = petFriendly;
        this.adminResponseDate = adminResponseDate;
    }



}
