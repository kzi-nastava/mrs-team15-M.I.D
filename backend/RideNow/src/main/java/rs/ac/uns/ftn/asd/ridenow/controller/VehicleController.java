package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.vehicle.UpdateVehicleRequest;
import rs.ac.uns.ftn.asd.ridenow.dto.vehicle.VehicleResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.Location;
import rs.ac.uns.ftn.asd.ridenow.service.VehicleService;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    @Autowired
    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/")
    public ResponseEntity<List<VehicleResponseDTO>> getAll(@RequestParam @NotNull Double lat, @RequestParam @NotNull Double lon) {
        List<VehicleResponseDTO> vehicles = vehicleService.getAllVehicles(lat, lon);
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
