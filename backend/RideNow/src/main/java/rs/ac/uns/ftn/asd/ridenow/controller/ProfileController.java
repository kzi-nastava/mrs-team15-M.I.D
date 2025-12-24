package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.profile.UpdateUserRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.prfoile.ChangePasswordRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.service.ProfileService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ProfileResponseDTO getProfile() {
        return profileService.getProfile();
    }

    @PutMapping
    public void updateProfile(@Valid @RequestBody UpdateUserRequestDTO dto) {
        profileService.updateProfile(dto);
    }

    @PutMapping("/password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequestDTO dto) {
        profileService.changePassword(dto);
    }
}





}
