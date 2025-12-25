package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.RideDetailsDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.RideHistoryItemDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.RegisterDriverRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.RegisterDriverResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.admin.DriverChangeRequestDTO;
import jakarta.validation.Valid;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverChangesStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
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
        return ResponseEntity.ok(details);
    }

    @PostMapping("/driver-register")
    public ResponseEntity<RegisterDriverResponseDTO> register(
            @Valid @RequestBody RegisterDriverRequestDTO request){

        RegisterDriverResponseDTO response = new RegisterDriverResponseDTO();
        response.setId(1L);
        response.setActive(false);
        response.setEmail(request.getEmail());
        response.setFirstName(request.getFirstName());
        response.setLastName(request.getLastName());
        response.setAddress(request.getAddress());
        response.setPhoneNumber(request.getPhoneNumber());
        response.setLicensePlate(request.getLicensePlate());
        response.setVehicleModel(request.getVehicleModel());
        response.setVehicleType(request.getVehicleType());
        response.setNumberOfSeats(request.getNumberOfSeats());
        response.setBabyFriendly(request.isBabyFriendly());
        response.setPetFriendly(request.isPetFriendly());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/driver-change-requests")
    public ResponseEntity<List<DriverChangeRequestDTO>> getPendingRequests() {
        List<DriverChangeRequestDTO> requests = new ArrayList<>();

        DriverChangeRequestDTO req = new DriverChangeRequestDTO();

        req.setEmail("driver@mail.com");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setPhoneNumber("123-456-7890");
        req.setProfileImage("profile_image_url");
        req.setAddress("123 Main St, Cityville");
        req.setLicensePlate("NS123AB");
        req.setVehicleModel("Toyota Prius");
        req.setVehicleType(VehicleType.valueOf("Standard"));
        req.setNumberOfSeats(4);
        req.setBabyFriendly(true);
        req.setPetFriendly(false);

        req.setStatus(DriverChangesStatus.valueOf("PENDING"));

        requests.add(req);

        return ResponseEntity.ok(requests);
    }

    @PutMapping("/driver-change-requests/{id}/approve")
    public ResponseEntity<Void> approveRequest(@PathVariable Long id) {
        // to do : approving logic
        return ResponseEntity.ok().build();
    }

    @PutMapping("/driver-change-requests/{id}/reject")
    public ResponseEntity<Void> rejectRequest(@PathVariable Long id) {
        // to do : rejecting logic
        return ResponseEntity.ok().build();
    }
}
