package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.auth.ForgotPasswordRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.auth.LoginRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.auth.LoginResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.auth.ResetPasswordRequestDTO;

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
        return ResponseEntity.ok(response);
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
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
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
}
