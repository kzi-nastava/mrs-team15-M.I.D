package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.profile.ChangePasswordRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.profile.UpdateProfileRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.profile.UserResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.User;

@Service
public class ProfileService {

    // test logged in user
    private User loggedUser;

    public ProfileService() {
        // test user data
        this.loggedUser = new User();
        this.loggedUser.setEmail("user@mail.com");
        this.loggedUser.setFirstName("Ana");
        this.loggedUser.setLastName("Anić");
        this.loggedUser.setAddress("Novi Sad");
        this.loggedUser.setPhoneNumber("0611234567");
        this.loggedUser.setProfileImage(null);
        this.loggedUser.setPassword("password123");
    }

    public UserResponseDTO getProfile() {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setEmail(loggedUser.getEmail());
        dto.setFirstName(loggedUser.getFirstName());
        dto.setLastName(loggedUser.getLastName());
        dto.setAddress(loggedUser.getAddress());
        dto.setPhoneNumber(loggedUser.getPhoneNumber());
        dto.setProfileImage(loggedUser.getProfileImage());

        return dto;
    }

    public void updateProfile(UpdateProfileRequestDTO dto) {
        loggedUser.setFirstName(dto.getFirstName());
        loggedUser.setLastName(dto.getLastName());
        loggedUser.setEmail(dto.getEmail());
        loggedUser.setAddress(dto.getAddress());
        loggedUser.setPhoneNumber(dto.getPhoneNumber());
        loggedUser.setProfileImage(dto.getProfileImage());
    }

    public void changePassword(ChangePasswordRequestDTO dto) {
        if (!loggedUser.getPassword().equals(dto.getCurrentPassword())) {
            throw new RuntimeException("Old password incorrect");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        loggedUser.setPassword(dto.getNewPassword());
    }
}
