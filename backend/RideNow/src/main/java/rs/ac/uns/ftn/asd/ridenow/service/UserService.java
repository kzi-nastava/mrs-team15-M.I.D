package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.user.ChangePasswordRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UpdateProfileRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UserResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.model.Vehicle;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.repository.UserRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.DriverRepository;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;

    public UserService(UserRepository userRepository, DriverRepository driverRepository) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
    }

    public void changePassword(Long userId, ChangePasswordRequestDTO dto) {
        // mock: password changed
    }

    public UserResponseDTO getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        UserResponseDTO dto = new UserResponseDTO();

        dto.setId(user.getId());
        dto.setRole(user instanceof Driver ? UserRoles.DRIVER : UserRoles.USER);
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setAddress(user.getAddress());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setProfileImage(user.getProfileImage());
        dto.setActive(user.isActive());

        if (user instanceof Driver driver) {
            System.out.println("Fetching driver details for user ID: " + userId);
            Vehicle vehicle = driver.getVehicle();

            if (vehicle != null) {
                dto.setLicensePlate(vehicle.getLicencePlate());
                System.out.println(dto.getLicensePlate());
                dto.setVehicleModel(vehicle.getModel());
                dto.setVehicleType(vehicle.getType());
                dto.setNumberOfSeats(vehicle.getSeatCount());
                dto.setBabyFriendly(vehicle.isChildFriendly());
                dto.setPetFriendly(vehicle.isPetFriendly());
            }

            dto.setHoursWorkedLast24(driver.getWorkingHoursLast24());
        }

        return dto;
    }



    public void updateUser(Long userId, UpdateProfileRequestDTO dto) {
        Optional<User> opt = userRepository.findById(userId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        User user = opt.get();

        // check email uniqueness (if changed)
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            Optional<User> existing = userRepository.findByEmail(dto.getEmail());
            if (existing.isPresent() && !existing.get().getId().equals(userId)) {
                throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
            }
        }

        // update user fields
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAddress(dto.getAddress());
        user.setProfileImage(dto.getProfileImage());

        userRepository.save(user);
    }
}
