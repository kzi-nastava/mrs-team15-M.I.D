package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.*;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RideResponseDTO;
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
    public ResponseEntity<List<DriverHistoryItemDTO>> getRideHistory() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long id = user.getId();

        List<DriverHistoryItemDTO> history = driverService.getDriverHistory(id);

        return ResponseEntity.ok(history);
    }

    @GetMapping("/rides")
    public ResponseEntity<List<RideResponseDTO>> findRides() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long driverId = user.getId();

        List<RideResponseDTO> rides = driverService.findScheduledRides(driverId);
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