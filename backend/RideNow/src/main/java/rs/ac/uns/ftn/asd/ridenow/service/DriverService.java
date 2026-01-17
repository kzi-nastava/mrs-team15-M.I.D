package rs.ac.uns.ftn.asd.ridenow.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.DriverChangeRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.DriverChangeResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.DriverHistoryItemDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RatingDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RouteDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RideResponseDTO;
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

    public List<DriverHistoryItemDTO> getDriverHistory(Long driverId) {
        List<DriverHistoryItemDTO> driverHistory = new ArrayList<>();

        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new EntityNotFoundException("Driver with id " + driverId + " not found"));

        List<Ride> driverRides = rideRepository.findByDriverWithAllRelations(driver);
        for (Ride ride : driverRides) {
            DriverHistoryItemDTO dto = new DriverHistoryItemDTO();
            dto.setRoute(new RouteDTO(ride.getRoute()));
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
                dto.setRating(new RatingDTO(ratingRepository.findByRide(ride)));
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

    public DriverChangeResponseDTO requestDriverChanges(@NotNull Long driverId, @NotNull DriverChangeRequestDTO request) {
        // map DTO -> entity
        DriverChangeResponseDTO response = new DriverChangeResponseDTO();
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
        entity.setLicensePlate(request.getLicensePlate());
        entity.setVehicleModel(request.getVehicleModel());
        entity.setNumberOfSeats(request.getNumberOfSeats());
        entity.setVehicleType(request.getVehicleType());
        entity.setBabyFriendly(request.getBabyFriendly() != null ? request.getBabyFriendly() : false);
        entity.setPetFriendly(request.getPetFriendly() != null ? request.getPetFriendly() : false);

        response.setEmail(request.getEmail());
        response.setFirstName(request.getFirstName());
        response.setLastName(request.getLastName());
        response.setPhoneNumber(request.getPhoneNumber());
        response.setAddress(request.getAddress());
        response.setProfileImage(request.getProfileImage());
        response.setLicensePlate(request.getLicensePlate());
        response.setVehicleModel(request.getVehicleModel());
        response.setNumberOfSeats(request.getNumberOfSeats());
        response.setVehicleType(request.getVehicleType());
        response.setBabyFriendly(request.getBabyFriendly() != null ? request.getBabyFriendly() : false);
        response.setPetFriendly(request.getPetFriendly() != null ? request.getPetFriendly() : false);

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

        System.out.println(entity.getLicensePlate());
        // save
        DriverRequest saved = driverRequestRepository.save(entity);

        return response;
    }

    public List<RideResponseDTO> findScheduledRides(Long driverId) {
        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new EntityNotFoundException("Driver with id " + driverId + " not found"));
        List<Ride> rides = rideRepository.findScheduledRidesByDriver(driver);
        List<RideResponseDTO> rideDTOs = new ArrayList<>();

        for (Ride ride : rides) {
            RideResponseDTO dto = new RideResponseDTO();
            dto.setRideId(ride.getId());
            dto.setStartTime(ride.getScheduledTime());
            dto.setPassengerEmails(new ArrayList<>());
            for (Passenger p : ride.getPassengers()) {
                dto.getPassengerEmails().add(p.getUser().getEmail());
            }
            dto.setRoute(new RouteDTO(ride.getRoute()));
            rideDTOs.add(dto);
        }

        return rideDTOs;
    }
}
