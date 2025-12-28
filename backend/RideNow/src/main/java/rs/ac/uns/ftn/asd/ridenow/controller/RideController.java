package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.CancelRideRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RideEstimateResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.StopRideResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.TrackVehicleDTO;

import rs.ac.uns.ftn.asd.ridenow.dto.ride.*;
import rs.ac.uns.ftn.asd.ridenow.model.Location;
import rs.ac.uns.ftn.asd.ridenow.service.RideService;

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

    @GetMapping("/{id}/track")
    public ResponseEntity<TrackVehicleDTO> trackRide(@PathVariable @NotNull @Min(1) Long id){
        TrackVehicleDTO vehicle = new TrackVehicleDTO();
        vehicle.setLocation(new Location(0L, 12.223, 45.334, "Bulevar Oslobodjenja 20, Novi Sad"));
        vehicle.setRemainingTimeInMinutes(12);

        return ResponseEntity.ok(vehicle);
    }

    @PostMapping("/{id}/inconsistency")
    public ResponseEntity<InconsistencyResponseDTO> reportInconsistency(@PathVariable @NotNull @Min(1) Long id, @RequestBody @Valid InconsistencyRequestDTO req){
        InconsistencyResponseDTO res = new InconsistencyResponseDTO();
        res.setRideId(id);
        res.setDescription(req.getDescription());
        res.setDriverId(req.getDriverId());
        res.setPassengerId(req.getPassengerId());

        return ResponseEntity.status(201).body(res);
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<Void> startRide(@PathVariable Long id) {
        rideService.startRide(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/estimate-route")
    public ResponseEntity<RouteResponseDTO> estimateRoute(
            @Valid @RequestBody EstimateRouteRequestDTO dto) {

        RouteResponseDTO response = rideService.estimateRoute(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<OrderRideResponseDTO> orderRide(
            @Valid @RequestBody OrderRideRequestDTO request) {
        return ResponseEntity.ok(rideService.orderRide(request));
    }
}