package rs.ac.uns.ftn.asd.ridenow.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.AdminChangesReviewRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.DriverChangeRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.RegisterDriverRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.RegisterDriverResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.DriverRequest;
import rs.ac.uns.ftn.asd.ridenow.model.Vehicle;
import rs.ac.uns.ftn.asd.ridenow.model.ActivationToken;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverChangesStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.repository.DriverRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.DriverRequestRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.VehicleRepository;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AdminService {

    private final DriverRepository driverRepository;
    private final DriverRequestRepository driverRequestRepository;
    private final VehicleRepository vehicleRepository;
    private final EmailService emailService;

    public AdminService(DriverRepository driverRepository, DriverRequestRepository driverRequestRepository,
                        VehicleRepository vehicleRepository, EmailService emailService) {
        this.driverRepository = driverRepository;
        this.driverRequestRepository = driverRequestRepository;
        this.vehicleRepository = vehicleRepository;
        this.emailService = emailService;
    }

    public List<DriverChangeRequestDTO> getDriverRequests() {
        List<DriverChangeRequestDTO> requests = new ArrayList<>();

        // load all driver requests ordered by submissionDate (newest first)
        List<DriverRequest> entities = driverRequestRepository.findAll(Sort.by(Sort.Direction.DESC, "submissionDate"));
        for (DriverRequest entity : entities) {
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
            System.out.println("Id " + entity.getId());
            requests.add(request);
        }

        return requests;
    }

    @Transactional
    public void reviewDriverRequest(
            Long adminId,
            Long requestId,
            AdminChangesReviewRequestDTO dto) {

        DriverRequest req = driverRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("DriverRequest not found: " + requestId));

        // set admin response date
        req.setAdminResponseDate(new Date(System.currentTimeMillis()));

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
    public RegisterDriverResponseDTO register(@Valid RegisterDriverRequestDTO request) {
        // create Vehicle
        Vehicle vehicle = new Vehicle();
        vehicle.setLicencePlate(request.getLicensePlate());
        vehicle.setModel(request.getVehicleModel());
        vehicle.setType(request.getVehicleType());
        vehicle.setSeatCount(request.getNumberOfSeats());
        vehicle.setChildFriendly(request.isBabyFriendly());
        vehicle.setPetFriendly(request.isPetFriendly());

        // create Driver
        Driver driver = new Driver();
        driver.setEmail(request.getEmail());
        // password is required in User - set a random placeholder or empty; application should handle password set later
        driver.setPassword("1234567");
        driver.setFirstName(request.getFirstName());
        driver.setLastName(request.getLastName());
        driver.setPhoneNumber(request.getPhoneNumber());
        driver.setProfileImage(request.getProfileImage() != null ? request.getProfileImage() : "default_profile_image_url");
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
}
