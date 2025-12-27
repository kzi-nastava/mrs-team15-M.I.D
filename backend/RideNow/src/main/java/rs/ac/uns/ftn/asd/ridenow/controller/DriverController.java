package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.DriverHistoryItemDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RideResponseDTO;

import java.util.List;

@RestController
@RequestMapping("/api/driver")
public class DriverController {
    @GetMapping("/{id}/ride-history")
    public ResponseEntity<List<DriverHistoryItemDTO>> getRideHistory(@PathVariable int id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

        DriverHistoryItemDTO ride1 = new DriverHistoryItemDTO();
        ride1.setStartLocation("Bulevar Oslobodjenja 10, Novi Sad");
        ride1.setEndLocation("Bulevar Oslobodjenja 24, Novi Sad");
        ride1.setDate(java.sql.Date.valueOf("2024-01-15"));
        ride1.setCost(1500.0);
        ride1.setCancelled(false);
        ride1.setInconsistencies(List.of("Late arrival"));
        ride1.setRating(3.5);
        ride1.setPanic(false);
        ride1.setDurationMinutes(20);
        ride1.setPassengers(List.of("Marko Markovic", "Jovana Jovanovic"));

        DriverHistoryItemDTO ride2 = new DriverHistoryItemDTO();
        ride2.setStartLocation("Zmaj Jovina 5, Novi Sad");
        ride2.setEndLocation("Trg Slobode 1, Novi Sad");
        ride2.setDate(java.sql.Date.valueOf("2024-02-20"));
        ride2.setCost(800.0);
        ride2.setCancelled(true);
        ride2.setInconsistencies(List.of("No show"));
        ride2.setRating(null);
        ride2.setPanic(false);
        ride2.setDurationMinutes(0);
        ride2.setPassengers(List.of("Ana Anic"));

        List<DriverHistoryItemDTO> history = List.of(ride1, ride2);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<RideResponseDTO> finish(@PathVariable Long id){
        if (id <= 0){
            return ResponseEntity.status(400).build();
        }

        RideResponseDTO response = new RideResponseDTO();
        response.setRideId(1L);
        response.setStartLocation("Bulevar Oslobodjenja 45, Novi Sad");
        response.setEndLocation("Narodnog fronta 12, Novi Sad");
        response.setStartLatitude(21.23);
        response.setStartLongitude(45.43);
        response.setEndLatitude(22.23);
        response.setEndLongitude(46.43);
        response.setPassengerEmails(List.of("marko.maric@gmail.com", "ana.danic@gmail.com"));
        response.setStartTime("2024-05-10T14:30:00");

        return  ResponseEntity.ok(response);
    }

    @GetMapping("/{driverId}")
    public ResponseEntity<List<RideResponseDTO>> findRides(@PathVariable Long driverId){
        if (driverId <= 0){
            return ResponseEntity.status(400).build();
        }

        RideResponseDTO ride1 = new RideResponseDTO();
        ride1.setRideId(1L);
        ride1.setStartLocation("Bulevar Oslobodjenja 45, Novi Sad");
        ride1.setEndLocation("Narodnog fronta 12, Novi Sad");
        ride1.setStartLatitude(21.23);
        ride1.setStartLongitude(45.43);
        ride1.setEndLatitude(22.23);
        ride1.setEndLongitude(46.43);
        ride1.setPassengerEmails(List.of("danka.danic@gmail.com", "mario.ploros@gmail.com"));
        ride1.setStartTime("2024-05-10T14:30:00");

        RideResponseDTO ride2 = new RideResponseDTO();
        ride2.setRideId(2L);
        ride2.setStartLocation("Trg Slobode 3, Novi Sad");
        ride2.setEndLocation("Bulevar Evrope 28, Novi Sad");
        ride2.setStartLatitude(23.23);
        ride2.setStartLongitude(47.43);
        ride2.setEndLatitude(24.23);
        ride2.setEndLongitude(48.43);
        ride2.setPassengerEmails(List.of("radovan.radinic@gmail.com", "galja.miric@g"));
        ride2.setStartTime("2024-05-11T09:15:00");
        List<RideResponseDTO> rides = List.of(ride1, ride2);
        return ResponseEntity.ok(rides);
    }

}
