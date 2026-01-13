package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.*;
import rs.ac.uns.ftn.asd.ridenow.model.Ride;
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.Vehicle;
import rs.ac.uns.ftn.asd.ridenow.model.Location;
import rs.ac.uns.ftn.asd.ridenow.model.Route;

import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;
import rs.ac.uns.ftn.asd.ridenow.repository.DriverRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.RideRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.RouteRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RideService {

    private final RouteRepository routeRepository;
    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;

    public RideService(RouteRepository routeRepository, RideRepository rideRepository, DriverRepository driverRepository) {
        this.routeRepository = routeRepository;
        this.rideRepository = rideRepository;
        this.driverRepository = driverRepository;
    }

    public RouteResponseDTO estimateRoute(EstimateRouteRequestDTO dto) {
        RouteResponseDTO response = new RouteResponseDTO();

        // test route data
        response.setDistanceKm(15.5);
        response.setEstimatedTimeMinutes(30);
        response.setPriceEstimate(2000);
        response.setRouteId(1L);
        return response;
    }

    public OrderRideResponseDTO orderRide(OrderRideRequestDTO dto) {
        OrderRideResponseDTO response = new OrderRideResponseDTO();

        // validate vehicle type
        VehicleType vehicleType;
        try {
            vehicleType = VehicleType.valueOf(dto.getVehicleType().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid vehicle type: " + dto.getVehicleType());
        }

        // create route
        Location start = new Location(dto.getStartLatitude(), dto.getStartLongitude(), dto.getStartAddress());
        Location end = new Location(dto.getEndLatitude(), dto.getEndLongitude(), dto.getEndAddress());
        Route route = new Route(dto.getDistanceKm(), dto.getEstimatedTimeMinutes(), start, end);
        if (dto.getStopAddresses() != null && dto.getStopLatitudes() != null && dto.getStopLongitudes() != null) {
            int stops = Math.min(dto.getStopAddresses().size(), Math.min(dto.getStopLatitudes().size(), dto.getStopLongitudes().size()));
            for (int i = 0; i < stops; i++) {
                route.addStopLocation(new Location(dto.getStopLatitudes().get(i), dto.getStopLongitudes().get(i), dto.getStopAddresses().get(i)));
            }
        }
        route = routeRepository.save(route);

        // try to auto-assign an available driver
        Optional<Driver> optDriver = driverRepository.autoAssign(vehicleType, 1, dto.isBabyFriendly(), dto.isPetFriendly());
        Driver assigned = null;
        if (optDriver.isPresent()) {
            assigned = optDriver.get();
        } else {
            // create a mock driver + vehicle and persist it
            Driver mock = new Driver();
            mock.setEmail("mock.driver@example.com");
            mock.setPassword("password");
            mock.setFirstName("Mock");
            mock.setLastName("Driver");
            mock.setPhoneNumber("000000000");
            mock.setAddress("Mock address");
            mock.setProfileImage(null);
            mock.setActive(true);
            mock.setBlocked(false);
            mock.setStatus(DriverStatus.ACTIVE);
            mock.setAvailable(true);
            mock.setRating(5.0);
            mock.setWorkingHoursLast24(0.0);

            Vehicle vehicle = new Vehicle();
            vehicle.setLicencePlate("MOCK-" + System.currentTimeMillis());
            vehicle.setModel("MockModel");
            vehicle.setPetFriendly(dto.isPetFriendly());
            vehicle.setChildFriendly(dto.isBabyFriendly());
            vehicle.setSeatCount(4);
            vehicle.setType(vehicleType);

            // establish the bi-directional relationship
            mock.assignVehicle(vehicle);

            assigned = driverRepository.save(mock);
        }

        // create ride and assign the driver
        Ride ride = new Ride();
        ride.setStatus(RideStatus.REQUESTED);
        ride.setScheduledTime(dto.getScheduledTime() != null ? dto.getScheduledTime() : LocalDateTime.now());
        ride.setDistanceKm(dto.getDistanceKm());
        ride.setPrice(dto.getPriceEstimate());
        ride.setRoute(route);
        ride.setDriver(assigned);
        ride = rideRepository.save(ride);

        // add ride to driver's history and mark unavailable
        if (assigned != null) {
            assigned.addRide(ride);
            assigned.setAvailable(false);
            driverRepository.save(assigned);
        }

        response.setId(ride.getId());
        response.setMainPassengerEmail(dto.getMainPassengerEmail());
        response.setStartAddress(dto.getStartAddress());
        response.setEndAddress(dto.getEndAddress());
        response.setStopAddresses(dto.getStopAddresses());
        response.setVehicleType(vehicleType);
        response.setBabyFriendly(dto.isBabyFriendly());
        response.setPetFriendly(dto.isPetFriendly());
        response.setLinkedPassengers(dto.getLinkedPassengers());
        response.setScheduledTime(ride.getScheduledTime());
        response.setStatus(ride.getStatus());
        response.setDistanceKm(ride.getDistanceKm());
        response.setEstimatedTimeMinutes(dto.getEstimatedTimeMinutes());
        response.setPriceEstimate(ride.getPrice());

        return response;
    }

    public void startRide(Long rideId) {
        // mock – no logic
    }
}
