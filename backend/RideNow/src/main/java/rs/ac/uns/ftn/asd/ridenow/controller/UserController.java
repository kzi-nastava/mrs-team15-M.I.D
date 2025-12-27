package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.user.RateRequestDTO;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @PostMapping("/rate-driver/{id}")
    public ResponseEntity<Void> rateDriver(@PathVariable int id, @Valid @RequestBody RateRequestDTO rateRequestDTO) {
        if (id <= 0) {
           return ResponseEntity.badRequest().build();
        }
        return  ResponseEntity.ok().build();
    }

    @PostMapping("/rate-vehicle/{id}")
    public ResponseEntity<Void> rateVehicle(@PathVariable int id, @Valid @RequestBody RateRequestDTO rateRequestDTO) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return  ResponseEntity.ok().build();
    }
}
