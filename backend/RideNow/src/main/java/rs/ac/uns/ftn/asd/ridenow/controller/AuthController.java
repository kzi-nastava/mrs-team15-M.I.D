package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.asd.ridenow.dto.auth.*;
import rs.ac.uns.ftn.asd.ridenow.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

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

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(
            @ModelAttribute RegisterRequestDTO request,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage){
        try{
            RegisterResponseDTO responseDTO = authService.register(request, profileImage);
            return ResponseEntity.ok().body(responseDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}