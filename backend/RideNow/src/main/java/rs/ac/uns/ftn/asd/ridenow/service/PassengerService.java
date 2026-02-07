package rs.ac.uns.ftn.asd.ridenow.service;

import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.driver.DriverHistoryItemDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RatingDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.model.RouteDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.passenger.RideHistoryItemDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.FavoriteRouteResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RoutePointDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RouteResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.*;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;
import rs.ac.uns.ftn.asd.ridenow.repository.*;
import jakarta.persistence.EntityNotFoundException;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class PassengerService {

    private final PriceService priceService;
    private final RegisteredUserRepository registeredUserRepository;
    private final RouteRepository routeRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private HistoryRepository historyRepository;

    public PassengerService(PriceService priceService,
                            RegisteredUserRepository registeredUserRepository,
                            RouteRepository routeRepository) {
        this.priceService = priceService;
        this.registeredUserRepository = registeredUserRepository;
        this.routeRepository = routeRepository;
    }

    public RouteResponseDTO addToFavorites(Long userId, Long routeId) {

        // fetch user and route
        RegisteredUser user = registeredUserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new EntityNotFoundException("Route with id " + routeId + " not found"));

        // check if already in favorites
        boolean exists = user.getFavoriteRoutes().stream()
                .anyMatch(fr -> fr.getRoute() != null && fr.getRoute().getId() != null && fr.getRoute().getId().equals(routeId));

        if (!exists) {
            FavoriteRoute fr = new FavoriteRoute();
            fr.setRoute(route);
            fr.assignUser(user);
            registeredUserRepository.save(user);
        }

        // build response DTO from route
        RouteResponseDTO dto = new RouteResponseDTO();
        dto.setRouteId(route.getId());
        dto.setDistanceKm(route.getDistanceKm());
        dto.setEstimatedTimeMinutes((int) route.getEstimatedTimeMin());
        dto.setPriceEstimateStandard(priceService.calculatePrice(VehicleType.STANDARD, route.getDistanceKm()));
        dto.setPriceEstimateLuxury(priceService.calculatePrice(VehicleType.LUXURY, route.getDistanceKm()));
        dto.setPriceEstimateVan(priceService.calculatePrice(VehicleType.VAN, route.getDistanceKm()));
        if (route.getEndLocation() != null) {
            dto.setEndAddress(route.getEndLocation().getAddress());
            dto.setEndLatitude(route.getEndLocation().getLatitude());
            dto.setEndLongitude(route.getEndLocation().getLongitude());
        }
        if (route.getStartLocation() != null) {
            dto.setStartAddress(route.getStartLocation().getAddress());
            dto.setStartLatitude(route.getStartLocation().getLatitude());
            dto.setStartLongitude(route.getStartLocation().getLongitude());
        }

        if (route.getStopLocations() != null) {
            List<String> stops = new ArrayList<>();
            route.getStopLocations().forEach(stop -> stops.add(stop.getAddress()));
            dto.setStopAddresses(stops);
            List<Double> stopLats = new ArrayList<>();
            route.getStopLocations().forEach(stop -> stopLats.add(stop.getLatitude()));
            dto.setStopLatitudes(stopLats);
            List<Double> stopLons = new ArrayList<>();
            route.getStopLocations().forEach(stop -> stopLons.add(stop.getLongitude()));
            dto.setStopLongitudes(stopLons);
        }

        List<RoutePointDTO> routePoints = route.getPolylinePoints().stream()
                .map(pp -> {
                    RoutePointDTO rp = new RoutePointDTO();
                    rp.setLat(pp.getLatitude());
                    rp.setLng(pp.getLongitude());
                    return rp;
                }).collect(Collectors.toList());
        dto.setRoute(routePoints);

        return dto;
    }


    public void removeFromFavorites(Long userId, Long routeId) {

        // fetch user and find favorite route
        RegisteredUser user = registeredUserRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));

        FavoriteRoute found = null;
        for (FavoriteRoute fr : new ArrayList<>(user.getFavoriteRoutes())) {
            if (fr.getRoute() != null && fr.getRoute().getId() != null && fr.getRoute().getId().equals(routeId)) {
                found = fr;
                break;
            }
        }

        if (found == null) {
            throw new EntityNotFoundException("Favorite route with id " + routeId + " not found for user " + userId);
        }

        user.getFavoriteRoutes().remove(found);
        found.setUser(null);
        registeredUserRepository.save(user);
    }

    public Collection<FavoriteRouteResponseDTO> getRoutes(Long userId) {
        RegisteredUser user = registeredUserRepository.getReferenceById(userId);
        Collection<FavoriteRouteResponseDTO> dtos = new ArrayList<>();
        List<FavoriteRoute> routes = user.getFavoriteRoutes();
        for (FavoriteRoute fr : routes) {
            Route route = fr.getRoute();
            FavoriteRouteResponseDTO dto = new FavoriteRouteResponseDTO();

            dto.setRouteId(route.getId());
            dto.setEndAddress(route.getEndLocation().getAddress());
            dto.setStartAddress(route.getStartLocation().getAddress());
            //Stops
            if (route.getStopLocations() != null) {
                List<String> stops = new ArrayList<>();
                route.getStopLocations().forEach(stop -> stops.add(stop.getAddress()));
                dto.setStopAddresses(stops);
            }

            dtos.add(dto);
        }
        return dtos;
    }

    public Page<RideHistoryItemDTO> getRideHistory(User user, Pageable pageable, Long date) {
        List<RideHistoryItemDTO> passengerHistory = new ArrayList<>();
        Page<Ride> passengerRides= getRides(user, pageable, date);
        for (Ride ride : passengerRides.getContent()) {
            RideHistoryItemDTO dto = new RideHistoryItemDTO();
            dto.setRoute(new RouteDTO(ride.getRoute()));
            dto.setStartTime(ride.getStartTime());
            dto.setEndTime(ride.getEndTime());
            dto.setPrice(ride.getPrice());
            dto.setRideId(ride.getId());
            dto.setDriver(ride.getDriver() != null ? ride.getDriver().getFirstName() + " " + ride.getDriver().getLastName() : null);

            List<String> passengerNames = new ArrayList<>();
            for (Passenger p : ride.getPassengers()) {
                passengerNames.add(p == null ? null : p.getUser().getFirstName() + " " + p.getUser().getLastName());
            }
            dto.setPassengers(passengerNames);

            if (ride.getPanicAlert() != null) {
                dto.setPanic(true);
                if (ride.getPanicAlert().getPanicBy() != null) {
                    dto.setPanicBy(ride.getPanicAlert().getPanicBy());
                }
            }

            dto.setCancelled(ride.getCancelled());
            if (ride.getCancelled()) {
                dto.setCancelledBy(ride.getCancelledBy());
            }

            if (ratingRepository.findByRide(ride) != null) {
                dto.setRating(new RatingDTO(ratingRepository.findByRide(ride)));
            }

            List<Inconsistency> inconsistencies = ride.getInconsistencies();
            List<String> inconsistencyStrings = new ArrayList<>();
            if (!inconsistencies.isEmpty()) {
                for (Inconsistency inconsistency : inconsistencies) {
                    inconsistencyStrings.add(inconsistency.getDescription());
                }
            }
            dto.setInconsistencies(inconsistencyStrings);

            RegisteredUser registeredUser = registeredUserRepository.getReferenceById(user.getId());
            boolean isFavorite = false;
            for (FavoriteRoute fr : registeredUser.getFavoriteRoutes()) {
                if (fr.getRoute() != null && ride.getRoute() != null && fr.getRoute().getId() != null && ride.getRoute().getId() != null &&
                        fr.getRoute().getId().equals(ride.getRoute().getId())) {
                    isFavorite = true;
                    break;
                }
            }
            dto.setFavoriteRoute(isFavorite);
            dto.setRouteId(ride.getRoute() != null ? ride.getRoute().getId() : null);

            passengerHistory.add(dto);
        }
        return new PageImpl(passengerHistory, pageable, passengerRides.getTotalElements());
    }

    private Page<Ride> getRides(User user, Pageable pageable, Long date) {
        LocalDateTime startOfDay = null;
        LocalDateTime endOfDay = null;

        if (date != null) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault());
            startOfDay = dateTime.toLocalDate().atStartOfDay();
            endOfDay = dateTime.toLocalDate().atTime(23, 59, 59);
        }
        return date != null ?
                findByDriverWithAllRelationsAndDate(user, startOfDay, endOfDay, pageable) :
                findByDriverWithAllRelations(user, pageable);
    }

    private Page<Ride> findByDriverWithAllRelations(User user, Pageable pageable) {
        if (user instanceof Driver) {
            return historyRepository.findDriverRidesWithAllRelations(user.getId(), pageable);
        }
        return historyRepository.findPassengerRidesWithAllRelations(user.getId(), pageable);
    }

    private Page<Ride> findByDriverWithAllRelationsAndDate(User user, LocalDateTime startOfDay, LocalDateTime endOfDay, Pageable pageable) {
        if (user instanceof Driver) {
            return historyRepository.findDriverRidesWithAllRelationsAndDate(user.getId(), startOfDay, endOfDay, pageable);
        }
        return historyRepository.findPassengerRidesWithAllRelationsAndDate(user.getId(), startOfDay, endOfDay, pageable);
    }

    public RouteResponseDTO getRoute(Long id, Long id1) {
        RegisteredUser user = registeredUserRepository.getReferenceById(id);

        FavoriteRoute found = null;
        for (FavoriteRoute fr : user.getFavoriteRoutes()) {
            if (fr.getRoute() != null && fr.getRoute().getId() != null && fr.getRoute().getId().equals(id1)) {
                found = fr;
                break;
            }
        }

        if (found == null) {
            throw new EntityNotFoundException("Favorite route with id " + id1 + " not found for user " + id);
        }

        Route route = found.getRoute();
        RouteResponseDTO dto = new RouteResponseDTO();
        dto.setRouteId(route.getId());
        dto.setDistanceKm(route.getDistanceKm());
        dto.setEstimatedTimeMinutes((int) route.getEstimatedTimeMin());
        dto.setPriceEstimateStandard(priceService.calculatePrice(VehicleType.STANDARD, route.getDistanceKm()));
        dto.setPriceEstimateLuxury(priceService.calculatePrice(VehicleType.LUXURY, route.getDistanceKm()));
        dto.setPriceEstimateVan(priceService.calculatePrice(VehicleType.VAN, route.getDistanceKm()));
        dto.setEndAddress(route.getEndLocation().getAddress());
        dto.setEndLatitude(route.getEndLocation().getLatitude());
        dto.setEndLongitude(route.getEndLocation().getLongitude());
        dto.setStartAddress(route.getStartLocation().getAddress());
        dto.setStartLatitude(route.getStartLocation().getLatitude());
        dto.setStartLongitude(route.getStartLocation().getLongitude());
        //Stops
        if (route.getStopLocations() != null) {
            List<String> stops = new ArrayList<>();
            route.getStopLocations().forEach(stop -> stops.add(stop.getAddress()));
            dto.setStopAddresses(stops);
            List<Double> stopLats = new ArrayList<>();
            route.getStopLocations().forEach(stop -> stopLats.add(stop.getLatitude()));
            dto.setStopLatitudes(stopLats);
            List<Double> stopLons = new ArrayList<>();
            route.getStopLocations().forEach(stop -> stopLons.add(stop.getLongitude()));
            dto.setStopLongitudes(stopLons);
        }
        // Route Polyline Points
        List<RoutePointDTO> routePoints = route.getPolylinePoints().stream()
                .map(pp -> {
                    RoutePointDTO rp = new RoutePointDTO();
                    rp.setLat(pp.getLatitude());
                    rp.setLng(pp.getLongitude());
                    return rp;
                })
                .collect(Collectors.toList());
        dto.setRoute(routePoints);
        return dto;
    }
}
