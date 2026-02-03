package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.passenger.RideHistoryItemDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.FavoriteRouteResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RouteResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.service.PassengerService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/passengers")
public class PassengerController {

    private final PassengerService passengerService;

    @Autowired
    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }


    @PreAuthorize("hasRole('USER')")
    @PutMapping("/favorite-routes/{routeId}")
    public ResponseEntity<RouteResponseDTO> addToFavorites(
            @PathVariable Long routeId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long id = user.getId();
        return ResponseEntity.ok(passengerService.addToFavorites(id, routeId));
    }


    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/favorite-routes/{routeId}")
    public ResponseEntity<Void> removeFromFavorites(
            @PathVariable Long routeId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long id = user.getId();
        passengerService.removeFromFavorites(id, routeId);
        return ResponseEntity.status(204).build();
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/favorite-routes")
    public ResponseEntity<Collection<FavoriteRouteResponseDTO>> getRoutes() {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(passengerService.getRoutes(user.getId()));
    }


    @PreAuthorize("hasRole('USER')")
    @GetMapping("/favorite-routes/{id}")
    public ResponseEntity<RouteResponseDTO> getRoute(@PathVariable Long id) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(passengerService.getRoute(user.getId(), id));
    }


    @PreAuthorize("hasRole('USER')")
    @GetMapping("/ride-history")
    public ResponseEntity<List<RideHistoryItemDTO>> getRideHistory(@RequestParam(required = false) String dateFrom, @RequestParam(required = false) String dateTo,
                                                                   @RequestParam(required = false) String sortBy, @RequestParam(required = false) String sortDirection){
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok().body(passengerService.getRideHistory(user.getId(),dateFrom, dateTo, sortBy, sortDirection));
    }
}
