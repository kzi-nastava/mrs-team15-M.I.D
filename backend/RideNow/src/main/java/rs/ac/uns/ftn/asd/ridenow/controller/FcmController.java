package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.user.FcmTokenDTO;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing FCM (Firebase Cloud Messaging) device tokens
 *
 * This endpoint allows mobile apps to register and update their FCM tokens
 * so that push notifications can be delivered to the correct device.
 */
@RestController
@RequestMapping("/api/fcm")
public class FcmController {

    private final UserRepository userRepository;

    public FcmController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER', 'USER')")
    @PostMapping("/register-token")
    public ResponseEntity<?> registerFcmToken(@RequestBody FcmTokenDTO tokenDTO) {
            // Get current authenticated user
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            // Validate token
            if (tokenDTO.getToken() == null || tokenDTO.getToken().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "FCM token cannot be empty"));
            }

            // Update user's FCM token
            user.setFcmDeviceToken(tokenDTO.getToken());
            userRepository.save(user);

            Map<String, String> response = new HashMap<>();
            response.put("success", "true");
            response.put("message", "FCM token registered successfully");

            return ResponseEntity.ok(response);
    }
}
