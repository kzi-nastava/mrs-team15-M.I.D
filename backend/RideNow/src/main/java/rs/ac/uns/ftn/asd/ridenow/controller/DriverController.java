package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/driver")
public class DriverController {
    @GetMapping("/{id}/ride-history")
    public ResponseEntity<List<String>> getRideHistory(@PathVariable int id) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }

    }
}
