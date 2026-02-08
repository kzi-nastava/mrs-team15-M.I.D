package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.*;
import jakarta.validation.Valid;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.DriverStatusResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.UserResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.Administrator;
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.User;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverChangesStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;
import rs.ac.uns.ftn.asd.ridenow.service.AdminService;
import rs.ac.uns.ftn.asd.ridenow.service.DriverService;
import rs.ac.uns.ftn.asd.ridenow.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @Autowired
    public AdminController(AdminService adminService, UserService userService) {
        this.userService = userService;
        this.adminService = adminService;
    }

    @GetMapping("/users/{id}/rides")
    public ResponseEntity<List<RideHistoryItemDTO>> getRideHistory(@PathVariable Long id,
           @RequestParam(required = false) String dateFrom, @RequestParam(required = false) String dateTo,
           @RequestParam(required = false) String sortBy, @RequestParam(required = false) String sortDirection){
        List<RideHistoryItemDTO> rides = new ArrayList<>();

        RideHistoryItemDTO firstRide = new RideHistoryItemDTO();
        firstRide.setId(1L);
        firstRide.setStartAddress("Bulevar Oslobođenja 45, Novi Sad");
        firstRide.setEndAddress("Narodnog fronta 12, Novi Sad");
        firstRide.setStartTime(LocalDateTime.of(2025, 5, 10, 14, 30));
        firstRide.setEndTime(LocalDateTime.of(2025, 5, 10, 14, 50));
        firstRide.setCancelled(false);
        firstRide.setCancelledBy(null);
        firstRide.setPrice(520.00);
        firstRide.setPanicTriggered(false);

        RideHistoryItemDTO secondRide = new RideHistoryItemDTO();
        secondRide.setId(2L);
        secondRide.setStartAddress("Trg slobode 3, Novi Sad");
        secondRide.setEndAddress("Bulevar Evrope 28, Novi Sad");
        secondRide.setStartTime(LocalDateTime.of(2025, 5, 11, 9, 15));
        secondRide.setEndTime(LocalDateTime.of(2025, 5, 11, 9, 40));
        secondRide.setCancelled(true);
        secondRide.setCancelledBy("PASSENGER");
        secondRide.setPrice(0.00);
        secondRide.setPanicTriggered(false);

        rides.add(firstRide);
        rides.add(secondRide);
        return ResponseEntity.ok().body(rides);
    }

    @GetMapping("/rides/{id}")
    public ResponseEntity<RideDetailsDTO> getRideDetails(@PathVariable Long id){
        RideDetailsDTO details = new RideDetailsDTO();
        details.setRideId(1L);
        details.setRoute("Bulevar Oslobođenja 45 → Narodnog fronta 12, Novi Sad");
        details.setDriver("Marko Marković");
        details.setPassenger("Ana Anić");
        details.setPrice(520.00);
        details.setPanicTriggered(false);
        details.setInconsistencies(null);
        details.setRating(4.8);
        return ResponseEntity.ok().body(details);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/driver-register")
    public ResponseEntity<RegisterDriverResponseDTO> register(
            @Valid @RequestBody RegisterDriverRequestDTO request) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.status(201).body(adminService.register(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/driver-requests")
    public ResponseEntity<List<DriverChangeRequestDTO>> getDriverRequests() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(adminService.getDriverRequests());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("driver-requests/{requestId}")
    public ResponseEntity<Void> reviewDriverRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody AdminChangesReviewRequestDTO dto) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long id = user.getId();
        adminService.reviewDriverRequest(id, requestId, dto);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/price-configs")
    public ResponseEntity<PriceConfigResponseDTO> getPriceConfigs() {
        return ResponseEntity.ok(adminService.getPriceConfigs());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/price-configs")
    public ResponseEntity<Void> updatePriceConfigs(@Valid @RequestBody PriceConfigRequestDTO request) {
        adminService.updatePriceConfigs(request);
        return ResponseEntity.ok().build();
    }
}
