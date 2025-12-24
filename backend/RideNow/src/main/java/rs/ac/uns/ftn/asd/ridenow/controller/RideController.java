package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.CancelRideRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RideEstimateResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.StopRideResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.TrackVehicleDTO;

import javax.sound.midi.Track;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    @GetMapping("/estimate")
    public ResponseEntity<RideEstimateResponseDTO> estimate(@RequestParam String startAddress, @RequestParam String destinationAddress){
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

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id, @RequestBody CancelRideRequestDTO request){
        if(request.getReason() == null || request.getReason().isEmpty()){
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.status(204).build();
    }

    @PostMapping("/{id}/track")
    public ResponseEntity<TrackVehicleDTO> trackRide(@PathVariable Long id){
        if (id == null || id <= 0){
            return ResponseEntity.status(400).build();
        }

        TrackVehicleDTO vehicle = new TrackVehicleDTO();
        vehicle.setLatitude(45.2671);
        vehicle.setLongitude(19.8335);
        vehicle.setRemainingTimeInMinutes(12);

        return ResponseEntity.ok(vehicle);
    }

    @PostMapping("/{id}/inconsistency")
    public ResponseEntity<Void> reportInconsistency(@PathVariable Long id, @RequestParam String description) {
        if (description == null || description.isEmpty()) {
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.status(204).build();
    }
}