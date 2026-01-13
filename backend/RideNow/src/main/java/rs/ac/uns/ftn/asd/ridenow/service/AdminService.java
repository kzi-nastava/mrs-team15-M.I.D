package rs.ac.uns.ftn.asd.ridenow.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.AdminChangesReviewRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.DriverChangeRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.RegisterDriverRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.RegisterDriverResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.Vehicle;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverChangesStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;
import rs.ac.uns.ftn.asd.ridenow.repository.DriverRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private final DriverRepository driverRepository;

    public AdminService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public List<DriverChangeRequestDTO> getDriverRequests() {
        List<DriverChangeRequestDTO> requests = new ArrayList<>();

        DriverChangeRequestDTO request = new DriverChangeRequestDTO();
        request.setEmail("driver@mail.com");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhoneNumber("123-456-7890");
        request.setProfileImage("profile_image_url");
        request.setAddress("123 Main St, Cityville");
        request.setLicensePlate("NS123AB");
        request.setVehicleModel("Toyota Prius");
        request.setVehicleType(VehicleType.STANDARD);
        request.setNumberOfSeats(4);
        request.setBabyFriendly(true);
        request.setPetFriendly(false);
        request.setStatus(DriverChangesStatus.PENDING);

        requests.add(request);

        return requests;
    }

    public void reviewDriverRequest(
            Long adminId,
            Long requestId,
            AdminChangesReviewRequestDTO dto) {
        // mock: request reviewed
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
        driver.setPassword("");
        driver.setFirstName(request.getFirstName());
        driver.setLastName(request.getLastName());
        driver.setPhoneNumber(request.getPhoneNumber());
        driver.setProfileImage(request.getProfileImage() != null ? request.getProfileImage() : "default_profile_image_url");
        driver.setAddress(request.getAddress());
        driver.setActive(true);
        driver.setBlocked(false);
        driver.setStatus(DriverStatus.ACTIVE);
        driver.setAvailable(true);

        // associate
        driver.assignVehicle(vehicle);

        // save (cascade will save vehicle)
        Driver saved = driverRepository.save(driver);

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
