package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.*;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RideResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.UpcomingRideDTO;
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.service.DriverService;

import java.util.List;

@RestController
@RequestMapping("/api/driver")
public class DriverController {

    private final DriverService driverService;

    @Autowired
    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping("/ride-history")
    public ResponseEntity<Page<DriverHistoryItemDTO>> getRideHistory(@RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int size,
                                                                     @RequestParam(defaultValue = "date") String sortBy,
                                                                     @RequestParam(defaultValue = "desc") String sortDir) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long id = user.getId();

        Page<DriverHistoryItemDTO> history;

        if ("passengers".equals(sortBy)) {
            history = driverService.getDriverHistory(id,PageRequest.of(page, size), "passengers",  sortDir);
        } else if ("duration".equals(sortBy)) {
            history = driverService.getDriverHistory(id, PageRequest.of(page, size), "duration",  sortDir);
        } else {
            // Use standard sorting for other fields
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), mapSortField(sortBy));
            Pageable pageable = PageRequest.of(page, size, sort);
            history = driverService.getDriverHistory(id, pageable, "", "");
        }

        return ResponseEntity.ok(history);
    }
  
      private String mapSortField(String sortBy) {
        return switch (sortBy) {
            case "route" -> "route.startLocation.address";
            case "passengers" -> "passengers.user.firstName";
            case "date" -> "scheduledTime";
            case "cancelled" -> "cancelled";
            case "duration" -> "endTime";
            case "cost" -> "price";
            case "panic" -> "panicAlert";
            default -> "scheduledTime";
        };
    }

    @GetMapping("/rides")
    public ResponseEntity<List<UpcomingRideDTO>> findRides() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long driverId = user.getId();

        List<UpcomingRideDTO> rides = driverService.findScheduledRides(driverId);
        return ResponseEntity.ok(rides);
    }

    @PostMapping("/{id}/change-request")
    public ResponseEntity<DriverChangeResponseDTO> requestDriverChange(@PathVariable @NotNull @Min(1) Long id,
                                                                       @RequestBody @NotNull DriverChangeRequestDTO request) {
        return ResponseEntity.ok(driverService.requestDriverChanges(id, request));
    }

    @PutMapping("/change-status")
    public ResponseEntity<?> changeDriverStatus(@RequestBody DriverStatusRequestDTO request) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user instanceof  Driver driver){
            driverService.changeDriverStatus(driver, request);
            DriverStatusResponseDTO response = new DriverStatusResponseDTO();
            response.setStatus(driver.getStatus());
            response.setPendingStatus(driver.getPendingStatus());
            return ResponseEntity.ok().body(response);
        }
        return ResponseEntity.badRequest().body("Driver does not exists");
    }

    @GetMapping("/status")
    public ResponseEntity<?> getDriverStatus() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user instanceof  Driver driver){
            DriverStatusResponseDTO response = new DriverStatusResponseDTO();
            response.setStatus(driver.getStatus());
            response.setPendingStatus(driver.getPendingStatus());
            return ResponseEntity.ok().body(response);
        }
        return ResponseEntity.badRequest().body("Driver does not exists");
    }
}