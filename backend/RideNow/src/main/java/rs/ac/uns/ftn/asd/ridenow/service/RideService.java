package rs.ac.uns.ftn.asd.ridenow.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.*;
import rs.ac.uns.ftn.asd.ridenow.dto.user.RateRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.RateResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.exception.RoutingException;
import rs.ac.uns.ftn.asd.ridenow.model.*;

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

    @Autowired
    private  PriceService priceService;

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
        try {
            // use provided coordinates from the DTO
            double latStart = dto.getStartLatitude();
            double lonStart = dto.getStartLongitude();
            double latEnd = dto.getEndLatitude();
            double lonEnd = dto.getEndLongitude();

            // choose routing method depending on presence of stop points
            RideEstimateResponseDTO estimate;
            if (dto.getStopLatitudes() != null && dto.getStopLongitudes() != null
                    && !dto.getStopLatitudes().isEmpty() && dto.getStopLongitudes().size() == dto.getStopLatitudes().size()) {
                System.out.println("Stop points provided.");
                estimate = routingService.getRouteWithStops(latStart, lonStart, latEnd, lonEnd,
                        dto.getStopLatitudes(), dto.getStopLongitudes());
            } else {
                System.out.println("No stop points provided, using direct route estimation.");
                estimate = routingService.getRoute(latStart, lonStart, latEnd, lonEnd);
            }

            RouteResponseDTO response = new RouteResponseDTO();
            response.setStartAddress(dto.getStartAddress());
            response.setStartLatitude(latStart);
            response.setStartLongitude(lonStart);
            response.setEndAddress(dto.getEndAddress());
            response.setEndLatitude(latEnd);
            response.setEndLongitude(lonEnd);
            response.setStopAddresses(dto.getStopAddresses());
            response.setStopLatitudes(dto.getStopLatitudes());
            response.setStopLongitudes(dto.getStopLongitudes());

            response.setDistanceKm(estimate.getDistanceKm());
            response.setEstimatedTimeMinutes(estimate.getEstimatedDurationMin());
            response.setRoute(estimate.getRoute()); 

            // calculate a price estimate using default vehicle type (STANDARD)
            double price = priceService.calculatePrice(VehicleType.STANDARD, estimate.getDistanceKm());
            response.setPriceEstimateStandard(price);

            price = priceService.calculatePrice(VehicleType.LUXURY, estimate.getDistanceKm());
            response.setPriceEstimateLuxury(price);

            price = priceService.calculatePrice(VehicleType.VAN, estimate.getDistanceKm());
            response.setPriceEstimateVan(price);

            // routeId is not persisted for estimates
            response.setRouteId(null);

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to estimate route: " + e.getMessage(), e);
        }
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

        Driver assigned = null;

        Ride ride = new Ride();
        ride.setStatus(RideStatus.REQUESTED);
        ride.setScheduledTime(dto.getScheduledTime() != null ? dto.getScheduledTime() : LocalDateTime.now());
        ride.setDistanceKm(dto.getDistanceKm());
        ride.setPrice(dto.getPriceEstimate());
        ride.setRoute(route);
        ride.setDriver(assigned);
        ride = rideRepository.save(ride);

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

        Vehicle vehicle = driver.getVehicle();
        try {
            double[] endCoordinate = routingService.getGeocode(ride.getRoute().getEndLocation().getAddress());

            RideEstimateResponseDTO estimate = routingService.getRoute(vehicle.getLat(), vehicle.getLon(), endCoordinate[0], endCoordinate[1]);

            return new TrackVehicleDTO(new Location(vehicle.getLat(), vehicle.getLon()), estimate.getEstimatedDurationMin());
        } catch (Exception e) {
            throw new RoutingException("Unable to track ride: " + e.getMessage());
        }
    }

    public Boolean finishRide(Long rideId, Long driverId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException("Ride with id " + rideId + " not found"));

        ride.setStatus(RideStatus.FINISHED);
        ride = rideRepository.save(ride);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextHour = now.plusHours(1);

        List<Ride> scheduledRides = rideRepository.findScheduledRidesForDriverInNextHour(
                driverId, now, nextHour);

        if (scheduledRides.isEmpty()) {
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
            return false;
        }

        Ride nextRide = scheduledRides.get(0);
        nextRide.setStatus(RideStatus.IN_PROGRESS);
        rideRepository.save(nextRide);

        return true;
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
            currentRideDTO.setRideId(ride.getId());
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
        if (!ride.getDriver().getId().equals(driver.getId())){
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

    public StopRideResponseDTO stopRide(User user) throws Exception {
        if (!(user instanceof Driver driver)) {
            throw new Exception("You are not a driver.");
        }
        Optional<Ride> optionalRide = rideRepository.findCurrentRideByDriver(driver.getId());
        if (optionalRide.isEmpty()) {
            throw new Exception("You don't have a ride in progress.");
        }

        Ride ride = optionalRide.get();
        Vehicle vehicle = driver.getVehicle();
        StopRideResponseDTO response = completeRide(ride, vehicle);
        updateRideOnCompletion(ride, response);
        rideRepository.save(ride);
        return response;
    }

    private StopRideResponseDTO completeRide(Ride ride, Vehicle vehicle) throws Exception {
        String startAddress = ride.getRoute().getStartLocation().getAddress();
        double[] startCoordinate = routingService.getGeocode(startAddress);
        double latStart = startCoordinate[0];
        double lonStart = startCoordinate[1];

        double latEnd = vehicle.getLat();
        double lonEnd = vehicle.getLon();

        RideEstimateResponseDTO estimation = routingService.getRoute(latStart, lonStart, latEnd, lonEnd);

        double price = priceService.calculatePrice(vehicle.getType(),estimation.getDistanceKm());

        String endAddress = routingService.getReverseGeocode(vehicle.getLat(), vehicle.getLon());

        StopRideResponseDTO responseDTO = new StopRideResponseDTO();
        responseDTO.setDistanceKm(estimation.getDistanceKm());
        responseDTO.setEstimatedDurationMin(estimation.getEstimatedDurationMin());
        responseDTO.setPrice(price);
        responseDTO.setEndAddress(endAddress);
        responseDTO.setRoute(estimation.getRoute());
        return responseDTO;
    }

    private void updateRideOnCompletion(Ride ride, StopRideResponseDTO response) throws Exception {
        ride.setStatus(RideStatus.FINISHED);
        ride.setEndTime(LocalDateTime.now());
        ride.setDistanceKm(response.getDistanceKm());
        ride.setPrice(response.getPrice());

        String startAddress = ride.getRoute().getStartLocation().getAddress();
        String endAddress = response.getEndAddress();
        Optional<Route> optionalRoute = routeRepository.findByStartAndEndAddress(startAddress, endAddress);
        if(optionalRoute.isEmpty()){
            updateRideRoute(startAddress, endAddress, ride);
            return;
        }
        Route route = optionalRoute.get();
        ride.setRoute(route);
    }

    private void updateRideRoute(String startAddress, String endAddress,
                                 Ride ride) throws Exception {

        double[] startCoordinate = routingService.getGeocode(startAddress);
        double latStart = startCoordinate[0];
        double lonStart = startCoordinate[1];

        double[] endCoordinate = routingService.getGeocode(endAddress);
        double latEnd = endCoordinate[0];
        double lonEnd = endCoordinate[1];

        RideEstimateResponseDTO estimation = routingService.getRoute(latStart, lonStart, latEnd, lonEnd);

        Route route = new Route();

        Location startLocation = new Location();
        startLocation.setAddress(startAddress);
        startLocation.setLatitude(latStart);
        startLocation.setLongitude(lonStart);

        Location endLocation = new Location();
        endLocation.setAddress(endAddress);
        endLocation.setLatitude(latEnd);
        endLocation.setLongitude(lonEnd);

        route.setStartLocation(startLocation);
        route.setEndLocation(endLocation);
        route.setDistanceKm(estimation.getDistanceKm());
        route.setEstimatedTimeMin(estimation.getEstimatedDurationMin());

        routeRepository.save(route);

        ride.setRoute(route);
    }
}
