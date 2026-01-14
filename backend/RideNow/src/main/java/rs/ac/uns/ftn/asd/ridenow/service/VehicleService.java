package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.vehicle.VehicleResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.Location;
import rs.ac.uns.ftn.asd.ridenow.model.Vehicle;
import rs.ac.uns.ftn.asd.ridenow.repository.VehicleRepository;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<VehicleResponseDTO> getAllVehicles(Double lat, Double lng) {
        List<Vehicle> vehicles = vehicleRepository.findVehiclesWithinBounds(
                lat - 0.1, lat + 0.1,
                lng - 0.1, lng + 0.1
        );
        return vehicles.stream()
                .map(this::convertToDTO)
                .toList();
    }

    private VehicleResponseDTO convertToDTO(Vehicle vehicle) {
        VehicleResponseDTO dto = new VehicleResponseDTO();
        dto.setLocation(new Location(vehicle.getLat(), vehicle.getLon()));
        dto.setAvailable(vehicle.isAvailable());
        dto.setLicencePlate(vehicle.getLicencePlate());
        return dto;
    }
}
