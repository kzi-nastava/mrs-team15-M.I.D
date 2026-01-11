package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.user.ChangePasswordRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.NotificationResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UpdateProfileRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UserResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequestDTO dto) {

        userService.changePassword(id, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequestDTO dto) {

        userService.updateUser(id, dto);
        return ResponseEntity.status(203).build();
    }

    @GetMapping("/{id}/notifications")
    public ResponseEntity<Collection<String>> getUserNotifications(@PathVariable @NotNull Long id) {
        NotificationResponseDTO notification1 = new NotificationResponseDTO();
        notification1.setMessage("Your ride is arriving soon!");
        notification1.setTimestamp(java.time.LocalDateTime.now());

        NotificationResponseDTO notification2 = new NotificationResponseDTO();
        notification2.setMessage("Your payment was successful.");
        notification2.setTimestamp(java.time.LocalDateTime.now().minusHours(1));

        List<String> notifications = List.of(notification1.getMessage(), notification2.getMessage());
        return ResponseEntity.ok(notifications);
    }
}
