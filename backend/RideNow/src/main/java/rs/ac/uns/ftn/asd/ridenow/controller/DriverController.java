package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.DriverChangeRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.DriverChangeResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.DriverHistoryItemDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RideResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.Location;
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

    @PostMapping("/{id}/finish")
    public ResponseEntity<RideResponseDTO> finish(@PathVariable @NotNull @Min(1) Long id) {
        RideResponseDTO response = new RideResponseDTO();
        response.setRideId(1L);
        Location startLocation = new Location(45.2671, 19.8335, "Bulevar Oslobodjenja 45, Novi Sad");
        Location endLocation = new Location(45.2550, 19.8450, "Narodnog fronta 12, Novi Sad");
        Location stopLocation1 = new Location(21.54534, 23.5435345, "Bulevar Evrope 22, Novi Sad");
        // Location startLocation = new Location(12L, 45.2671, 19.8335, "Bulevar
        // Oslobodjenja 45, Novi Sad");
        // Location endLocation = new Location(15L, 45.2550, 19.8450, "Narodnog fronta
        // 12, Novi Sad");
        // Location stopLocation1 = new Location(82L, 21.54534, 23.5435345, "Bulevar
        // Evrope 22, Novi Sad");
        // response.setRoute(new Route(13L, startLocation, endLocation,
        // List.of(stopLocation1), 5, 15));
        response.setPassengerEmails(List.of("marko.maric@gmail.com", "ana.danic@gmail.com"));
        response.setStartTime("2024-05-10T14:30:00");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{driverId}/rides")
    public ResponseEntity<List<RideResponseDTO>> findRides(@PathVariable @NotNull @Min(1) Long driverId) {
        RideResponseDTO ride1 = new RideResponseDTO();
        ride1.setRideId(1L);
        Location startLocation = new Location(45.2671, 19.8335, "Bulevar Oslobodjenja 45, Novi Sad");
        Location endLocation = new Location(45.2550, 19.8450, "Narodnog fronta 12, Novi Sad");
        Location stopLocation1 = new Location(21.54534, 23.5435345, "Bulevar Evrope 22, Novi Sad");
        // ride1.setRideId(1L);
        // Location startLocation = new Location(12L, 45.2671, 19.8335, "Bulevar
        // Oslobodjenja 45, Novi Sad");
        // Location endLocation = new Location(15L, 45.2550, 19.8450, "Narodnog fronta
        // 12, Novi Sad");
        // Location stopLocation1 = new Location(82L, 21.54534, 23.5435345, "Bulevar
        // Evrope 22, Novi Sad");
        // ride1.setRoute(new Route(13L, startLocation, endLocation,
        // List.of(stopLocation1), 5, 15));
        ride1.setPassengerEmails(List.of("danka.danic@gmail.com", "mario.ploros@gmail.com"));
        ride1.setStartTime("2024-05-10T14:30:00");

        RideResponseDTO ride2 = new RideResponseDTO();
        ride2.setRideId(2L);
        Location startLocation2 = new Location(45.2671, 19.8335, "Bulevar Oslobodjenja 45, Novi Sad");
        Location endLocation2 = new Location(45.2550, 19.8450, "Narodnog fronta 12, Novi Sad");
        Location stopLocation2 = new Location(21.54534, 23.5435345, "Bulevar Evrope 22, Novi Sad");
        Location stopLocation3 = new Location(41.423424, 42.42342, "Janka Cmelika 32, Novi Sad");
        // Location startLocation2 = new Location(12L, 45.2671, 19.8335, "Bulevar
        // Oslobodjenja 45, Novi Sad");
        // Location endLocation2 = new Location(15L, 45.2550, 19.8450, "Narodnog fronta
        // 12, Novi Sad");
        // Location stopLocation2 = new Location(82L, 21.54534, 23.5435345, "Bulevar
        // Evrope 22, Novi Sad");
        // Location stopLocation3 = new Location(43L, 41.423424, 42.42342, "Janka
        // Cmelika 32, Novi Sad");
        // //ride2.setRoute(new Route(13L, startLocation2, endLocation2,
        // List.of(stopLocation2, stopLocation3), 5, 15));
        ride2.setPassengerEmails(List.of("radovan.radinic@gmail.com", "galja.miric@gmail.com"));
        ride2.setStartTime("2024-05-11T09:15:00");
        List<RideResponseDTO> rides = List.of(ride1, ride2);
        return ResponseEntity.ok(rides);
    }

    @PostMapping("/{id}/change-request")
    public ResponseEntity<DriverChangeResponseDTO> requestDriverChange(@PathVariable @NotNull @Min(1) Long id,
                                                                       @RequestBody @NotNull DriverChangeRequestDTO request) {
        return ResponseEntity.ok(driverService.requestDriverChanges(id, request));
    }
}