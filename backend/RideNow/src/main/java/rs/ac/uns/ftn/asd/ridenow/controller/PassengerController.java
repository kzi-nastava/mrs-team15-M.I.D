package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.passenger.RideHistoryItemDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.FavoriteRouteResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RouteResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.service.PassengerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.Collection;

@Tag(name = "Passenger", description = "Passenger management endpoints")
@RestController
@RequestMapping("/api/passengers")
public class PassengerController {

    private final PassengerService passengerService;

    @Autowired
    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @Operation(summary = "Get favorite routes", description = "Retrieve passenger's saved favorite routes")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/favorite-routes")
    public ResponseEntity<Collection<FavoriteRouteResponseDTO>> getRoutes() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(passengerService.getRoutes(user.getId()));
    }

    @Operation(summary = "Add route to favorites", description = "Save a route as favorite for quick access")
    @PostMapping("/favorite-routes/{routeId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RouteResponseDTO> addToFavorites(
            @PathVariable Long routeId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long id = user.getId();
        return ResponseEntity.ok(passengerService.addToFavorites(id, routeId));
    }

    @Operation(summary = "Remove route from favorites", description = "Delete a route from passenger's favorite list")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/favorite-routes/{routeId}")
    public ResponseEntity<Void> removeFromFavorites(
            @PathVariable Long routeId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long id = user.getId();
        passengerService.removeFromFavorites(id, routeId);
        return ResponseEntity.status(204).build();
    }

    @Operation(summary = "Get favorite route details", description = "Retrieve details of a specific favorite route by ID")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/favorite-routes/{id}")
    public ResponseEntity<RouteResponseDTO> getRoute(@PathVariable Long id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(passengerService.getRoute(user.getId(), id));
    }

    @Operation(summary = "Get ride history", description = "Retrieve passenger's past rides with pagination and sorting")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/ride-history")
    public ResponseEntity<Page<RideHistoryItemDTO>> getRideHistory(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
                                                                   @RequestParam(defaultValue = "date") String sortBy, @RequestParam(defaultValue = "desc") String sortDir,
                                                                   @RequestParam(required = false) @Min(0) Long date) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Page<RideHistoryItemDTO> history;

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), mapSortField(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);
        history = passengerService.getRideHistory(user, pageable, date);
        return ResponseEntity.ok(history);
    }

    private String mapSortField(String sortBy) {
        return switch (sortBy) {
            case "route" -> "route.startLocation.address";
            case "startTime" -> "startTime";
            case "endTime" -> "endTime";
            default -> "scheduledTime";
        };
    }
}
