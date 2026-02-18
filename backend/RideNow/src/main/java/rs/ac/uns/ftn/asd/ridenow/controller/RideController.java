package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
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
import rs.ac.uns.ftn.asd.ridenow.service.PanicAlertService;
import rs.ac.uns.ftn.asd.ridenow.service.RideService;
import rs.ac.uns.ftn.asd.ridenow.service.RoutingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;
import java.util.Map;

@Tag(name = "Rides", description = "Ride management endpoints")
@RestController
@RequestMapping("/api/rides")
public class RideController {

    @Autowired
    private RoutingService routingService;

    private final RideService rideService;

    @Autowired
    private PanicAlertService panicAlertService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @Operation(summary = "Estimate ride cost and duration", description = "Calculate ride cost and duration based on start and end coordinates")
    @GetMapping("/estimate")
    public ResponseEntity<?> estimate(@RequestParam Double startLatitude, @RequestParam Double startLongitude,
                                      @RequestParam Double endLatitude, @RequestParam Double endLongitude) {
        try {
            RideEstimateResponseDTO response = routingService.getRoute(startLatitude,startLongitude,endLatitude,endLongitude
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Stop current ride", description = "Driver stops the current ride in progress")
    @PreAuthorize("hasRole('DRIVER')")
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

    @Operation(summary = "Cancel a ride", description = "Allow user or driver to cancel a scheduled or ongoing ride")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
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

    @Operation(summary = "Track vehicle location", description = "Get real-time vehicle location and details for an ongoing ride")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}/track")
    public ResponseEntity<TrackVehicleDTO> trackRide(@PathVariable @NotNull @Min(1) Long id){
        TrackVehicleDTO vehicle = rideService.trackRide(id);
        return ResponseEntity.ok(vehicle);
    }

    @Operation(summary = "Report ride inconsistency", description = "Report any issues or discrepancies during a ride")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/inconsistency")
    public ResponseEntity<InconsistencyResponseDTO> reportInconsistency(@RequestBody @Valid InconsistencyRequestDTO req){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        InconsistencyResponseDTO res = rideService.reportInconsistency(req, user.getId());

        return ResponseEntity.status(201).body(res);
    }

    @Operation(summary = "Finish a ride", description = "Driver marks a ride as completed")
    @PreAuthorize("hasRole('DRIVER')")
    @PostMapping("/{id}/finish")
    public ResponseEntity<Boolean> finish(@PathVariable @NotNull @Min(1) Long id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long driverId = user.getId();

        Boolean nextAvailable = rideService.finishRide(id, driverId);
        return ResponseEntity.ok(nextAvailable);
    }

    @Operation(summary = "Start a ride", description = "Driver starts a scheduled ride")
    @PreAuthorize("hasRole('DRIVER')")
    @PutMapping("/{id}/start")
    public ResponseEntity<Void> startRide(@PathVariable Long id) {
        rideService.startRide(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get passenger pickup details", description = "Retrieve passenger information and pickup confirmation for a ride")
    @PreAuthorize("hasRole('DRIVER')")
    @GetMapping("/{id}/start")
    public ResponseEntity<StartRideResponseDTO> passangerPickup(@PathVariable Long id) {
        return ResponseEntity.ok(rideService.passangerPickup(id));
    }


    @Operation(summary = "Estimate route details", description = "Calculate route with stops, distance, and time estimates")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/estimate-route")
    public ResponseEntity<RouteResponseDTO> estimateRoute(
            @Valid @RequestBody EstimateRouteRequestDTO dto) {
        try {
            RouteResponseDTO response = rideService.estimateRoute(dto);
            return ResponseEntity.status(201).body(response);

        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Order a ride", description = "User requests and orders a new ride")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/order-ride")
    public ResponseEntity<?> orderRide(
            @Valid @RequestBody OrderRideRequestDTO request) {
        User user = (User)  SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = user.getEmail();
        try{
            return ResponseEntity.status(201).body(rideService.orderRide(request, email));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @Operation(summary = "Rate driver", description = "User rates the driver after ride completion")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{rideId}/rate")
    public ResponseEntity<RateResponseDTO> rateDriver(@PathVariable @NotNull @Min(1) Long rideId, @Valid @RequestBody RateRequestDTO req) {
        RateResponseDTO res = rideService.makeRating(req, rideId);
        return ResponseEntity.status(201).body(res);
    }

    @Operation(summary = "Get upcoming rides", description = "Retrieve list of upcoming rides for the current user")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    @GetMapping("/my-upcoming-rides")
    public List<UpcomingRideDTO> getUpcomingRides() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = user.getId();
        return rideService.getUpcomingRidesByUser(userId);
    }

    @Operation(summary = "Get current ride", description = "Retrieve details of the user's current active ride")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    @GetMapping("/my-current-ride")
    public CurrentRideDTO getCurrentRide(){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            return rideService.getCurrentRide(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Operation(summary = "Trigger panic alert", description = "User or driver triggers a panic alert during a ride")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER')")
    @PostMapping("/panic-alert")
    public ResponseEntity<?> triggerPanicAlert(){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            panicAlertService.triggerPanicAlert(user);
            return ResponseEntity.ok(Map.of("message", "Panic alert triggered successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @Operation(summary = "Get active rides", description = "Admin retrieves all currently active rides in the system")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/active-rides")
    public ResponseEntity<List<ActiveRideDTO>> getActiveRides() {
        return ResponseEntity.ok(rideService.getActiveRides());
    }

    @Operation(summary = "Reorder a ride", description = "User or admin reorders a previously completed ride with same route")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/reorder-ride")
    public ResponseEntity<?> reorderRide(@RequestBody ReorderRideRequestDTO request) {
        try {
            rideService.reorderRide(request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}