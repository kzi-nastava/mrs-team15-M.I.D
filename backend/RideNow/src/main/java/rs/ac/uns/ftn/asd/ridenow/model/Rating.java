package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private  int driverRating = 0;

    @Column(nullable = false)
    private  int vehicleRating = 0;

    @Lob
    private String comment;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToOne(optional = false)
    @JoinColumn(name = "ride_id", nullable = false, unique = true)
    private Ride ride;

    public Rating(int driverRating, int vehicleRating, String comment) {
        this.driverRating = driverRating;
        this.vehicleRating = vehicleRating;
        this.comment = comment;
    }

    public Rating() {
    }
}
