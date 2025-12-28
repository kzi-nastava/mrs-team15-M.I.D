package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.user.ChangePasswordRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UpdateProfileRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UserResponseDTO;

@Service
public class UserService {

    public void changePassword(Long userId, ChangePasswordRequestDTO dto) {
        // mock: password changed
    }

    public UserResponseDTO getUser(Long userId) {
        UserResponseDTO dto = new UserResponseDTO();
       
        dto.setEmail("user@mail.com");
        dto.setFirstName("Ana");
        dto.setLastName("Anić");
        dto.setAddress("Bulevar Oslobođenja 45");
        dto.setPhoneNumber("+381641234567");
        dto.setProfileImage("profile.png");

        return dto;
    }

    public void updateUser(Long userId, UpdateProfileRequestDTO dto) {
        // mock: profile updated
    }

    public void addToFavorites(Long userId, Long routeId) {
        // mock: route added to favorites
    }

    public void removeFromFavorites(Long userId, Long routeId) {
        // mock: route removed from favorites
    }
}
