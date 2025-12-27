package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.DriverHistoryItemDTO;

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
}
