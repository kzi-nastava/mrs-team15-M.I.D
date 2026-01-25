package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RoutePointDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RouteResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.FavoriteRoute;
import rs.ac.uns.ftn.asd.ridenow.model.RegisteredUser;
import rs.ac.uns.ftn.asd.ridenow.model.Route;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;
import rs.ac.uns.ftn.asd.ridenow.repository.RegisteredUserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PassengerService {

    @Autowired
    private PriceService priceService;
    @Autowired
    private RegisteredUserRepository registeredUserRepository;

    public RouteResponseDTO addToFavorites(Long userId, Long routeId) {

        RouteResponseDTO dto = new RouteResponseDTO();
        dto.setRouteId(routeId);
        dto.setDistanceKm(14.0);
        dto.setEstimatedTimeMinutes(25);
        dto.setPriceEstimateStandard(1800);

        return dto;
    }


    public void removeFromFavorites(Long userId, Long routeId) {
        // mock: route removed from favorites
    }

    public Collection<RouteResponseDTO> getRoutes(Long userId) {
        RegisteredUser user = registeredUserRepository.getReferenceById(userId);
        RouteResponseDTO dto = new RouteResponseDTO();
        Collection<RouteResponseDTO> dtos = new ArrayList<>();
        List<FavoriteRoute> routes = user.getFavoriteRoutes();
        for (FavoriteRoute fr : routes) {
            Route route = fr.getRoute();
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

            dtos.add(dto);
        }
        return dtos;
    }
}
