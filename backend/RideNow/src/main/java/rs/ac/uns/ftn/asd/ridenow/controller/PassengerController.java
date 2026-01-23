package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RouteResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.service.PassengerService;

import java.util.ArrayList;
import java.util.Collection;

@RestController
@RequestMapping("/api/passengers")
public class PassengerController {

    private final PassengerService passengerService;

    @Autowired
    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @PutMapping("/{id}/routes/{routeId}")
    public ResponseEntity<RouteResponseDTO> addToFavorites(
            @PathVariable Long id,
            @PathVariable Long routeId) {

        return ResponseEntity.ok(passengerService.addToFavorites(id, routeId));
    }

    @DeleteMapping("/{id}/routes/{routeId}")
    public ResponseEntity<Void> removeFromFavorites(
            @PathVariable Long id,
            @PathVariable Long routeId) {

        passengerService.removeFromFavorites(id, routeId);
        return ResponseEntity.status(204).build();
    }

    @GetMapping("/{id}/routes/{routeId}")
    public ResponseEntity<RouteResponseDTO> getRouteById(
            @PathVariable Long id,
            @PathVariable Long routeId) {

        RouteResponseDTO dto = new RouteResponseDTO();
        dto.setRouteId(routeId);
        dto.setDistanceKm(14.0);
        dto.setEstimatedTimeMinutes(25);
        dto.setPriceEstimateStandard(1800);

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{userId}/routes")
    public ResponseEntity<Collection<RouteResponseDTO>> getRoutes(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "false") boolean favorite) {

        Collection<RouteResponseDTO> collection = new ArrayList<>();
        RouteResponseDTO dto1 = new RouteResponseDTO();
        dto1.setRouteId(1L);
        dto1.setDistanceKm(10.5);
        dto1.setEstimatedTimeMinutes(20);
        dto1.setPriceEstimateStandard(1500);
        collection.add(dto1);
        RouteResponseDTO dto2 = new RouteResponseDTO();
        dto2.setRouteId(2L);
        dto2.setDistanceKm(25.0);
        dto2.setEstimatedTimeMinutes(40);
        dto2.setPriceEstimateStandard(3000);
        collection.add(dto2);
        return ResponseEntity.ok(collection);
    }
}
