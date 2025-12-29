package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.user.ChangePasswordRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UpdateProfileRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UserResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

@Service
public class UserService {

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
        // mock: profile updated
    }
}
