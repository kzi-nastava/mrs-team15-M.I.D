package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.*;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RideResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.service.DriverService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<RideResponseDTO>> findRides() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long driverId = user.getId();

        List<RideResponseDTO> rides = driverService.findScheduledRides(driverId);
        return ResponseEntity.ok(rides);
    }

    @PostMapping(path = "/change-request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DriverChangeResponseDTO> requestDriverChange(@ModelAttribute DriverChangeRequestDTO request,
                                                                       @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) throws IOException {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user instanceof  Driver driver){
            return ResponseEntity.ok(driverService.requestDriverChanges(driver, request, profileImage));
        }
        return ResponseEntity.badRequest().build();

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

    @PutMapping("/activate-account")
    public ResponseEntity<?> activateDriverAccount(@RequestBody DriverAccountActivationRequestDTO request) {
        try {
            driverService.activateDriverAccountByToken(request);
            return ResponseEntity.ok(Map.of("message", "Account activated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to activate account"));
        }

    }

}