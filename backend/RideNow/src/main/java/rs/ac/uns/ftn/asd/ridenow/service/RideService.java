package rs.ac.uns.ftn.asd.ridenow.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RouteDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.*;
import rs.ac.uns.ftn.asd.ridenow.dto.user.RateRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.user.RateResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.exception.RoutingException;
import rs.ac.uns.ftn.asd.ridenow.model.*;

import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.PassengerRole;
import rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;
import rs.ac.uns.ftn.asd.ridenow.repository.*;
import rs.ac.uns.ftn.asd.ridenow.websocket.NotificationWebSocketHandler;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RideService {

    private final RoutingService routingService;
    private final PanicAlertRepository panicAlertRepository;
    private final PriceService priceService;
    private final RouteRepository routeRepository;
    private final RideRepository rideRepository;
    private final DriverRepository driverRepository;
    private final RatingRepository ratingRepository;
    private final InconsistencyRepository inconsistencyRepository;
    private final PassengerRepository passengerRepository;
    private final RegisteredUserRepository registeredUserRepository;
    private final NotificationService notificationService;
    private final NotificationWebSocketHandler webSocketHandler;

    public RideService(RoutingService routingService, PanicAlertRepository panicAlertRepository,
                       PriceService priceService, RouteRepository routeRepository,
                       RideRepository rideRepository, DriverRepository driverRepository,
                       RatingRepository ratingRepository, InconsistencyRepository inconsistencyRepository,
                       PassengerRepository passengerRepository, RegisteredUserRepository registeredUserRepository,
                       NotificationService notificationService, NotificationWebSocketHandler webSocketHandler) {

        this.routingService = routingService;
        this.panicAlertRepository = panicAlertRepository;
        this.priceService = priceService;
        this.routeRepository = routeRepository;
        this.rideRepository = rideRepository;
        this.driverRepository = driverRepository;
        this.ratingRepository = ratingRepository;
        this.inconsistencyRepository = inconsistencyRepository;
        this.passengerRepository = passengerRepository;
        this.registeredUserRepository = registeredUserRepository;
        this.notificationService = notificationService;
        this.webSocketHandler = webSocketHandler;
    }

    public RouteResponseDTO estimateRoute(EstimateRouteRequestDTO dto) {
        try {

            double latStart = dto.getStartLatitude();
            double lonStart = dto.getStartLongitude();
            double latEnd = dto.getEndLatitude();
            double lonEnd = dto.getEndLongitude();

            // routing method depending on stop points
            RideEstimateResponseDTO estimate;
            if (dto.getStopLatitudes() != null && dto.getStopLongitudes() != null
                    && !dto.getStopLatitudes().isEmpty() && dto.getStopLongitudes().size() == dto.getStopLatitudes().size()) {

                estimate = routingService.getRouteWithStops(latStart, lonStart, latEnd, lonEnd,
                        dto.getStopLatitudes(), dto.getStopLongitudes());
            } else {

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

            // calculate a price estimate for all vehicle types
            double price = priceService.calculatePrice(VehicleType.STANDARD, estimate.getDistanceKm());
            response.setPriceEstimateStandard(price);

            price = priceService.calculatePrice(VehicleType.LUXURY, estimate.getDistanceKm());
            response.setPriceEstimateLuxury(price);

            price = priceService.calculatePrice(VehicleType.VAN, estimate.getDistanceKm());
            response.setPriceEstimateVan(price);

            response.setRouteId(null);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to estimate route: " + e.getMessage(), e);
        }
    }

    public OrderRideResponseDTO orderRide(OrderRideRequestDTO dto, String mainPassenger) {
        OrderRideResponseDTO response = new OrderRideResponseDTO();

        // validate vehicle type
        VehicleType vehicleType;
        try {
            vehicleType = VehicleType.valueOf(dto.getVehicleType().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid vehicle type: " + dto.getVehicleType());
        }

        // make new route or find in favorites
        Route route = makeRoute(dto);

        Ride ride = new Ride();

        if (dto.getScheduledTime() != null) {
            LocalDateTime scheduled = dto.getScheduledTime();
            LocalDateTime prev30 = scheduled.minusMinutes(30);
            LocalDateTime next30 = scheduled.plusMinutes(30);

            response = getBestDriver(dto, vehicleType, prev30, next30);

        } else {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextHour = now.plusHours(1);
            response = getBestDriver(dto, vehicleType, now, nextHour);

        }

        Driver assigned = driverRepository.findById(response.getDriverId())
                .orElseThrow(() -> new EntityNotFoundException("Driver not found"));
        int ETA = response.getETA();
        // Assign selected driver
        ride.setDriver(assigned);
        ride.setStatus(RideStatus.REQUESTED);
        if (dto.getScheduledTime() == null) {
            ride.setScheduledTime(LocalDateTime.now().plusMinutes(ETA));
        }
        else {
            ride.setScheduledTime(dto.getScheduledTime());
            ETA = (int) java.time.Duration.between(LocalDateTime.now(), dto.getScheduledTime()).toMinutes();
        }
        ride.setDistanceKm(dto.getDistanceKm());
        ride.setPrice(dto.getPriceEstimate());
        // Add passengers to ride
        Passenger main = new Passenger();
        RegisteredUser mainUser = (RegisteredUser) registeredUserRepository.findByEmail(mainPassenger)
                .orElseThrow(() -> new EntityNotFoundException("User with email " + mainPassenger + " not found"));

        main.setUser(mainUser);
        main.setRole(PassengerRole.CREATOR);
        main.setRide(ride);
        ride.addPassenger(main);
        if (dto.getLinkedPassengers() != null) {
            for (String email : dto.getLinkedPassengers()) {
                try {
                    RegisteredUser user = registeredUserRepository.findByEmail(email)
                            .orElseThrow(() -> new EntityNotFoundException("User with email " + email + " not found"));

                    Passenger passenger = new Passenger();
                    passenger.setUser(user);
                    passenger.setRole(PassengerRole.PASSENGER);
                    passenger.setRide(ride);
                    ride.addPassenger(passenger);

                    // Send notification to the added passenger
                    notificationService.createAndSendPassengerAddedNotification(user, ride);
                } catch (Exception e) {
                    // skip invalid passengers
                }
            }
        }

        if (dto.getFavoriteRouteId() == null) {
            route = routeRepository.save(route);
            ride.setRoute(route);
            ride = rideRepository.save(ride);
            response.setDriverId(assigned.getId());
        } else {
            ride.setRoute(route);
            ride = rideRepository.save(ride);
            response.setDriverId(assigned.getId());
        }

        // mark driver as unavailable
        if (dto.getScheduledTime() == null) {
            assigned.setStatus(DriverStatus.INACTIVE);
        }

        driverRepository.save(assigned);

        response.setId(ride.getId());
        response.setMainPassengerEmail(mainPassenger);
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
        response.setETA(ETA);

        return response;
    }

    public OrderRideResponseDTO getBestDriver(OrderRideRequestDTO dto, VehicleType vehicleType, LocalDateTime now, LocalDateTime nextHour){

        OrderRideResponseDTO response = new OrderRideResponseDTO();

        final int seats = 1 + (dto.getLinkedPassengers() != null ? dto.getLinkedPassengers().size() : 0);
        final boolean babyFriendly = dto.isBabyFriendly();
        final boolean petFriendly = dto.isPetFriendly();

        List<Driver> potentialDrivers = driverRepository.findAll();

        // filter by vehicle type and seat count and baby/pet and working hours
        List<Driver> matchingDrivers = potentialDrivers.stream()
                .filter(d -> d.getVehicle() != null && d.getVehicle().getType() == vehicleType)
                .filter(d -> d.getVehicle().getSeatCount() >= seats)
                .filter(d -> !babyFriendly || (d.getVehicle() != null && d.getVehicle().isChildFriendly()))
                .filter(d -> !petFriendly || (d.getVehicle() != null && d.getVehicle().isPetFriendly()))
                .filter(d -> d.getWorkingHoursLast24() != null && d.getWorkingHoursLast24() <= 8.0)
                .toList();

        if (matchingDrivers.isEmpty()) {
            // no drivers match criteria
            response.setStatus(null);
            response.setId(null);
            response.setDriverId(null);
            return response;
        }

        // compute distances from driver current location to ride start
        double startLat = dto.getStartLatitude();
        double startLon = dto.getStartLongitude();
        List<Driver> availableDrivers;
        if (now == LocalDateTime.now()) {
            availableDrivers = matchingDrivers.stream()
                    .filter(Driver::getAvailable)
                    .filter(d -> d.getStatus() != null && d.getStatus() == DriverStatus.ACTIVE)
                    .filter(d -> rideRepository.findScheduledRidesForDriverInNextHour(d.getId(), now, nextHour).isEmpty())
                    .toList();

        }else {
            availableDrivers = matchingDrivers.stream()
                    .filter(Driver::getAvailable)
                    .filter(d -> rideRepository.findScheduledRidesForDriverInNextHour(d.getId(), now, nextHour).isEmpty())
                    .toList();
        }

        Driver assigned = null;
        int ETA = -1;
        try {
            if (!availableDrivers.isEmpty()) {
                // pick nearest available driver by distance (use routing service estimate)
                double bestDist = Double.MAX_VALUE;
                for (Driver d : availableDrivers) {
                    if (d.getVehicle() == null) continue;
                    double dLat = d.getVehicle().getLat();
                    double dLon = d.getVehicle().getLon();
                    try {
                        RideEstimateResponseDTO est = routingService.getRoute(dLat, dLon, startLat, startLon);
                        if (est.getDistanceKm() < bestDist) {
                            bestDist = est.getDistanceKm();
                            assigned = d;
                            ETA = (int) est.getEstimatedDurationMin();
                        }
                    } catch (Exception ex) {
                        // skip driver on errors
                    }
                }
            } else {
                // No free drivers. Consider drivers currently in progress who will be free soon
                double bestScore = Double.MAX_VALUE;
                for (Driver d : matchingDrivers) {
                    // skip drivers that have scheduled rides soon
                    if (!rideRepository.findScheduledRidesForDriverInNextHour(d.getId(), now, nextHour).isEmpty()) continue;

                    Optional<Ride> inProgress = rideRepository.findCurrentRideByDriver(d.getId());
                    if (inProgress.isEmpty()) continue;
                    Ride current = inProgress.get();
                    if (d.getVehicle() == null) continue;
                    try {
                        double vehLat = d.getVehicle().getLat();
                        double vehLon = d.getVehicle().getLon();
                        double[] endCoord = routingService.getGeocode(current.getRoute().getEndLocation().getAddress());
                        RideEstimateResponseDTO estToEnd = routingService.getRoute(vehLat, vehLon, endCoord[0], endCoord[1]);
                        // if time left > 10 min skip
                        if (estToEnd.getEstimatedDurationMin() > 10) continue;
                        // now compute distance from end of current ride to new ride start
                        RideEstimateResponseDTO estEndToStart = routingService.getRoute(endCoord[0], endCoord[1], startLat, startLon);
                        // scoring: prioritize smaller estEndToStart distance
                        double score = estEndToStart.getDistanceKm();
                        if (score < bestScore) {
                            bestScore = score;
                            assigned = d;
                            ETA = (int) estEndToStart.getEstimatedDurationMin();
                        }
                    } catch (Exception ex) {
                        // skip driver on errors
                    }
                }
            }
        } catch (Exception e) {
            // ignore and continue
        }
        if (assigned != null) {
            response.setETA(ETA);
            response.setDriverId(assigned.getId());
        } else {
            response.setDriverId(null);
        }
        return response;
    }

    private Route makeRoute(OrderRideRequestDTO dto) {
        Route route;
        if (dto.getFavoriteRouteId() == null) {
            // create new route
            Location start = new Location(dto.getStartLatitude(), dto.getStartLongitude(), dto.getStartAddress());
            Location end = new Location(dto.getEndLatitude(), dto.getEndLongitude(), dto.getEndAddress());
            route = new Route(dto.getDistanceKm(), dto.getEstimatedTimeMinutes(), start, end);

            // validate and add stop locations (ensure latitudes and longitudes lists match)
            if (dto.getStopLatitudes() != null || dto.getStopLongitudes() != null || dto.getStopAddresses() != null) {
                List<Double> stopLats = dto.getStopLatitudes();
                List<Double> stopLons = dto.getStopLongitudes();
                List<String> stopAddrs = dto.getStopAddresses();

                if (stopLats == null || stopLons == null) {
                    throw new IllegalArgumentException("Stop latitudes and longitudes must both be provided or both be null");
                }
                if (stopLats.size() != stopLons.size()) {
                    throw new IllegalArgumentException("Stop latitudes and longitudes lists must have the same size");
                }
                int stops = stopLats.size();
                if (stopAddrs != null && stopAddrs.size() != stops) {
                    throw new IllegalArgumentException("Stop addresses list size must match stop coordinates size");
                }

                for (int i = 0; i < stops; i++) {
                    String addr = (stopAddrs != null) ? stopAddrs.get(i) : null;
                    route.addStopLocation(new Location(stopLats.get(i), stopLons.get(i), addr));
                }
            }

            // validate and set drawable route polyline points (routeLattitudes / routeLongitudes)
            List<Double> routeLats = dto.getRouteLattitudes();
            List<Double> routeLons = dto.getRouteLongitudes();
            if (routeLats == null || routeLons == null) {
                throw new IllegalArgumentException("Route latitudes and longitudes must both be provided");
            }
            if (routeLats.size() != routeLons.size()) {
                throw new IllegalArgumentException("Route latitudes and longitudes lists must have the same size");
            }
            for (int i = 0; i < routeLats.size(); i++) {
                route.getPolylinePoints().add(new PolylinePoint(routeLats.get(i), routeLons.get(i)));
            }
        }else{
            route = routeRepository.findById(dto.getFavoriteRouteId())
                    .orElseThrow(() -> new EntityNotFoundException("Favorite route with id " + dto.getFavoriteRouteId() + " not found"));

        }
        return route;
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

        // Delete RIDE_FINISHED notifications for all passengers of this ride
        // since the ride has been rated and they no longer need the notification
        notificationService.deleteRideFinishedNotifications(rideId);

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
            Route route = ride.getRoute();
            List<Location> stops = route.getStopLocations();
            List<Double> stopLats = new ArrayList<>();
            List<Double> stopLons = new ArrayList<>();
            for (Location stop : stops) {
                stopLats.add(stop.getLatitude());
                stopLons.add(stop.getLongitude());
            }

            RideEstimateResponseDTO estimate = routingService.getRouteWithStops(vehicle.getLat(), vehicle.getLon(), route.getEndLocation().getLatitude(), route.getEndLocation().getLongitude(), stopLats, stopLons);

            return new TrackVehicleDTO(new Location(vehicle.getLat(), vehicle.getLon()), estimate.getEstimatedDurationMin());
        } catch (Exception e) {
            throw new RoutingException("Unable to track ride: " + e.getMessage());
        }
    }

    public Boolean finishRide(Long rideId, Long driverId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException("Ride with id " + rideId + " not found"));

        ride.setStatus(RideStatus.FINISHED);
        ride.setEndTime(LocalDateTime.now());
        ride = rideRepository.save(ride);

        // Broadcast finished ride to all ride participants
        Map<String, Object> completionData = new HashMap<>();
        completionData.put("rideId", rideId);
        completionData.put("triggeredBy", "DRIVER");
        completionData.put("timestamp", new Date());

        webSocketHandler.broadcastRideComplete(rideId, completionData);
        // Websocket cleanup
        webSocketHandler.unregisterRide(rideId);
        System.out.println("Ride " + rideId + " completed and unregistered");

    // Delete old ride-related notifications (passenger added, ride assigned, ride started)
        notificationService.deleteRideRelatedNotifications(rideId);

        // Also delete any notifications for the specific passengers (fallback for notifications without relatedEntityId)
        List<RegisteredUser> passengers = ride.getPassengers().stream()
            .map(Passenger::getUser)
            .collect(Collectors.toList());
        notificationService.deleteRideRelatedNotificationsForPassengers(passengers, ride.getId());

        // Send ride finished notifications to all passengers (creator gets rating request)
        for (Passenger passenger : ride.getPassengers()) {
            boolean isCreator = passenger.getRole() == PassengerRole.CREATOR;
            notificationService.createRideFinishedNotification(passenger.getUser(), ride, isCreator);
        }

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
        nextRide.setStartTime(LocalDateTime.now());
        rideRepository.save(nextRide);

        return true;
    }

    public InconsistencyResponseDTO reportInconsistency(InconsistencyRequestDTO req, Long userId) {
        RegisteredUser regUser = registeredUserRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));
        Ride ride = rideRepository.findById(req.getRideId()).orElseThrow(() -> new EntityNotFoundException("Ride with id " + req.getRideId() + " not found"));
        Passenger passenger = passengerRepository.findByUserAndRide(regUser, ride).orElseThrow(() -> new EntityNotFoundException("Passenger with not found"));

        Inconsistency inconsistency = new Inconsistency(ride, passenger, req.getDescription());
        Inconsistency savedInconsistency = inconsistencyRepository.save(inconsistency);
        return new InconsistencyResponseDTO(savedInconsistency);
    }

    public void startRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException("Ride with id " + rideId + " not found"));

        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartTime(LocalDateTime.now());
        Driver driver = ride.getDriver();
        driver.setStatus(DriverStatus.INACTIVE);
        driverRepository.save(driver);
        rideRepository.save(ride);

        List<Long> participantIds = new ArrayList<>();
        if(ride.getDriver() != null){
            participantIds.add(ride.getDriver().getId());

        }
        // Send ride started notification to all passengers
        for (Passenger passenger : ride.getPassengers()) {
            notificationService.createRideStartedNotification(passenger.getUser(), ride);
            participantIds.add(passenger.getUser().getId());
        }

        webSocketHandler.registerRideParticipants(rideId, participantIds);
        System.out.println("Started ride " + rideId + " with " + participantIds.size() + " participants");
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
            Route route = ride.getRoute();
            CurrentRideDTO currentRideDTO = new CurrentRideDTO();
            currentRideDTO.setEstimatedDurationMin((int) route.getEstimatedTimeMin());
            currentRideDTO.setRoute(new RouteDTO(route));
            currentRideDTO.setRideId(ride.getId());
            currentRideDTO.setPanic(ride.getPanicAlert() != null);

            List<Long> participantIds = new ArrayList<>();
            if(ride.getDriver() != null){
                participantIds.add(ride.getDriver().getId());
            }
            for (Passenger passenger : ride.getPassengers()) {
                participantIds.add(passenger.getUser().getId());
            }
            webSocketHandler.registerRideParticipants(ride.getId(), participantIds);
            System.out.println("Re-registered " + participantIds.size() + " participants for ride " + ride.getId());

            return currentRideDTO;
        } catch (Exception e) {
            throw new Exception(e);
        }
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
        if (passenger.getRole() != PassengerRole.CREATOR) {
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

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("rideId", ride.getId());
        eventData.put("triggeredBy", "DRIVER");
        eventData.put("endAddress", response.getEndAddress());
        eventData.put("distanceKm", response.getDistanceKm());
        eventData.put("estimatedDurationMin", response.getEstimatedDurationMin());
        eventData.put("price", response.getPrice());
        eventData.put("route", response.getRoute());
        webSocketHandler.broadcastRideStop(ride.getId(), eventData);
        System.out.println("Ride " + ride.getId() + " stopped early");

        return response;
    }

    private StopRideResponseDTO completeRide(Ride ride, Vehicle vehicle) throws Exception {
        double latStart =  ride.getRoute().getStartLocation().getLatitude();
        double lonStart =  ride.getRoute().getStartLocation().getLongitude();

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

        double latStart = ride.getRoute().getStartLocation().getLatitude();
        double lonStart = ride.getRoute().getStartLocation().getLongitude();

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

    public StartRideResponseDTO passangerPickup(Long id) {
        Ride ride = rideRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ride with id " + id + " not found"));

        StartRideResponseDTO responseDTO = new StartRideResponseDTO();
        responseDTO.setId(ride.getId());
        // Start and End Address should be up to third comma
        String startAddress = ride.getRoute().getStartLocation().getAddress();
        String endAddress = ride.getRoute().getEndLocation().getAddress();
        String[] startParts = startAddress.split(",", 4);
        String[] endParts = endAddress.split(",", 4);
        responseDTO.setStartAddress(startParts.length >= 3 ? String.join(",", startParts[0], startParts[1], startParts[2]) : startAddress);
        responseDTO.setEndAddress(endParts.length >= 3 ? String.join(",", endParts[0], endParts[1], endParts[2]) : endAddress);

        // Stop lats and lons
        List<Double> stopLats = new ArrayList<>();
        List<Double> stopLons = new ArrayList<>();
        for (Location stop : ride.getRoute().getStopLocations()) {
            stopLats.add(stop.getLatitude());
            stopLons.add(stop.getLongitude());
        }
        responseDTO.setStopLats(stopLats);
        responseDTO.setStopLngs(stopLons);

        // Map model PolylinePoint objects to DTOs expected by the response
        List<RoutePointDTO> routePoints = ride.getRoute().getPolylinePoints().stream()
                .map(pp -> {
                    RoutePointDTO rp = new RoutePointDTO();
                    rp.setLat(pp.getLatitude());
                    rp.setLng(pp.getLongitude());
                    return rp;
                })
                .collect(Collectors.toList());
        responseDTO.setRoute(routePoints);

        List<String> passengerNames = new ArrayList<>();

        for (Passenger passenger : ride.getPassengers()) {
            RegisteredUser user = passenger.getUser();
            passengerNames.add(user.getFirstName() + " " + user.getLastName());
        }
        responseDTO.setPassengers(passengerNames);
        List<String> imageUrls = new ArrayList<>();
        for (Passenger passenger : ride.getPassengers()) {
            RegisteredUser user = passenger.getUser();
            imageUrls.add(user.getProfileImage());
        }
        responseDTO.setPassengerImages(imageUrls);
        return responseDTO;
    }

    public void reorderRide(ReorderRideRequestDTO request) throws Exception {
        Optional<Ride> optionalRide = rideRepository.findById(request.getRideId());
        if (optionalRide.isEmpty()) {
            throw new Exception("Ride does not exist");
        }

        if (request.getScheduledTime() != null && request.getScheduledTime().isBefore(LocalDateTime.now())) {
            throw new Exception("Scheduled time must be in the future");
        }

        String email = "";
        Ride ride = optionalRide.get();
        List<Passenger> passengers = ride.getPassengers();
        for (Passenger passenger : passengers) {
            if (passenger.getRole() == PassengerRole.CREATOR) {
                email = passenger.getUser().getEmail();
            }
        }
        Optional<RegisteredUser> optionalRegisteredUser = registeredUserRepository.findByEmail(email);
        if (optionalRegisteredUser.isEmpty()){
            throw new Exception("User does not exist");
        }
        RegisteredUser registeredUser = optionalRegisteredUser.get();
        OrderRideRequestDTO dto = buildOrderRequestFromRide(registeredUser, ride, request);

        orderRide(dto, email);
    }

    public List<ActiveRideDTO> getActiveRides(){
        List<Ride> activeRides = rideRepository.findActiveRides();
        List<ActiveRideDTO> activeRideDTOs = new ArrayList<>();
        for (Ride ride : activeRides) {
            ActiveRideDTO dto = new ActiveRideDTO();
            dto.setRideId(ride.getId());
            dto.setDriverName(ride.getDriver().getFirstName() + " " + ride.getDriver().getLastName());
            dto.setStartTime(ride.getStartTime());
            dto.setRoute(new RouteDTO(ride.getRoute()));
            List<String> passengerNames = new ArrayList<>();
            for (Passenger passenger : ride.getPassengers()) {
                RegisteredUser user = passenger.getUser();
                passengerNames.add(user.getFirstName() + " " + user.getLastName());
            }
            dto.setPassengerNames(String.join(", ", passengerNames));
            if(ride.getPanicAlert() != null){
                dto.setPanic(true);
                dto.setPanicBy(ride.getPanicAlert().getPanicBy());
            }else{
                dto.setPanic(false);
                dto.setPanicBy(null);
            }
            activeRideDTOs.add(dto);
        }
        return activeRideDTOs;
    }

    private OrderRideRequestDTO buildOrderRequestFromRide(RegisteredUser registeredUser, Ride ride, ReorderRideRequestDTO request) {
        OrderRideRequestDTO dto = new OrderRideRequestDTO();

        dto.setStartAddress(ride.getRoute().getStartLocation().getAddress());
        dto.setStartLatitude(ride.getRoute().getStartLocation().getLatitude());
        dto.setStartLongitude(ride.getRoute().getStartLocation().getLongitude());

        dto.setEndAddress(ride.getRoute().getEndLocation().getAddress());
        dto.setEndLatitude(ride.getRoute().getEndLocation().getLatitude());
        dto.setEndLongitude(ride.getRoute().getEndLocation().getLongitude());

        List<Location> stopLocations = ride.getRoute().getStopLocations();
        if (stopLocations != null && !stopLocations.isEmpty()) {
            List<String> stopAddresses = new ArrayList<>();
            List<Double> stopLatitudes = new ArrayList<>();
            List<Double> stopLongitudes = new ArrayList<>();

            for (Location stopLocation : stopLocations) {
                stopAddresses.add(stopLocation.getAddress());
                stopLatitudes.add(stopLocation.getLatitude());
                stopLongitudes.add(stopLocation.getLongitude());
            }

            dto.setStopAddresses(stopAddresses);
            dto.setStopLatitudes(stopLatitudes);
            dto.setStopLongitudes(stopLongitudes);
        }

        dto.setVehicleType(ride.getDriver().getVehicle().getType().name());
        dto.setBabyFriendly(ride.getDriver().getVehicle().isChildFriendly());
        dto.setPetFriendly(ride.getDriver().getVehicle().isPetFriendly());

        List<String> linkedPassengers = new ArrayList<>();
        for (Passenger passenger : ride.getPassengers()) {
            if (passenger.getRole() != PassengerRole.CREATOR) {
                linkedPassengers.add(passenger.getUser().getEmail());
            }
        }
        dto.setLinkedPassengers(linkedPassengers);

        dto.setDistanceKm(ride.getDistanceKm());
        dto.setEstimatedTimeMinutes((int) ride.getRoute().getEstimatedTimeMin());
        dto.setPriceEstimate(ride.getPrice());

        if (ride.getRoute().getPolylinePoints() != null) {
            List<Double> routeLatitudes = new ArrayList<>();
            List<Double> routeLongitudes = new ArrayList<>();

            for (PolylinePoint point : ride.getRoute().getPolylinePoints()) {
                routeLatitudes.add(point.getLatitude());
                routeLongitudes.add(point.getLongitude());
            }
            dto.setRouteLattitudes(routeLatitudes);
            dto.setRouteLongitudes(routeLongitudes);
        }

        if (request != null) {
            dto.setScheduledTime(request.getScheduledTime());
        }

        dto.setFavoriteRouteId(ride.getRoute().getId());
        return dto;
    }
}