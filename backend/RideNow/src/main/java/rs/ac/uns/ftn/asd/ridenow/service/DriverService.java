package rs.ac.uns.ftn.asd.ridenow.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.*;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RatingDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RouteDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RideResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.UpcomingRideDTO;
import rs.ac.uns.ftn.asd.ridenow.model.*;
import rs.ac.uns.ftn.asd.ridenow.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static rs.ac.uns.ftn.asd.ridenow.util.AddressUtil.formatAddress;

@Service
public class DriverService {
    private final RideRepository rideRepository;
    private final RatingRepository ratingRepository;
    private final DriverRepository driverRepository;
    private final DriverRequestRepository driverRequestRepository;
    private final ActivationTokenRepository activationTokenRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public DriverService(RideRepository rideRepository,
                         RatingRepository ratingRepository, DriverRepository driverRepository,
                         DriverRequestRepository driverRequestRepository,
                         ActivationTokenRepository activationTokenRepository,
                         UserRepository userRepository,
                         EmailService emailService,
                         PasswordEncoder passwordEncoder,
                         VehicleRepository vehicleRepository, AuthService authService) {
        this.rideRepository = rideRepository;
        this.ratingRepository = ratingRepository;
        this.driverRepository = driverRepository;
        this.driverRequestRepository = driverRequestRepository;
        this.activationTokenRepository = activationTokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.vehicleRepository = vehicleRepository;
        this.authService = authService;
    }

  private Page<Ride> getRides(Long driverId, Pageable pageable, String sortBy, String sortDir, Long date) {
        // Validate sortBy and sortDir
        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new EntityNotFoundException("Driver with id " + driverId + " not found"));

      LocalDateTime startOfDay = null;
      LocalDateTime endOfDay = null;

      if (date != null) {
          LocalDateTime dateTime = LocalDateTime.ofInstant(
                  Instant.ofEpochMilli(date), ZoneId.systemDefault());
          startOfDay = dateTime.toLocalDate().atStartOfDay();
          endOfDay = dateTime.toLocalDate().atTime(23, 59, 59);
      }

