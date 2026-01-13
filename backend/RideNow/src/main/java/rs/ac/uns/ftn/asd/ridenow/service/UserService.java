package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.user.ChangePasswordRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UpdateProfileRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UserResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.model.Vehicle;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;
import rs.ac.uns.ftn.asd.ridenow.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void changePassword(Long userId, ChangePasswordRequestDTO dto) {
        // mock: password changed
    }

    public UserResponseDTO getUser(Long userId) {
        UserResponseDTO dto = new UserResponseDTO();

        dto.setId(userId);
        dto.setRole(UserRoles.DRIVER);
        dto.setEmail("user@mail.com");
        dto.setFirstName("Ana");
        dto.setLastName("Anić");
        dto.setAddress("Bulevar Oslobođenja 45");
        dto.setPhoneNumber("+381641234567");
        dto.setProfileImage("profile.png");

        dto.setLicensePlate("BG123456");
        dto.setVehicleModel("Toyota Corolla");
        dto.setVehicleType(VehicleType.STANDARD);
        dto.setNumberOfSeats(4);
        dto.setBabyFriendly(true);
        dto.setPetFriendly(false);
        dto.setHoursWorkedLast24(8.0);


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
