package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.auth.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login (@RequestBody LoginRequestDTO request){
        if(!request.getEmail().contains("@")){
            return ResponseEntity.status(401).build();
        }
        LoginResponseDTO response = new LoginResponseDTO();
        response.setId(1L);
        response.setActive(true);
        response.setEmail(request.getEmail());
        response.setFirstName("Jane");
        response.setLastName("Doe");
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam Long id){
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequestDTO request){
        if(request.getEmail() == null || request.getEmail().isEmpty()){
            return ResponseEntity.status(400).build();
        }
        if(!request.getEmail().contains("@")){
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequestDTO request){
        if (request.getNewPassword() == null || request.getConfirmNewPassword() == null
                || request.getNewPassword().isEmpty() || request.getConfirmNewPassword().isEmpty()){
            return ResponseEntity.status(400).build();
        }
        if(!request.getNewPassword().equals(request.getConfirmNewPassword())){
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody RegisterRequestDTO request){
        if (request.getEmail() == null || request.getPassword() == null || request.getConfirmPassword() == null ||
            request.getEmail().isEmpty() || request.getPassword().isEmpty() || request.getConfirmPassword().isEmpty()){
            return ResponseEntity.status(400).build();
        }
        if(!request.getPassword().equals(request.getConfirmPassword())){
            return ResponseEntity.status(400).build();
        }
        if(!request.getEmail().contains("@")){
            return ResponseEntity.status(400).build();
        }
        RegisterResponseDTO response = new RegisterResponseDTO();
        response.setId(1L);
        response.setActive(false);
        response.setEmail(request.getEmail());
        response.setFirstName(request.getFirstName());
        response.setLastName(request.getLastName());
        return ResponseEntity.status(201).body(response);
    }
}
