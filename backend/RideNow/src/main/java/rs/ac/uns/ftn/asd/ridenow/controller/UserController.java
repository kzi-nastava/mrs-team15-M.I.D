package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.asd.ridenow.dto.auth.RegisterResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.DriverChangeRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.ChangePasswordRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UpdateProfileRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UserResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAnyRole('USER', 'DRIVER', 'ADMIN')")
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO dto) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.changePassword(user.getId(), dto);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('USER', 'DRIVER', 'ADMIN')")
    @GetMapping("")
    public ResponseEntity<UserResponseDTO> getUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(userService.getUser(user));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping(path = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateUser(
            @ModelAttribute UpdateProfileRequestDTO request,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        try {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            userService.updateUser(user.getId(),request, profileImage);
            return ResponseEntity.status(203).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


}
