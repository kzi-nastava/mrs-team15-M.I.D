package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.passenger.RideHistoryItemDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.FavoriteRouteResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RoutePointDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RouteResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.FavoriteRoute;
import rs.ac.uns.ftn.asd.ridenow.model.RegisteredUser;
import rs.ac.uns.ftn.asd.ridenow.model.Route;
import rs.ac.uns.ftn.asd.ridenow.model.Ride;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;
import rs.ac.uns.ftn.asd.ridenow.repository.RegisteredUserRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.RideRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.RouteRepository;
import jakarta.persistence.EntityNotFoundException;

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
    private final RideRepository rideRepository;
    private final RouteRepository routeRepository;

    public PassengerService(PriceService priceService,
                            RegisteredUserRepository registeredUserRepository,
                            RideRepository rideRepository,
                            RouteRepository routeRepository) {
        this.priceService = priceService;
        this.registeredUserRepository = registeredUserRepository;
        this.rideRepository = rideRepository;
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
        FavoriteRouteResponseDTO dto = new FavoriteRouteResponseDTO();
        Collection<FavoriteRouteResponseDTO> dtos = new ArrayList<>();
        List<FavoriteRoute> routes = user.getFavoriteRoutes();
        for (FavoriteRoute fr : routes) {
            Route route = fr.getRoute();
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

    public List<RideHistoryItemDTO> getRideHistory(Long id, String dateFrom, String dateTo, String sortBy, String sortDirection) {
        List<RideHistoryItemDTO> history = new ArrayList<>();

        // parse optional date filters (format yyyy-MM-dd). If parsing fails, ignore the filter.
        LocalDateTime fromDateTime = null;
        LocalDateTime toDateTime = null;
        try {
            if (dateFrom != null && !dateFrom.isBlank()) {
                LocalDate d = LocalDate.parse(dateFrom);
                fromDateTime = d.atStartOfDay();
            }
        } catch (Exception ignored) {}
        try {
            if (dateTo != null && !dateTo.isBlank()) {
                LocalDate d = LocalDate.parse(dateTo);
                toDateTime = d.atTime(23, 59, 59);
            }
        } catch (Exception ignored) {}

        List<Ride> allRides = rideRepository.findAll();

        for (Ride ride : allRides) {
            boolean isPassenger = ride.getPassengers().stream()
                    .anyMatch(p -> p.getUser() != null && p.getUser().getId() != null && p.getUser().getId().equals(id));
            if (!isPassenger) continue;

            LocalDateTime ref = ride.getScheduledTime() != null ? ride.getScheduledTime() : ride.getStartTime();
            if (fromDateTime != null && (ref == null || ref.isBefore(fromDateTime))) continue;
            if (toDateTime != null && (ref == null || ref.isAfter(toDateTime))) continue;

            RideHistoryItemDTO dto = new RideHistoryItemDTO();
            dto.setId(ride.getId());
            dto.setStartAddress(ride.getRoute() != null && ride.getRoute().getStartLocation() != null ? ride.getRoute().getStartLocation().getAddress() : null);
            dto.setEndAddress(ride.getRoute() != null && ride.getRoute().getEndLocation() != null ? ride.getRoute().getEndLocation().getAddress() : null);
            dto.setStartTime(ride.getStartTime());
            dto.setEndTime(ride.getEndTime());
            dto.setCancelled(ride.getCancelled() != null ? ride.getCancelled() : false);
            dto.setCancelledBy(ride.getCancelledBy());
            dto.setPrice(ride.getPrice());
            dto.setPanicTriggered(ride.getPanicAlert() != null);

            RegisteredUser user = registeredUserRepository.getReferenceById(id);
            boolean isFavorite = false;
            for (FavoriteRoute fr : user.getFavoriteRoutes()) {
                if (fr.getRoute() != null && ride.getRoute() != null && fr.getRoute().getId() != null && ride.getRoute().getId() != null &&
                        fr.getRoute().getId().equals(ride.getRoute().getId())) {
                    isFavorite = true;
                    break;
                }
            }
            dto.setFavoriteRoute(isFavorite);
            dto.setRouteId(ride.getRoute() != null ? ride.getRoute().getId() : null);
            history.add(dto);
        }

        // Sorting - default by startTime desc
        if ("date".equalsIgnoreCase(sortBy)) {
            if ("asc".equalsIgnoreCase(sortDirection)) {
                history.sort(Comparator.comparing(RideHistoryItemDTO::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())));
            } else {
                history.sort(Comparator.comparing(RideHistoryItemDTO::getStartTime, Comparator.nullsLast(Comparator.reverseOrder())));
            }
        } else {
            history.sort(Comparator.comparing(RideHistoryItemDTO::getStartTime, Comparator.nullsLast(Comparator.reverseOrder())));
        }

        return history;
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
