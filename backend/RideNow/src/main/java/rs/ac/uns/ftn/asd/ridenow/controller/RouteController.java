package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.route.RouteResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.service.RouteService;

import java.util.Collection;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping("/favorites/{routeId}")
    public ResponseEntity<Void> addToFavorites(@PathVariable Long routeId) {
        routeService.addToFavorites(routeId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/favorites/{routeId}")
    public ResponseEntity<Void> removeFromFavorites(@PathVariable Long routeId) {
        routeService.removeFromFavorites(routeId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/favorites")
    public ResponseEntity<Collection<RouteResponseDTO>> getFavorites() {
        return ResponseEntity.ok(routeService.getFavoriteRoutes());
    }
}
