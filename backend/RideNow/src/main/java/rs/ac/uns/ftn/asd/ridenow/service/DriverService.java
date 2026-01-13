package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.DriverHistoryItemDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RatingDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RouteDTO;
import rs.ac.uns.ftn.asd.ridenow.model.*;
import rs.ac.uns.ftn.asd.ridenow.repository.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class DriverService {
    private final RideRepository rideRepository;
    private final RatingRepository ratingRepository;
    private final DriverRepository driverRepository;

    public DriverService(RideRepository rideRepository,
                         RatingRepository ratingRepository, DriverRepository driverRepository) {
        this.rideRepository = rideRepository;
        this.ratingRepository = ratingRepository;
        this.driverRepository = driverRepository;
    }

    private RouteDTO mapRouteToDTO(Route route) {
        return new RouteDTO(
                route.getId(),
                route.getDistanceKm(),
                route.getStartLocation(),
                route.getEndLocation(),
                route.getStopLocations()
        );
    }

    private RatingDTO mapRatingToDTO(Rating rating) {
        return new RatingDTO(
                rating.getDriverRating(),
                rating.getVehicleRating(),
                rating.getDriverComment(),
                rating.getVehicleComment()
        );
    }

    public List<DriverHistoryItemDTO> getDriverHistory(Long driverId) {
        List<DriverHistoryItemDTO> driverHistory = new ArrayList<>();

        Driver driver = driverRepository.getReferenceById(driverId);

        List<Ride> driverRides = rideRepository.findByDriverWithAllRelations(driver);
        for (Ride ride : driverRides) {
            DriverHistoryItemDTO dto = new DriverHistoryItemDTO();
            dto.setRoute(mapRouteToDTO(ride.getRoute()));
            dto.setDate(ride.getScheduledTime().toLocalDate());
            if (ride.getStartTime() != null &&  ride.getEndTime() != null) {
                dto.setDurationMinutes((double) Duration.between(ride.getStartTime(), ride.getEndTime()).toSeconds() / 60);
            } else {
                dto.setDurationMinutes(0.0);
            }
            dto.setCost(ride.getPrice());

            List<String> passengerNames = new ArrayList<>();
            for (Passenger p : ride.getPassengers()) {
                passengerNames.add(p == null ? null : p.getUser().getFirstName() + " " + p.getUser().getLastName());
            }
            dto.setPassengers(passengerNames);

            if (ride.getPanicAlert() != null) {
                dto.setPanic(true);
                if (ride.getPanicAlert().getPanicBy() != null) {
                    dto.setPanicBy(ride.getPanicAlert().getPanicBy());
                }
            }

            dto.setCancelled(ride.getCancelled());
            if(ride.getCancelled()) {
                dto.setCancelledBy(ride.getCancelledBy());
            }

            if (ratingRepository.findByRide(ride)!= null) {
                dto.setRating(mapRatingToDTO(ratingRepository.findByRide(ride)));
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
