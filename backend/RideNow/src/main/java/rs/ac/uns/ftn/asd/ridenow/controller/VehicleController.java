package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.vehicle.UpdateVehicleRequest;
import rs.ac.uns.ftn.asd.ridenow.dto.vehicle.VehicleResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.Location;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    @GetMapping("/")
    public ResponseEntity<List<VehicleResponseDTO>> getAll() {
        VehicleResponseDTO vehicle1 = new VehicleResponseDTO();
        vehicle1.setLicencePlate("NS123AB");
        vehicle1.setAvailable(true);
        vehicle1.setLocation(new Location(23.42342, 42.312312, ""));
        //vehicle1.setLocation(new Location(42L, 23.42342, 42.312312, ""));
        VehicleResponseDTO vehicle2 = new VehicleResponseDTO();
        vehicle2.setLicencePlate("NS133AB");
        vehicle2.setAvailable(false);
        vehicle2.setLocation(new Location(23.42342, 42.312312, ""));
        //vehicle2.setLocation(new Location(42L, 23.42342, 42.312312, ""));

        List<VehicleResponseDTO> vehicles = List.of(vehicle1, vehicle2);

        return ResponseEntity.ok(vehicles);
    }

    @PostMapping("/update-location/{licencePlate}")
    public ResponseEntity<VehicleResponseDTO> updateVehicleLocation(@PathVariable @NotNull @NotEmpty String licencePlate, @Valid @RequestBody UpdateVehicleRequest req) {
        VehicleResponseDTO vehicle = new VehicleResponseDTO();
        vehicle.setLicencePlate(licencePlate);
        vehicle.setLocation(new Location(req.getLat(), req.getLon(), ""));
        //vehicle.setLocation(new Location(14L, req.getLat(), req.getLon(), ""));
        vehicle.setAvailable(true);
        return ResponseEntity.ok(vehicle);
    }
}
