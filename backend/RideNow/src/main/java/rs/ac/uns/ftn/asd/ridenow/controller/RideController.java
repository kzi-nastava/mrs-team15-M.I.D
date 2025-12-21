package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RideEstimateResponseDTO;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    @GetMapping("/estimate")
    public ResponseEntity<RideEstimateResponseDTO> estimateRide(@RequestParam String from, @RequestParam String to){
        RideEstimateResponseDTO response = new RideEstimateResponseDTO();
        response.setEstimatedDurationMin(24);
        return ResponseEntity.ok(response);
    }
}