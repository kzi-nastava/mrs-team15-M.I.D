package rs.ac.uns.ftn.asd.ridenow.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.DriverChangeResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.DriverHistoryItemDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RatingDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RouteDTO;
import rs.ac.uns.ftn.asd.ridenow.model.*;
import rs.ac.uns.ftn.asd.ridenow.repository.*;

import java.sql.Date;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class DriverService {
    private final RideRepository rideRepository;
    private final RatingRepository ratingRepository;
    private final DriverRepository driverRepository;
    private final DriverRequestRepository driverRequestRepository;

    public DriverService(RideRepository rideRepository,
                         RatingRepository ratingRepository, DriverRepository driverRepository,
                         DriverRequestRepository driverRequestRepository) {
        this.rideRepository = rideRepository;
        this.ratingRepository = ratingRepository;
        this.driverRepository = driverRepository;
        this.driverRequestRepository = driverRequestRepository;
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

    public DriverChangeResponseDTO requestDriverChanges(@NotNull Long driverId, @NotNull DriverChangeResponseDTO request) {
        // map DTO -> entity
        DriverRequest entity = new DriverRequest();
        entity.setSubmissionDate(new Date(System.currentTimeMillis()));
        entity.setRequestStatus(rs.ac.uns.ftn.asd.ridenow.model.enums.DriverChangesStatus.PENDING);
        entity.setDriverId(driverId);

        entity.setEmail(request.getEmail());
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setAddress(request.getAddress());
        entity.setProfileImage(request.getProfileImage());
        entity.setVehicleModel(request.getVehicleModel());
        entity.setNumberOfSeats(request.getNumberOfSeats());
        entity.setVehicleType(request.getVehicleType());
        entity.setBabyFriendly(request.getBabyFriendly() != null ? request.getBabyFriendly() : false);
        entity.setPetFriendly(request.getPetFriendly() != null ? request.getPetFriendly() : false);

        // vehicleId is required by entity; try to set to driver's current vehicle if present
        try {
            Driver driver = driverRepository.getReferenceById(driverId);
            if (driver != null && driver.getVehicle() != null) {
                entity.setVehicleId(driver.getVehicle().getId());
            } else {
                entity.setVehicleId(0L);
            }
        } catch (Exception ex) {
            entity.setVehicleId(0L);
        }

        // save
        DriverRequest saved = driverRequestRepository.save(entity);

        // return the original DTO (could be adapted to include saved id/status)
        return request;
    }
}
