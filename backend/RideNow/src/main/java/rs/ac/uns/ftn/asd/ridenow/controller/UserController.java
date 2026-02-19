package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.asd.ridenow.dto.auth.RegisterResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.DriverChangeRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.passenger.RideHistoryItemDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.BlockedStatusResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.ChangePasswordRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.ReportResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UpdateProfileRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UserResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.Collection;

@Tag(name = "Users", description = "User management endpoints")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Change password", description = "Update user password with old and new password")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER', 'ADMIN')")
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO dto) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.changePassword(user.getId(), dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get user profile", description = "Retrieve user profile information")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER', 'ADMIN')")
    @GetMapping("")
    public ResponseEntity<UserResponseDTO> getUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.getUser(user));
    }

    @Operation(summary = "Update user profile", description = "Update user profile with new information and optional profile image")
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

    @Operation(summary = "Get user report", description = "Retrieve statistics and report for user's rides within date range")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    @GetMapping("/report")
    public ResponseEntity<ReportResponseDTO> getReport(@RequestParam(required = false) @Min(0) Long startDate,
                                                       @RequestParam(required = false) @Min(0) Long endDate) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.getReport(startDate, endDate, user.getId()));
    }

    @Operation(summary = "Get blocked status", description = "Check if current user is blocked and get the reason")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    @GetMapping("/blocked-status")
    public ResponseEntity<BlockedStatusResponseDTO> getBlockedStatus() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.getBlockedStatus(user.getId()));
    }
}
