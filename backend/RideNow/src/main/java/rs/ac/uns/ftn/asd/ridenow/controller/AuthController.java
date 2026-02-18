package rs.ac.uns.ftn.asd.ridenow.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.asd.ridenow.dto.auth.*;
import rs.ac.uns.ftn.asd.ridenow.model.ActivationToken;
import rs.ac.uns.ftn.asd.ridenow.model.ForgotPasswordToken;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.repository.ActivationTokenRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.ForgotPasswordTokenRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.UserRepository;
import rs.ac.uns.ftn.asd.ridenow.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Tag(name = "Authentication", description = "User authentication endpoints")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private ActivationTokenRepository activationTokenRepository;
    @Autowired
    private  ForgotPasswordTokenRepository forgotPasswordTokenRepository;
    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            LoginResponseDTO responseDTO = authService.login(request);
            return ResponseEntity.ok().body(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "User logout", description = "Invalidate user session and JWT token")
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDTO> logout(){
        try{
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            authService.logout(user);
            LogoutResponseDTO response = new LogoutResponseDTO("Logged out successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LogoutResponseDTO response = new LogoutResponseDTO(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Forgot password", description = "Initiate forgot password process by sending reset link to user's email")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        try {
            authService.forgotPassword(request);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Reset password", description = "Reset user password using token from email link")
    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestParam String token, @RequestBody ResetPasswordRequestDTO request) {
        Optional<ForgotPasswordToken> optionalToken = forgotPasswordTokenRepository.findByToken(token);
        if (optionalToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid token"));
        }

        ForgotPasswordToken forgotPasswordToken = optionalToken.get();
        if (forgotPasswordToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            authService.handleExpiredForgotPasswordToken(forgotPasswordToken);
            return ResponseEntity.badRequest().body(Map.of("message", "Token expired. New activation link sent to your email."));
        }

        User user = forgotPasswordToken.getUser();
        user.setForgotPasswordToken(null);
        userRepository.save(user);
        authService.resetPassword(user, request);
        return ResponseEntity.ok(Map.of("message", "Your password has been successfully updated."));
    }

    @Operation(summary = "User registration", description = "Register a new user with profile information and optional profile image")
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(@Valid
            @ModelAttribute RegisterRequestDTO request,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
        try {
            RegisterResponseDTO responseDTO = authService.register(request, profileImage);
            return ResponseEntity.ok().body(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Activate account", description = "Activate user account using token from email link")
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

    @Operation(summary = "Verify reset code", description = "Verify the reset code sent to user's email for password reset")
    @PostMapping("/verify-reset-code")
    public ResponseEntity<?> verifyResetCode(@Valid @RequestBody VerifyCodeRequestDTO request) {
        Optional<ForgotPasswordToken> optionalToken =  forgotPasswordTokenRepository.findByVerificationCodeAndUser_Email(request.getCode(), request.getEmail());
        if (optionalToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid code or email"));
        }

        ForgotPasswordToken forgotPasswordToken = optionalToken.get();
        if (forgotPasswordToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            authService.handleExpiredForgotPasswordToken(forgotPasswordToken);
            return ResponseEntity.badRequest().body(Map.of("message", "Code expired. New code sent to your email."));
        }
        return ResponseEntity.ok(Map.of("message", "Code verified","token", forgotPasswordToken.getToken()));
    }

    @Operation(summary = "Verify activation code", description = "Verify the activation code sent to user's email for account activation")
    @PutMapping("/activate-code")
    public ResponseEntity<?> activateCode(@Valid @RequestBody VerifyCodeRequestDTO request) {
        Optional<ActivationToken> optionalToken = activationTokenRepository.findByVerificationCodeAndUser_Email(request.getCode(), request.getEmail());
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

    @Operation(summary = "Resend activation email", description = "Resend account activation email with new token if previous token expired or lost")
    @PostMapping("/resend-activation-email")
    public ResponseEntity<?> resendActivationEmail(@Valid @RequestBody ActivateAccountRequestDTO request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }
        User user = optionalUser.get();
        if (user.isActive()) {
            return ResponseEntity.badRequest().body("Account is already activated");
        }
        authService.sendActivationEmail(user);
        return ResponseEntity.ok().build();
    }
}