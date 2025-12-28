package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.user.RateRequestDTO;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @PostMapping("{userId}/rate-driver/{driverId}")
    public ResponseEntity<RateRequestDTO> rateDriver(@PathVariable int userId, @PathVariable int driverId, @Valid @RequestBody RateRequestDTO rateRequestDTO) {
        if (userId <= 0 || driverId <= 0) {
           return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(201).body(rateRequestDTO);
    }

    @PostMapping("{userId}/rate-vehicle/{vehicleId}")
    public ResponseEntity<RateRequestDTO> rateVehicle(@PathVariable int userId, @PathVariable int vehicleId, @Valid @RequestBody RateRequestDTO rateRequestDTO) {
        if (userId <= 0 || vehicleId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        return  ResponseEntity.status(201).body(rateRequestDTO);
    }
}
