package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.asd.ridenow.dto.auth.*;
import rs.ac.uns.ftn.asd.ridenow.model.ActivationToken;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.repository.ActivationTokenRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.UserRepository;
import rs.ac.uns.ftn.asd.ridenow.service.AuthService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private ActivationTokenRepository activationTokenRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        try {
            LoginResponseDTO responseDTO = authService.login(request);
            return ResponseEntity.ok().body(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam Long id) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            return ResponseEntity.status(400).build();
        }
        if (!request.getEmail().contains("@")) {
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        if (request.getNewPassword() == null || request.getConfirmNewPassword() == null
                || request.getNewPassword().isEmpty() || request.getConfirmNewPassword().isEmpty()) {
            return ResponseEntity.status(400).build();
        }
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(
            @ModelAttribute RegisterRequestDTO request,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
        try {
            RegisterResponseDTO responseDTO = authService.register(request, profileImage);
            return ResponseEntity.ok().body(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/activate")
    public ResponseEntity<?> activate(@RequestParam String token) {
        Optional<ActivationToken> optionalToken = activationTokenRepository.findByToken(token);
        if (optionalToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid token"));
        }

        ActivationToken activationToken = optionalToken.get();
        if (activationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            authService.handleExpiredActivationToken(activationToken);
            return ResponseEntity.badRequest().body(Map.of("message", "Token expired. New activation link sent to your email."));
        }

        User user = activationToken.getUser();
        user.setActive(true);
        user.setActivationToken(null);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Account activated successfully"));
    }
}