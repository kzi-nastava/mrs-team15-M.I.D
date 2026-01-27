package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.model.PriceConfig;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;
import rs.ac.uns.ftn.asd.ridenow.repository.PriceRepository;

import java.util.Optional;

@Service
public class PriceService {

    @Autowired
    private PriceRepository priceRepository;

    public double calculatePrice(VehicleType type, double distanceKm) {
        Optional<PriceConfig> optionalPriceConfig = priceRepository.findByVehicleType(type);
        if(optionalPriceConfig.isEmpty()){
            return 0;
        }
        PriceConfig priceConfig = optionalPriceConfig.get();
        return priceConfig.getBasePrice() + priceConfig.getPricePerKm() * distanceKm;
    }
}
