package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.profile.UpdateProfileRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.profile.ChangePasswordRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.profile.UserResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.service.ProfileService;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private ProfileService profileService;

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }


    @GetMapping
    public UserResponseDTO getProfile() {
        return profileService.getProfile();
    }

    @PutMapping
    public ResponseEntity<Void> updateProfile(
            @Valid @RequestBody UpdateProfileRequestDTO dto) {

        profileService.updateProfile(dto);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO dto) {

        profileService.changePassword(dto);
        return ResponseEntity.ok().build();
    }

}
