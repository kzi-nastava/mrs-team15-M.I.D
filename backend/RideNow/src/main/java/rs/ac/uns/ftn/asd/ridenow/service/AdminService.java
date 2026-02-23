package rs.ac.uns.ftn.asd.ridenow.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.*;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RatingDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RouteDTO;
import rs.ac.uns.ftn.asd.ridenow.model.*;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.*;
import rs.ac.uns.ftn.asd.ridenow.dto.model.PriceConfigDTO;
import rs.ac.uns.ftn.asd.ridenow.model.*;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverChangesStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.repository.DriverRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.DriverRequestRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.PriceRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.UserRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.VehicleRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.*;
import rs.ac.uns.ftn.asd.ridenow.dto.user.ReportResponseDTO;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AdminService {

    private final DriverRepository driverRepository;
    private final DriverRequestRepository driverRequestRepository;
    private final VehicleRepository vehicleRepository;
    private final EmailService emailService;
    private final AuthService authService;
    private final PriceRepository priceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private  HistoryRepository historyRepository;

    @Autowired
    private UserService userService;

    public AdminService(DriverRepository driverRepository, DriverRequestRepository driverRequestRepository,
                        VehicleRepository vehicleRepository, EmailService emailService,
                        AuthService authService, PriceRepository priceRepository) {
        this.driverRepository = driverRepository;
        this.driverRequestRepository = driverRequestRepository;
        this.vehicleRepository = vehicleRepository;
        this.emailService = emailService;
        this.authService = authService;
        this.priceRepository = priceRepository;
    }

    public List<DriverChangeRequestDTO> getDriverRequests() {
        List<DriverChangeRequestDTO> requests = new ArrayList<>();

        // load all driver requests ordered by submissionDate (newest first)
        List<DriverRequest> entities = driverRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "submissionDate"));
        for (DriverRequest entity : entities) {
            // build response dto
            DriverChangeRequestDTO request = new DriverChangeRequestDTO();
            request.setEmail(entity.getEmail());
            request.setFirstName(entity.getFirstName());
            request.setLastName(entity.getLastName());
            request.setPhoneNumber(entity.getPhoneNumber());
            request.setProfileImage(entity.getProfileImage());
            request.setAddress(entity.getAddress());
            request.setLicensePlate(entity.getLicensePlate());
            request.setVehicleModel(entity.getVehicleModel());
            request.setVehicleType(entity.getVehicleType() != null ? entity.getVehicleType() : null);
            request.setNumberOfSeats(entity.getNumberOfSeats());
            request.setBabyFriendly(entity.isBabyFriendly());
            request.setPetFriendly(entity.isPetFriendly());
            request.setStatus(entity.getRequestStatus());
            request.setSubmitDate(entity.getSubmissionDate());
            request.setId(entity.getId());
            request.setDriverId(entity.getDriverId());
            request.setId(entity.getId());
            request.setMessage(entity.getMessage());
            request.setAdminResponseDate(entity.getAdminResponseDate());
            requests.add(request);
        }

        return requests;
    }

    @Transactional
    public void reviewDriverRequest(
            Long adminId,
            Long requestId,
            AdminChangesReviewRequestDTO dto) {

        // find change request
        DriverRequest req = driverRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("DriverRequest not found: " + requestId));

        // set admin response date
        req.setAdminResponseDate(new Date(System.currentTimeMillis()));
        req.setMessage(dto.getMessage());
        if (dto.isApproved()) {
            req.setRequestStatus(DriverChangesStatus.APPROVED);

            // apply changes: update existing driver or create new one
            Driver driver = null;
            Long driverId = req.getDriverId();
            if (driverId != null && driverId > 0) {
                driver = driverRepository.findById(driverId).orElse(null);
            }

            if (driver == null) {
                driver = new Driver();
                driver.setEmail(req.getEmail());
                driver.setPassword("1234567");
                driver.setFirstName(req.getFirstName());
                driver.setLastName(req.getLastName());
                driver.setPhoneNumber(req.getPhoneNumber());
                driver.setProfileImage(req.getProfileImage() != null ? req.getProfileImage() : "default_profile_image_url");
                driver.setAddress(req.getAddress());
                driver.setActive(true);
                driver.setBlocked(false);
                driver.setStatus(DriverStatus.ACTIVE);
                driver.setAvailable(true);
            } else {
                // update fields
                driver.setEmail(req.getEmail());
                driver.setFirstName(req.getFirstName());
                driver.setLastName(req.getLastName());
                driver.setPhoneNumber(req.getPhoneNumber());
                driver.setProfileImage(req.getProfileImage() != null ? req.getProfileImage() : driver.getProfileImage());
                driver.setAddress(req.getAddress());
            }

            // find existing vehicle by licence plate to avoid unique constraint violation
            Vehicle vehicle = null;
            if (req.getLicensePlate() != null && !req.getLicensePlate().isEmpty()) {
                vehicle = vehicleRepository.findByLicencePlate(req.getLicensePlate());
            }

            if (vehicle != null) {
                // If vehicle is attached to a different driver, detach from that driver first
                if (vehicle.getDriver() != null && (driver.getId() == null || !vehicle.getDriver().getId().equals(driver.getId()))) {
                    Driver previousDriver = vehicle.getDriver();
                    previousDriver.setVehicle(null);
                    // persist previous driver to remove the association
                    driverRepository.save(previousDriver);
                }
                // update vehicle fields if necessary
                vehicle.setModel(req.getVehicleModel());
                vehicle.setType(req.getVehicleType());
                vehicle.setSeatCount(req.getNumberOfSeats());
                vehicle.setChildFriendly(req.isBabyFriendly());
                vehicle.setPetFriendly(req.isPetFriendly());
            } else {
                vehicle = new Vehicle();
                vehicle.setLicencePlate(req.getLicensePlate());
                vehicle.setModel(req.getVehicleModel());
                vehicle.setType(req.getVehicleType());
                vehicle.setSeatCount(req.getNumberOfSeats());
                vehicle.setChildFriendly(req.isBabyFriendly());
                vehicle.setPetFriendly(req.isPetFriendly());
            }

            // link vehicle and driver
            driver.assignVehicle(vehicle);

            // Save driver (cascade will save vehicle). If vehicle was existing and detached from previous driver above,
            // this will update driver_id on vehicle rather than inserting a duplicate.
            Driver saved = driverRepository.save(driver);
            // update driverId in request in case it was a new driver
            req.setDriverId(saved.getId());

        } else {
            req.setRequestStatus(DriverChangesStatus.REJECTED);

        }

        driverRequestRepository.save(req);
    }

    @Transactional
    public RegisterDriverResponseDTO register(@Valid RegisterDriverRequestDTO request, MultipartFile profileImage) throws IOException {
        // create Vehicle
        Vehicle vehicle = new Vehicle();
        vehicle.setLicencePlate(request.getLicensePlate());
        vehicle.setModel(request.getVehicleModel());
        vehicle.setType(request.getVehicleType());
        vehicle.setSeatCount(request.getNumberOfSeats());
        vehicle.setChildFriendly(request.isBabyFriendly());
        vehicle.setPetFriendly(request.isPetFriendly());

        // generate profile image URL
        String profileImageURL = authService.generateProfileImageUrl(profileImage);

        // create Driver
        Driver driver = new Driver();
        driver.setEmail(request.getEmail());
        // password is required in User - set a random placeholder or empty; application should handle password set later
        driver.setPassword("1234567");
        driver.setFirstName(request.getFirstName());
        driver.setLastName(request.getLastName());
        driver.setPhoneNumber(request.getPhoneNumber());
        driver.setProfileImage(profileImageURL);
        driver.setAddress(request.getAddress());
        // mark admin-created drivers as inactive until they set their password
        driver.setActive(false);
        driver.setBlocked(false);
        driver.setStatus(DriverStatus.INACTIVE);
        driver.setAvailable(false);
        driver.setRole(UserRoles.DRIVER);

        // attach activation token so frontend can show activation link for initial password set
        ActivationToken token = new ActivationToken(UUID.randomUUID().toString(), LocalDateTime.now().plusHours(24), driver);
        driver.setActivationToken(token);

        // associate
        driver.assignVehicle(vehicle);

        // save (cascade will save vehicle and activation token)
        Driver saved = driverRepository.save(driver);

        // send activation email (don't fail the request if email sending fails)
        try {
            emailService.sendDriverActivationMail(saved.getEmail(), saved.getActivationToken());
        } catch (Exception e) {
            System.err.println("Failed to send activation email: " + e.getMessage());
        }

        RegisterDriverResponseDTO response = new RegisterDriverResponseDTO();
        response.setId(saved.getId());
        response.setEmail(saved.getEmail());
        response.setFirstName(saved.getFirstName());
        response.setLastName(saved.getLastName());
        response.setPhoneNumber(saved.getPhoneNumber());
        response.setProfileImage(saved.getProfileImage());
        response.setAddress(saved.getAddress());
        response.setLicensePlate(saved.getVehicle() != null ? saved.getVehicle().getLicencePlate() : null);
        response.setVehicleModel(saved.getVehicle() != null ? saved.getVehicle().getModel() : null);
        response.setVehicleType(saved.getVehicle() != null ? saved.getVehicle().getType() : null);
        response.setNumberOfSeats(saved.getVehicle() != null ? saved.getVehicle().getSeatCount() : 0);
        response.setBabyFriendly(saved.getVehicle() != null && saved.getVehicle().isChildFriendly());
        response.setPetFriendly(saved.getVehicle() != null && saved.getVehicle().isPetFriendly());

        return response;
    }

    public PriceConfigResponseDTO getPriceConfigs() {
        PriceConfigResponseDTO response = new PriceConfigResponseDTO();
        List<PriceConfigDTO> prices = new ArrayList<>();

        // load all price configs
        List<PriceConfig> entities = priceRepository.findAll();
        for (PriceConfig entity : entities) {
            PriceConfigDTO price = new PriceConfigDTO();
            price.setVehicleType(entity.getVehicleType());
            price.setBasePrice(entity.getBasePrice());
            price.setPricePerKm(entity.getPricePerKm());
            prices.add(price);
        }

        response.setPrices(prices);
        return response;
    }

    public void updatePriceConfigs(PriceConfigRequestDTO request) {
        // update each price config; if any config for a vehicle type is missing, throw an error
        for (PriceConfigDTO priceDTO : request.getPrices()) {
            PriceConfig config = priceRepository.findByVehicleType(priceDTO.getVehicleType())
                    .orElseThrow(() -> new IllegalArgumentException("PriceConfig not found for vehicle type: " + priceDTO.getVehicleType()));
            config.setBasePrice(priceDTO.getBasePrice());
            config.setPricePerKm(priceDTO.getPricePerKm());
            priceRepository.save(config);
        }
    }

    public Page<UserItemDTO> getNonAdminUsers(Pageable pageable) {
        List<UserRoles> roles = List.of(UserRoles.DRIVER, UserRoles.USER);
        Page<User> users = userRepository.findByRoleIn(roles, pageable);
        List<UserItemDTO> usersDTO = users.getContent().stream()
                .map(this::mapToUserItemDTO)
                .toList();

        return new PageImpl<>(usersDTO, pageable, users.getTotalElements());
    }

    private UserItemDTO mapToUserItemDTO(User user) {
        UserItemDTO dto = new UserItemDTO();
        dto.setId(user.getId());
        dto.setName(user.getFirstName());
        dto.setSurname(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        return dto;
    }

    public Page<AdminRideHistoryItemDTO> getRideHistory(User user, Pageable pageable, Long date) {
        List<AdminRideHistoryItemDTO> history = new ArrayList<>();
        Page<Ride> rides = getRides(user, pageable, date);
        for (Ride ride : rides.getContent()) {
            AdminRideHistoryItemDTO dto = new AdminRideHistoryItemDTO();
            dto.setRoute(new RouteDTO(ride.getRoute()));
            dto.setStartTime(ride.getStartTime());
            dto.setEndTime(ride.getEndTime());
            dto.setPrice(ride.getPrice());
            dto.setRideId(ride.getId());
            dto.setDriver(ride.getDriver() != null ? ride.getDriver().getFirstName() + " " + ride.getDriver().getLastName() : null);

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
            if (ride.getCancelled()) {
                dto.setCancelledBy(ride.getCancelledBy());
            }

            if (ratingRepository.findByRide(ride) != null) {
                dto.setRating(new RatingDTO(ratingRepository.findByRide(ride)));
            }

            List<Inconsistency> inconsistencies = ride.getInconsistencies();
            List<String> inconsistencyStrings = new ArrayList<>();
            if (!inconsistencies.isEmpty()) {
                for (Inconsistency inconsistency : inconsistencies) {
                    inconsistencyStrings.add(inconsistency.getDescription());
                }
            }
            dto.setInconsistencies(inconsistencyStrings);
            dto.setRouteId(ride.getRoute() != null ? ride.getRoute().getId() : null);
            history.add(dto);
        }
        return new PageImpl(history, pageable, rides.getTotalElements());
    }

    private Page<Ride> getRides(User user, Pageable pageable, Long date) {
        LocalDateTime startOfDay = null;
        LocalDateTime endOfDay = null;

        if (date != null) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault());
            startOfDay = dateTime.toLocalDate().atStartOfDay();
            endOfDay = dateTime.toLocalDate().atTime(23, 59, 59);
        }
        return date != null ?
                findWithAllRelationsAndDate(user, startOfDay, endOfDay, pageable) :
                findWithAllRelations(user, pageable);
    }

    private Page<Ride> findWithAllRelations(User user, Pageable pageable) {
        if (user instanceof Driver) {
            return historyRepository.findDriverRidesWithAllRelations(user.getId(), pageable);
        }
        return historyRepository.findPassengerRidesWithAllRelations(user.getId(), pageable);
    }

    private Page<Ride> findWithAllRelationsAndDate(User user, LocalDateTime startOfDay, LocalDateTime endOfDay, Pageable pageable) {
        if (user instanceof Driver) {
            return historyRepository.findDriverRidesWithAllRelationsAndDate(user.getId(), startOfDay, endOfDay, pageable);
        }
        return historyRepository.findPassengerRidesWithAllRelationsAndDate(user.getId(), startOfDay, endOfDay, pageable);
    }

    public AdminReportResponseDTO getReport(Long startDateReq, Long endDateReq, boolean drivers, boolean users,String userIdReq) {

        AdminReportResponseDTO response = new AdminReportResponseDTO();

        // determine selected users
        List<User> selectedUsers = new ArrayList<>();
        // if a specific personId provided, try to parse and load that user
        if (userIdReq != null && !userIdReq.isBlank()) {
            try {
                Long pid = Long.parseLong(userIdReq);
                var opt = userRepository.findById(pid);
                if (opt.isEmpty()) throw new IllegalArgumentException("User not found with id: " + pid);
                selectedUsers.add(opt.get());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid personId: " + userIdReq);
            }
        } else {
            // collect users by role flags
            List<User> all = userRepository.findAll();
            for (User u : all) {
                if (u.getRole() == null) continue;
                if (drivers && u.getRole().toString().equals("DRIVER")) selectedUsers.add(u);
                if (users && u.getRole().toString().equals("USER")) selectedUsers.add(u);
            }
        }

        // If no users selected, return empty response
        if (selectedUsers.isEmpty()) {
            response.setRidesPerDay(java.util.Collections.emptyMap());
            response.setKmPerDay(java.util.Collections.emptyMap());
            response.setMoneyPerDay(java.util.Collections.emptyMap());
            response.setSumRides(0);
            response.setSumKM(0.0);
            response.setSumMoney(0.0);
            response.setAvgRides(0);
            response.setAvgKM(0.0);
            response.setAvgMoney(0.0);
            response.setStartDate(null);
            response.setEndDate(null);
            response.setDrivers(drivers);
            response.setUsers(users);
            response.setPerson(userIdReq);
            return response;
        }

        // Determine default start/end if not provided: use earliest scheduledTime across relevant rides, end = now
        LocalDateTime defaultEnd = LocalDateTime.now();
        LocalDateTime end = (endDateReq == null) ? defaultEnd : LocalDateTime.ofInstant(Instant.ofEpochMilli(endDateReq), ZoneId.systemDefault());

        LocalDateTime defaultStart = defaultEnd;
        boolean foundAny = false;
        for (User u : selectedUsers) {
            // fetch all relevant rides for this user (no date filter)
            Page<Ride> page = findWithAllRelations(u, Pageable.unpaged());
            for (Ride r : page.getContent()) {
                if (r.getScheduledTime() == null) continue;
                if (!foundAny) {
                    defaultStart = r.getScheduledTime();
                    foundAny = true;
                } else {
                    if (r.getScheduledTime().isBefore(defaultStart)) defaultStart = r.getScheduledTime();
                }
            }
        }

        LocalDateTime start = (startDateReq == null) ? defaultStart : LocalDateTime.ofInstant(Instant.ofEpochMilli(startDateReq), ZoneId.systemDefault());

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("startDate must be before or equal to endDate");
        }

        // per-day maps
        java.util.Map<java.time.LocalDate, Integer> ridesPerDay = new java.util.HashMap<>();
        java.util.Map<java.time.LocalDate, Double> kmPerDay = new java.util.HashMap<>();
        java.util.Map<java.time.LocalDate, Double> moneyPerDay = new java.util.HashMap<>();

        double totalKm = 0.0;
        double totalMoney = 0.0;
        int totalRides = 0;

        // For each selected user, fetch their rides in date range and aggregate
        long startMillis = start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endMillis = end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
         for (User u : selectedUsers) {
             ReportResponseDTO userReport = userService.getReport(startMillis, endMillis, u.getId());
             if (userReport == null) continue;

            // aggregate totals
            totalRides += userReport.getSumRides();
            totalKm += userReport.getSumKM();
            totalMoney += userReport.getSumMoney();

            // aggregate per-day maps
            if (userReport.getRidesPerDay() != null) {
                for (var e : userReport.getRidesPerDay().entrySet()) {
                    java.time.LocalDate day = e.getKey();
                    int cnt = e.getValue() == null ? 0 : e.getValue();
                    ridesPerDay.put(day, ridesPerDay.getOrDefault(day, 0) + cnt);
                }
            }
            if (userReport.getKmPerDay() != null) {
                for (var e : userReport.getKmPerDay().entrySet()) {
                    java.time.LocalDate day = e.getKey();
                    double km = e.getValue() == null ? 0.0 : e.getValue();
                    kmPerDay.put(day, kmPerDay.getOrDefault(day, 0.0) + km);
                }
            }
            if (userReport.getMoneyPerDay() != null) {
                for (var e : userReport.getMoneyPerDay().entrySet()) {
                    java.time.LocalDate day = e.getKey();
                    double m = e.getValue() == null ? 0.0 : e.getValue();
                    moneyPerDay.put(day, moneyPerDay.getOrDefault(day, 0.0) + m);
                }
            }
        }

        // days in range inclusive
        long daysInRange = java.time.temporal.ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;
        if (daysInRange <= 0) daysInRange = 1;

        // build response dto
        response.setRidesPerDay(ridesPerDay);
        response.setKmPerDay(kmPerDay);
        response.setMoneyPerDay(moneyPerDay);

        response.setSumRides(totalRides);
        response.setSumKM(totalKm);
        response.setSumMoney(totalMoney);

        response.setAvgRides((int) Math.round((double) totalRides / (double) daysInRange));
        response.setAvgKM(totalKm / (double) daysInRange);
        response.setAvgMoney(totalMoney / (double) daysInRange);

        response.setStartDate(java.time.LocalDate.from(start));
        response.setEndDate(java.time.LocalDate.from(end));

        response.setDrivers(drivers);
        response.setUsers(users);
        response.setPerson(userIdReq);

        return response;
    }
}

