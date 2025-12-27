package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.CancelRideRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RideEstimateResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.StopRideResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.TrackVehicleDTO;

import javax.sound.midi.Track;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.*;
import rs.ac.uns.ftn.asd.ridenow.service.RideService;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.StopRideResponseDTO;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

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


    @PostMapping("/route")
    public ResponseEntity<RouteResponseDTO> estimateRoute(
            @Valid @RequestBody EstimateRouteRequestDTO dto) {

        RouteResponseDTO response = rideService.estimateRoute(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> orderRide(
            @Valid @RequestBody OrderRideRequestDTO request) {

        rideService.orderRide(request);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{id}/finish")
    public ResponseEntity<RideResponseDTO> finish(@PathVariable Long id){
        if (id <= 0){
            return ResponseEntity.status(400).build();
        }

        RideResponseDTO response = new RideResponseDTO();
        response.setRideId(1L);
        response.setStartLocation("Bulevar Oslobodjenja 45, Novi Sad");
        response.setEndLocation("Narodnog fronta 12, Novi Sad");
        response.setStartLatitude(21.23);
        response.setStartLongitude(45.43);
        response.setEndLatitude(22.23);
        response.setEndLongitude(46.43);
        response.setPassengerEmails(List.of("marko.maric@gmail.com", "ana.danic@gmail.com"));
        response.setStartTime("2024-05-10T14:30:00");

        return  ResponseEntity.ok(response);
    }

    @GetMapping("/{driverId}")
    public ResponseEntity<List<RideResponseDTO>> findRides(@PathVariable Long driverId){
        if (driverId <= 0){
            return ResponseEntity.status(400).build();
        }

        RideResponseDTO ride1 = new RideResponseDTO();
        ride1.setRideId(1L);
        ride1.setStartLocation("Bulevar Oslobodjenja 45, Novi Sad");
        ride1.setEndLocation("Narodnog fronta 12, Novi Sad");
        ride1.setStartLatitude(21.23);
        ride1.setStartLongitude(45.43);
        ride1.setEndLatitude(22.23);
        ride1.setEndLongitude(46.43);
        ride1.setPassengerEmails(List.of("danka.danic@gmail.com", "mario.ploros@gmail.com"));
        ride1.setStartTime("2024-05-10T14:30:00");

        RideResponseDTO ride2 = new RideResponseDTO();
        ride2.setRideId(2L);
        ride2.setStartLocation("Trg Slobode 3, Novi Sad");
        ride2.setEndLocation("Bulevar Evrope 28, Novi Sad");
        ride2.setStartLatitude(23.23);
        ride2.setStartLongitude(47.43);
        ride2.setEndLatitude(24.23);
        ride2.setEndLongitude(48.43);
        ride2.setPassengerEmails(List.of("radovan.radinic@gmail.com", "galja.miric@g"));
        ride2.setStartTime("2024-05-11T09:15:00");
        List<RideResponseDTO> rides = List.of(ride1, ride2);
        return ResponseEntity.ok(rides);
    }


}