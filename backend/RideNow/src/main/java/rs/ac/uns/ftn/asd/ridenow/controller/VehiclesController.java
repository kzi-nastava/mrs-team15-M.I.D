package rs.ac.uns.ftn.asd.ridenow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.asd.ridenow.dto.vehicles.VehicleResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle")
public class VehiclesController {
    @GetMapping("/")
    public ResponseEntity<List<VehicleResponseDTO>> getAll() {
        VehicleResponseDTO vehicle1 = new VehicleResponseDTO();
        vehicle1.setLicencePlate("NS123AB");
        vehicle1.setType(VehicleType.STANDARD);
        vehicle1.setAvailable(true);
        vehicle1.setLat(45.2671);
        vehicle1.setLon(19.8335);
        vehicle1.setRating(4.5);
        vehicle1.setPetFriendly(true);
        vehicle1.setChildFriendly(true);

        VehicleResponseDTO vehicle2 = new VehicleResponseDTO();
        vehicle2.setLicencePlate("NS133AB");
        vehicle2.setType(VehicleType.LUXURY);
        vehicle2.setAvailable(false);
        vehicle2.setLat(45.2675);
        vehicle2.setLon(19.8340);
        vehicle2.setRating(4.8);
        vehicle2.setPetFriendly(false);
        vehicle2.setChildFriendly(true);

        List<VehicleResponseDTO> vehicles = List.of(vehicle1, vehicle2);

        return ResponseEntity.ok(vehicles);
    }

    @PutMapping("/{id}/location")
    public ResponseEntity<VehicleResponseDTO> updateVehicleLocation(@PathVariable String id, double lat, double lon) {
        VehicleResponseDTO vehicle = new VehicleResponseDTO();
        vehicle.setLicencePlate(id);
        vehicle.setLat(lat);
        vehicle.setLon(lon);
        vehicle.setAvailable(true);
        vehicle.setRating(4.6);
        vehicle.setPetFriendly(true);
        vehicle.setChildFriendly(false);
        vehicle.setType(VehicleType.STANDARD);

        return ResponseEntity.ok(vehicle);
    }
}
