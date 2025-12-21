package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.auth.LoginRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.auth.LoginResponseDTO;

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
}
