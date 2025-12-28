package rs.ac.uns.ftn.asd.ridenow.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.route.RouteResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.service.PassengerService;
import rs.ac.uns.ftn.asd.ridenow.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/api/passengers")
public class PassengerController {

    private PassengerService passengerService;

    @Autowired
    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @PutMapping("/{id}/routes/{routeId}")
    public ResponseEntity<Void> addToFavorites(
            @PathVariable Long id,
            @PathVariable Long routeId) {

        passengerService.addToFavorites(id, routeId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/routes/{routeId}")
    public ResponseEntity<Void> removeFromFavorites(
            @PathVariable Long id,
            @PathVariable Long routeId) {

        passengerService.removeFromFavorites(id, routeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/routes/{routeId}")
    public ResponseEntity<RouteResponseDTO> getRouteById(
            @PathVariable Long id,
            @PathVariable Long routeId) {

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/routes")
    public ResponseEntity<Collection<RouteResponseDTO>> getRoutes(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "false") boolean favorite) {

        return ResponseEntity.ok().build();
    }
}
