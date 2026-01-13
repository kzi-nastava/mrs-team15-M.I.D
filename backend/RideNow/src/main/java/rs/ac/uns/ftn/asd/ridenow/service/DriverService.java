package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.DriverHistoryItemDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RouteDTO;
import rs.ac.uns.ftn.asd.ridenow.model.*;
import rs.ac.uns.ftn.asd.ridenow.repository.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class DriverService {
    private final RideRepository rideRepository;
    private final PanicAlertRepository panicAlertRepository;
    private final RatingRepository ratingRepository;
    private final DriverRepository driverRepository;

    public DriverService(RideRepository rideRepository, PanicAlertRepository panicAlertRepository,
                         RatingRepository ratingRepository, DriverRepository driverRepository) {
        this.rideRepository = rideRepository;
        this.panicAlertRepository = panicAlertRepository;
        this.ratingRepository = ratingRepository;
        this.driverRepository = driverRepository;
    }

    private RouteDTO mapRouteToDTO(Route route) {
        return new RouteDTO(
                route.getId(),
                route.getDistanceKm(),
                route.getStartLocation(),
                route.getEndLocation()
        );
    }

    public List<DriverHistoryItemDTO> getDriverHistory(Long driverId) {
        List<DriverHistoryItemDTO> driverHistory = new ArrayList<>();

        Driver driver = driverRepository.getReferenceById(driverId);

        List<Ride> driverRides = rideRepository.findByDriver(driver);
        for (Ride ride : driverRides) {
            DriverHistoryItemDTO dto = new DriverHistoryItemDTO();
            dto.setRoute(mapRouteToDTO(ride.getRoute()));
            dto.setDate(ride.getScheduledTime().toLocalDate());
            dto.setDurationMinutes(ride.getRoute().getEstimatedTimeMin());
            dto.setCost(ride.getPrice());
            List<String> passengerNames = new ArrayList<>();
            for (Passenger p : ride.getPassengers()) {
                passengerNames.add(p == null ? null : p.getUser().getFirstName() + " " + p.getUser().getLastName());
            }
            dto.setPassengers(passengerNames);

            List<PanicAlert> alerts = panicAlertRepository.findByRide(ride);

            if (!alerts.isEmpty()) {
                dto.setPanic(true);
                dto.setPanicBy("Test Whatever");
            }

            dto.setCancelled(ride.getCancelled());

            Rating rating = ratingRepository.findByRide(ride);

            if (rating != null) {
                Double rate = (double) (rating.getDriverRating() + rating.getVehicleRating()) / 2;
                dto.setRating(rate);
            }

            List<Inconsistency> inconsistencies = ride.getInconsistencies();
            List<String> inconsistencyStrings = new ArrayList<>();
            if (!inconsistencies.isEmpty()) {
                for  (Inconsistency inconsistency : inconsistencies) {
                    inconsistencyStrings.add(inconsistency.getDescription());
                }
            }
            dto.setInconsistencies(inconsistencyStrings);

            driverHistory.add(dto);
        }
        return driverHistory;
    }
}
