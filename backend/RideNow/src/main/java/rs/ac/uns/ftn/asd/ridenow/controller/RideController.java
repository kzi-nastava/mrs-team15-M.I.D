package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RideEstimateResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.StopRideResponseDTO;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    @GetMapping("/estimate")
    public ResponseEntity<RideEstimateResponseDTO> estimateRide(@RequestParam String startAddress, @RequestParam String destinationAddress){
        RideEstimateResponseDTO response = new RideEstimateResponseDTO();
        response.setEstimatedDurationMin(24);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<StopRideResponseDTO> stop (@PathVariable Long id){
        StopRideResponseDTO response = new StopRideResponseDTO();
        response.setEndLocation("Bulevar Oslobodjenja 24, Novi Sad");
        response.setPrice(570);
        return  ResponseEntity.ok(response);
    }

}