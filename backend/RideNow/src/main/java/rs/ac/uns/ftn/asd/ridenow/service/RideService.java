package rs.ac.uns.ftn.asd.ridenow.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.*;
import rs.ac.uns.ftn.asd.ridenow.dto.user.RateRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.RateResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.*;

import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.PassengerRole;
import rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;
import rs.ac.uns.ftn.asd.ridenow.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class RideService {

    @Autowired
    private  RoutingService routingService;

    @Autowired
    private  PanicAlertRepository panicAlertRepository;

    private final RouteRepository routeRepository;
    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;
    private final RatingRepository ratingRepository;
    private final InconsistencyRepository inconsistencyRepository;
    private final PassengerRepository passengerRepository;
    private final RegisteredUserRepository registeredUserRepository;

    public RideService(RouteRepository routeRepository, RideRepository rideRepository, DriverRepository driverRepository, RatingRepository ratingRepository, InconsistencyRepository inconsistencyRepository, PassengerRepository passengerRepository, RegisteredUserRepository registeredUserRepository) {
        this.routeRepository = routeRepository;
        this.rideRepository = rideRepository;
        this.driverRepository = driverRepository;
        this.ratingRepository = ratingRepository;
        this.inconsistencyRepository = inconsistencyRepository;
        this.passengerRepository = passengerRepository;
        this.registeredUserRepository = registeredUserRepository;
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
            mock.setEmail("mock1.driver@example.com");
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

    public RateResponseDTO makeRating(RateRequestDTO req, Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException("Ride with id " + rideId + " not found"));

        // Create and populate the rating entity
        Rating rating = new Rating();
        rating.setRide(ride);
        rating.setVehicleRating(req.getVehicleRating());
        rating.setDriverRating(req.getDriverRating());
        rating.setDriverComment(req.getDriverComment());
        rating.setVehicleComment(req.getVehicleComment());
        rating.setCreatedAt(LocalDateTime.now());

        // Save to database
        Rating savedRating = ratingRepository.save(rating);
        return(new RateResponseDTO(savedRating));
    }

    public TrackVehicleDTO trackRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException("Ride with id " + rideId + " not found"));

        Driver driver = ride.getDriver();
        if (driver == null) {
            throw new EntityNotFoundException("No driver assigned to ride with id " + rideId);
        }

        // TODO: implement real time estimation
        Vehicle vehicle = driver.getVehicle();
        return new TrackVehicleDTO(new Location(vehicle.getLat(), vehicle.getLon()), 10);
    }

    public RideResponseDTO finishRide(Long rideId, Long driverId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException("Ride with id " + rideId + " not found"));

        ride.setStatus(RideStatus.FINISHED);
        ride = rideRepository.save(ride);

        // mark driver as available again
        Driver driver = ride.getDriver();
        if (driver != null) {
            driver.setAvailable(true);
            if(driver.getPendingStatus() != null){
                driver.setStatus(driver.getPendingStatus());
                driver.setPendingStatus(null);
            }
            driverRepository.save(driver);
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextHour = now.plusHours(1);

        List<Ride> scheduledRides = rideRepository.findScheduledRidesForDriverInNextHour(
                driverId, now, nextHour);

        if (scheduledRides.isEmpty()) {
            return null;
        }

        Ride nextRide = scheduledRides.get(0);
        RideResponseDTO response = new RideResponseDTO();
        response.setRideId(nextRide.getId());
        response.setStartTime(nextRide.getScheduledTime());
        response.setPassengerEmails(nextRide.getPassengers().stream()
                .map(p -> p.getUser().getEmail())
                .toList());

        return response;
    }

    public InconsistencyResponseDTO reportInconsistency(InconsistencyRequestDTO req, Long userId) {
        RegisteredUser regUser = registeredUserRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));
        Passenger passenger = (Passenger) passengerRepository.findByUser(regUser).orElseThrow(() -> new EntityNotFoundException("Passenger with not found"));
        Ride ride = rideRepository.findById(req.getRideId()).orElseThrow(() -> new EntityNotFoundException("Ride with id " + req.getRideId() + " not found"));

        Inconsistency inconsistency = new Inconsistency(ride, passenger, req.getDescription());
        Inconsistency savedInconsistency = inconsistencyRepository.save(inconsistency);
        return new InconsistencyResponseDTO(savedInconsistency);
    }

    public void startRide(Long rideId) {
        // mock – no logic
    }

    public List<UpcomingRideDTO> getUpcomingRidesByUser(Long user_id) {
        List<Object[]> results = rideRepository.findUpcomingRidesByUser(user_id);
        List<UpcomingRideDTO> upcomingRides = new ArrayList<>();
        for (Object[] row : results) {
            UpcomingRideDTO upcomingRide = new UpcomingRideDTO();
            upcomingRide.setId((Long) row[0]);
            upcomingRide.setRoute((String) row[2]);
            upcomingRide.setStartTime((String) row[3]);
            upcomingRide.setPassengers((String) row[4]);
            upcomingRide.setCanCancel(row[1] == user_id);
            upcomingRides.add(upcomingRide);
        }
        return upcomingRides;
    }

    public CurrentRideDTO getCurrentRide(User user) throws Exception {
        try{
            Optional<Ride> optionalRide = Optional.empty();
            if(user instanceof RegisteredUser registeredUser){
                optionalRide = rideRepository.findCurrentRideByUser(registeredUser.getId());
            }
            else if(user instanceof  Driver driver){
                optionalRide = rideRepository.findCurrentRideByDriver(driver.getId());
            }
            if(optionalRide.isEmpty()){
                throw new Exception("You don't have ride in progress");
            }
            Ride ride = optionalRide.get();
            String startAddress = ride.getRoute().getStartLocation().getAddress();
            String endAddress = ride.getRoute().getEndLocation().getAddress();

            double[] startCoordinate = routingService.getGeocode(startAddress);
            double latStart = startCoordinate[0];
            double lonStart = startCoordinate[1];

            double[] endCoordinate = routingService.getGeocode(endAddress);
            double latEnd = endCoordinate[0];
            double lonEnd = endCoordinate[1];

            RideEstimateResponseDTO response = routingService.getRoute(latStart, lonStart, latEnd, lonEnd);

            CurrentRideDTO currentRideDTO = new CurrentRideDTO();
            currentRideDTO.setStartAddress(startAddress);
            currentRideDTO.setEndAddress(endAddress);
            currentRideDTO.setEstimatedDurationMin(response.getEstimatedDurationMin());
            currentRideDTO.setRoute(response.getRoute());
            currentRideDTO.setDistanceKm(response.getDistanceKm());
            return currentRideDTO;
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public void triggerPanicAlert(User user) throws Exception {
        Optional<Ride> optionalRide = Optional.empty();
        if(user instanceof RegisteredUser registeredUser){
            optionalRide = rideRepository.findCurrentRideByUser(registeredUser.getId());
        }
        else if(user instanceof  Driver driver){
            optionalRide = rideRepository.findCurrentRideByDriver(driver.getId());
        }
        if(optionalRide.isEmpty()){
            throw new Exception("You don't have ride in progress");
        }
        Ride ride = optionalRide.get();
        if(ride.getPanicAlert() != null){
            throw new Exception("Panic mode already active. Help is on the way!");
        }
        PanicAlert panicAlert = new PanicAlert();
        panicAlert.setRide(ride);
        panicAlert.setResolved(false);
        panicAlert.setPanicBy(user.getRole().name());
        panicAlertRepository.save(panicAlert);
        ride.setPanicAlert(panicAlert);
        rideRepository.save(ride);
    }

    public void userRideCancellation(RegisteredUser registeredUser, Long rideId, CancelRideRequestDTO request) throws Exception {
        Optional<Ride> optionalRide = rideRepository.findById(rideId);
        if(optionalRide.isEmpty()){
            throw new Exception("Ride does not exists");
        }
        Ride ride = optionalRide.get();

        Optional<Passenger> optionalPassenger = passengerRepository.findByUserAndRide(registeredUser, ride);
        if (optionalPassenger.isEmpty()) {
            throw new Exception("You are not a passenger on this ride");
        }

        Passenger passenger = optionalPassenger.get();
        if(passenger.getRole() != PassengerRole.CREATOR){
            throw new Exception("Only the creator can cancel the ride");
        }

        if (LocalDateTime.now().plusMinutes(10).isBefore(ride.getScheduledTime())) {
            ride.setCancelled(true);
            ride.setCancelledBy("USER");
            ride.setCancelReason(request.getReason());
            ride.setStatus(RideStatus.CANCELLED);
            rideRepository.save(ride);
        }
        else{
            throw  new Exception("You can cancel a ride up to 10 minutes before it starts.");
        }
    }

    public void driverRideCancellation(Driver driver, Long rideId, CancelRideRequestDTO request) throws Exception {
        Optional<Ride> optionalRide = rideRepository.findById(rideId);
        if(optionalRide.isEmpty()){
            throw new Exception("Ride does not exists");
        }
        Ride ride = optionalRide.get();
        if (ride.getDriver().getId().equals(driver.getId())){
            throw new Exception("You are not a driver on this ride");
        }
        String reason = request.getReason().trim();
        if (reason.isEmpty()) {
            throw new Exception("You must provide a reason for cancellation.");
        }
        ride.setCancelled(true);
        ride.setCancelledBy("DRIVER");
        ride.setCancelReason(reason);
        ride.setStatus(RideStatus.CANCELLED);
        rideRepository.save(ride);
    }
}
