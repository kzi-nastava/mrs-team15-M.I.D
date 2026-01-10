package rs.ac.uns.ftn.asd.ridenow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.ftn.asd.ridenow.model.enums.NotificationType;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

@Setter
@Getter
@Entity
public class PriceConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    @Column(nullable = false)
    private double basePrice;

    @Column(nullable = false)
    private double pricePerKm;

    public PriceConfig(VehicleType vehicleType, double basePrice, double pricePerKm) {
        this.vehicleType = vehicleType;
        this.basePrice = basePrice;
        this.pricePerKm = pricePerKm;
    }

    public PriceConfig() {
    }
}