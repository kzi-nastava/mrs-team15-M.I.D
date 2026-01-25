package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RouteResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.RegisteredUser;
import rs.ac.uns.ftn.asd.ridenow.model.User;
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

    @GetMapping("/favorite-routes")
    public ResponseEntity<Collection<RouteResponseDTO>> getRoutes() {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(passengerService.getRoutes(user.getId()));
    }
}
