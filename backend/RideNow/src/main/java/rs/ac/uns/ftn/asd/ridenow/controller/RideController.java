package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.CancelRideRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RideEstimateResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.StopRideResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.TrackVehicleDTO;

import rs.ac.uns.ftn.asd.ridenow.dto.ride.*;
import rs.ac.uns.ftn.asd.ridenow.dto.user.RateRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.RateResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.service.RideService;
import rs.ac.uns.ftn.asd.ridenow.service.RoutingService;

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
            System.out.println(latStart);
            double lonStart = startCoordinate[1];
            System.out.println(lonStart);


            double[] endCoordinate = routingService.getGeocode(destinationAddress);
            double latEnd = endCoordinate[0];
            System.out.println(latEnd);
            double lonEnd = endCoordinate[1];
            System.out.println(lonEnd);


            RideEstimateResponseDTO response = routingService.getRoute(latStart, lonStart, latEnd, lonEnd);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/stop")
    public ResponseEntity<StopRideResponseDTO> stop (@PathVariable Long id){
        StopRideResponseDTO response = new StopRideResponseDTO();
        response.setEndLocation("Bulevar Oslobodjenja 24, Novi Sad");
        response.setPrice(570);
        return  ResponseEntity.ok().body(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id, @RequestBody CancelRideRequestDTO request){
        if(request.getReason() == null || request.getReason().isEmpty()){
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.status(204).build();
    }

    @GetMapping("/{id}/track")
    public ResponseEntity<TrackVehicleDTO> trackRide(@PathVariable @NotNull @Min(1) Long id){
        TrackVehicleDTO vehicle = rideService.trackRide(id);
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
}