      // Different sorting options
      if (sortBy.equals("passengers")) {
          if (sortDir.equals("asc")) {
              return date != null ?
                      rideRepository.findRidesSortedByFirstPassengerNameAscWithDate(driverId, startOfDay, endOfDay, pageable) :
                      rideRepository.findRidesSortedByFirstPassengerNameAsc(driverId, pageable);
          } else {
              return date != null ?
                      rideRepository.findRidesSortedByFirstPassengerNameDescWithDate(driverId, startOfDay, endOfDay, pageable) :
                      rideRepository.findRidesSortedByFirstPassengerNameDesc(driverId, pageable);
          }
      } else if (sortBy.equals("duration")) {
          if (sortDir.equals("asc")) {
              return date != null ?
                      rideRepository.findRidesSortedByDurationAscWithDate(driverId, startOfDay, endOfDay, pageable) :
                      rideRepository.findRidesSortedByDurationAsc(driverId, pageable);
          } else {
              return date != null ?
                      rideRepository.findRidesSortedByDurationDescWithDate(driverId, startOfDay, endOfDay, pageable) :
                      rideRepository.findRidesSortedByDurationDesc(driverId, pageable);
          }
      }
      return date != null ?
              rideRepository.findByDriverWithAllRelationsAndDate(driver, startOfDay, endOfDay, pageable) :
              rideRepository.findByDriverWithAllRelations(driver, pageable);
    }

    public Page<DriverHistoryItemDTO> getDriverHistory(Long driverId, Pageable pageable, String sortBy, String sortDir, Long date) {
        List<DriverHistoryItemDTO> driverHistory = new ArrayList<>();

        // Validate sortBy and sortDir
        Page<Ride> driverRides = getRides(driverId, pageable, sortBy, sortDir, date);
        for (Ride ride : driverRides.getContent()) {
            DriverHistoryItemDTO dto = new DriverHistoryItemDTO();
            dto.setRoute(new RouteDTO(ride.getRoute()));
            dto.setStartTime(ride.getStartTime());
            dto.setEndTime(ride.getEndTime());
            dto.setCost(ride.getPrice());

            // Get passenger names
            List<String> passengerNames = new ArrayList<>();
            for (Passenger p : ride.getPassengers()) {
                passengerNames.add(p == null ? null : p.getUser().getFirstName() + " " + p.getUser().getLastName());
            }
            dto.setPassengers(passengerNames);

            // Check for panic alert
            if (ride.getPanicAlert() != null) {
                dto.setPanic(true);
                if (ride.getPanicAlert().getPanicBy() != null) {
                    dto.setPanicBy(ride.getPanicAlert().getPanicBy());
                }
            }

            // Check if ride was cancelled
            dto.setCancelled(ride.getCancelled());
            if(ride.getCancelled()) {
                dto.setCancelledBy(ride.getCancelledBy());
            }

            if (ratingRepository.findByRide(ride)!= null) {
                dto.setRating(new RatingDTO(ratingRepository.findByRide(ride)));
            }

            // Get inconsistencies
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
        return new PageImpl(driverHistory, pageable, driverRides.getTotalElements());
    }

    public DriverChangeResponseDTO requestDriverChanges(Driver driver, DriverChangeRequestDTO request, MultipartFile profileImage) throws IOException {

        // Check email and license plate uniqueness
        if (request.getEmail() != null && !request.getEmail().equals(driver.getEmail())) {
            Optional<User> existing = userRepository.findByEmail(request.getEmail());
            if (existing.isPresent() && !existing.get().getId().equals(driver.getId())) {
                throw new IllegalArgumentException("Email already in use: " + request.getEmail());
            }
        }
        // License plate check
        if (request.getLicensePlate() != null && driver.getVehicle() != null && !request.getLicensePlate().equals(driver.getVehicle().getLicencePlate())) {
            Vehicle existingVehicle = vehicleRepository.findByLicencePlate(request.getLicensePlate());
            if (existingVehicle != null && !existingVehicle.getDriver().getId().equals(driver.getId())) {
                throw new IllegalArgumentException("License plate already in use: " + request.getLicensePlate());
            }
        }

        DriverChangeResponseDTO response = new DriverChangeResponseDTO();
        DriverRequest entity = new DriverRequest();
        entity.setSubmissionDate(new Date(System.currentTimeMillis()));
        entity.setRequestStatus(rs.ac.uns.ftn.asd.ridenow.model.enums.DriverChangesStatus.PENDING);
        entity.setDriverId(driver.getId());

        entity.setEmail(request.getEmail());
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setAddress(request.getAddress());
        String profileImageURL = driver.getProfileImage();
        // Check if profile image is provided
        if (profileImage != null && !profileImage.isEmpty()){
            profileImageURL = authService.generateProfileImageUrl(profileImage);

        }

        // build entity
        entity.setProfileImage(profileImageURL);
        entity.setLicensePlate(request.getLicensePlate());
        entity.setVehicleModel(request.getVehicleModel());
        entity.setNumberOfSeats(request.getNumberOfSeats());
        entity.setVehicleType(request.getVehicleType());
        entity.setBabyFriendly(request.getBabyFriendly() != null ? request.getBabyFriendly() : false);
        entity.setPetFriendly(request.getPetFriendly() != null ? request.getPetFriendly() : false);

        // build response dto
        response.setEmail(request.getEmail());
        response.setFirstName(request.getFirstName());
        response.setLastName(request.getLastName());
        response.setPhoneNumber(request.getPhoneNumber());
        response.setAddress(request.getAddress());
        response.setProfileImage(profileImageURL);
        response.setLicensePlate(request.getLicensePlate());
        response.setVehicleModel(request.getVehicleModel());
        response.setNumberOfSeats(request.getNumberOfSeats());
        response.setVehicleType(request.getVehicleType());
        response.setBabyFriendly(request.getBabyFriendly() != null ? request.getBabyFriendly() : false);
        response.setPetFriendly(request.getPetFriendly() != null ? request.getPetFriendly() : false);


        if (driver.getVehicle() != null) {
            entity.setVehicleId(driver.getVehicle().getId());
        } else {
            entity.setVehicleId(0L);
        }

        // save
        driverRequestRepository.save(entity);

        return response;
    }



    public List<UpcomingRideDTO> findScheduledRides(Long driverId) {
        // Validate driver existence
        Driver driver = driverRepository.findById(driverId).orElseThrow(() -> new EntityNotFoundException("Driver with id " + driverId + " not found"));
        List<Ride> rides = rideRepository.findScheduledRidesByDriver(driver);
        List<UpcomingRideDTO> rideDTOs = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Transform each Ride into an UpcomingRideDTO
        for (Ride ride : rides) {
            UpcomingRideDTO dto = new UpcomingRideDTO();
            dto.setId(ride.getId());
            dto.setStartTime(ride.getScheduledTime().format(formatter));
            dto.setPassengers("");
            for (Passenger p : ride.getPassengers()) {
                dto.setPassengers(dto.getPassengers() + p.getUser().getFirstName() + " " + p.getUser().getLastName() + ", ");
            }
            if (dto.getPassengers().length() > 2) {
                dto.setPassengers(dto.getPassengers().substring(0, dto.getPassengers().length() - 2));
            }
            dto.setCanCancel(true);

            String formattedStartAddress = formatAddress(ride.getRoute().getStartLocation().getAddress());
            String formattedEndAddress = formatAddress(ride.getRoute().getEndLocation().getAddress());

            dto.setRoute(formattedStartAddress + " → " + formattedEndAddress);
            rideDTOs.add(dto);
        }

        return rideDTOs;
    }

    public boolean hasRideInProgress(Driver driver) {
        Optional<Ride> optionalRide = driverRepository.findRideInProgress(driver.getId());
        return optionalRide.isPresent();
    }

    public void changeDriverStatus(Driver driver, DriverStatusRequestDTO request){
        if(hasRideInProgress(driver)){
            driver.setPendingStatus(request.getStatus());
        }else{
            driver.setStatus(request.getStatus());
            driver.setPendingStatus(null);
        }
        driverRepository.save(driver);
    }

    public void activateDriverAccountByToken(DriverAccountActivationRequestDTO request) {

        // token validation
        if (request.getToken() == null || request.getToken().isBlank()) {
            throw new IllegalArgumentException("Invalid token");
        }

        Optional<ActivationToken> optionalToken = activationTokenRepository.findByToken(request.getToken());
        if (optionalToken.isEmpty()) {
            throw new IllegalArgumentException("Invalid token");
        }
        // token expired
        ActivationToken activationToken = optionalToken.get();
        if (activationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            handleExpiredActivationToken(activationToken);
            throw new IllegalArgumentException("Token expired. New activation link sent to your email.");
        }
        // token does not belong to a driver
        User user = activationToken.getUser();
        if (!(user instanceof Driver)) {
            throw new IllegalArgumentException("Token does not belong to a driver");
        }

        // validate passwords
        if (request.getPassword() == null || request.getPasswordConfirmation() == null || !request.getPassword().equals(request.getPasswordConfirmation())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        // encode and set password
        String hashed = passwordEncoder.encode(request.getPassword());
        user.setPassword(hashed);
        user.setActive(true);
        user.setActivationToken(null);
        userRepository.save(user);

        // delete token record
        activationTokenRepository.delete(activationToken);
    }

    private void handleExpiredActivationToken(ActivationToken activationToken) {
        User user = activationToken.getUser();
        user.setActivationToken(null);
        activationTokenRepository.delete(activationToken);
        userRepository.save(user);

        // generate new token and send
        String tokenStr = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        ActivationToken newToken = new ActivationToken(tokenStr, expiresAt, user);
        user.setActivationToken(newToken);
        activationTokenRepository.save(newToken);
        userRepository.save(user);
        // send email
        emailService.sendDriverActivationMail(user.getEmail(), newToken);
    }

    public DriverLocationResponseDTO updateDriverLocation(Driver driver, DriverLocationRequestDTO request) {
        Vehicle vehicle = vehicleRepository.findByDriver(driver);
        if (vehicle == null) {
            throw new IllegalArgumentException("Driver not found");
        }
        vehicle.setLat(request.getLat());
        vehicle.setLon(request.getLon());
        vehicleRepository.save(vehicle);

        DriverLocationResponseDTO response = new DriverLocationResponseDTO();
        response.setLat(request.getLat());
        response.setLon(request.getLon());
        response.setLicencePlate(vehicle.getLicencePlate());
        return response;
    }

    public DriverCanStartRideResponseDTO canDriverStartRide(Driver driver){
        DriverCanStartRideResponseDTO response = new DriverCanStartRideResponseDTO();
        Optional<Ride> rides = rideRepository.findCurrentRideByDriver(driver.getId());
        if (rides.isPresent()){
            response.setCanStartRide(false);
            return response;
        }
        response.setCanStartRide(true);
        return response;
    }
}
