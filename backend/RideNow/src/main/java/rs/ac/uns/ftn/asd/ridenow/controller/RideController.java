package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.CancelRideRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RideEstimateResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.StopRideResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.TrackVehicleDTO;

import rs.ac.uns.ftn.asd.ridenow.dto.ride.*;
import rs.ac.uns.ftn.asd.ridenow.dto.user.RateRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.RateResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.RegisteredUser;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.service.RideService;
import rs.ac.uns.ftn.asd.ridenow.service.RoutingService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    @Autowired
    private RoutingService routingService;

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @GetMapping("/estimate")
    public ResponseEntity<?> estimate(@RequestParam String startAddress, @RequestParam String destinationAddress){
        try{
            double[] startCoordinate = routingService.getGeocode(startAddress);
            double latStart = startCoordinate[0];
            double lonStart = startCoordinate[1];

            double[] endCoordinate = routingService.getGeocode(destinationAddress);
            double latEnd = endCoordinate[0];
            double lonEnd = endCoordinate[1];

            RideEstimateResponseDTO response = routingService.getRoute(latStart, lonStart, latEnd, lonEnd);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/stop")
    public ResponseEntity<?> stop (){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            StopRideResponseDTO response =  rideService.stopRide(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id, @RequestBody CancelRideRequestDTO request) {
        try {
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(user instanceof RegisteredUser registeredUser){
                rideService.userRideCancellation(registeredUser, id, request);
            }
            else if(user instanceof Driver driver){
                rideService.driverRideCancellation(driver, id, request);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/track")
    public ResponseEntity<TrackVehicleDTO> trackRide(@PathVariable @NotNull @Min(1) Long id){
        TrackVehicleDTO vehicle = rideService.trackRide(id);
        return ResponseEntity.ok(vehicle);
    }

    @PostMapping("/inconsistency")
    public ResponseEntity<InconsistencyResponseDTO> reportInconsistency(@RequestBody @Valid InconsistencyRequestDTO req){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        InconsistencyResponseDTO res = rideService.reportInconsistency(req, user.getId());

        return ResponseEntity.status(201).body(res);
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<RideResponseDTO> finish(@PathVariable @NotNull @Min(1) Long id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long driverId = user.getId();

        RideResponseDTO res = rideService.finishRide(id, driverId);
        return ResponseEntity.ok(res);
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
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping
    public ResponseEntity<OrderRideResponseDTO> orderRide(
            @Valid @RequestBody OrderRideRequestDTO request) {
        return ResponseEntity.status(201).body(rideService.orderRide(request));
    }

    @PostMapping("/{rideId}/rate")
    public ResponseEntity<RateResponseDTO> rateDriver(@PathVariable @NotNull @Min(1) Long rideId, @Valid @RequestBody RateRequestDTO req) {
        RateResponseDTO res = rideService.makeRating(req, rideId);
        return ResponseEntity.status(201).body(res);
    }

    @GetMapping("/my-upcoming-rides")
    public List<UpcomingRideDTO> getUpcomingRides() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = user.getId();
        return rideService.getUpcomingRidesByUser(userId);
    }

    @GetMapping("/my-current-ride")
    public CurrentRideDTO getCurrentRide(){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            return rideService.getCurrentRide(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/panic-alert")
    public ResponseEntity<?> triggerPanicAlert(){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            rideService.triggerPanicAlert(user);
            return ResponseEntity.ok(Map.of("message", "Panic alert triggered successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}