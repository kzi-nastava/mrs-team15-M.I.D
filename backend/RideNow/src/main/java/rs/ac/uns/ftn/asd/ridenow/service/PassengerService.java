package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RouteResponseDTO;

@Service
public class PassengerService {

    public RouteResponseDTO addToFavorites(Long userId, Long routeId) {

        RouteResponseDTO dto = new RouteResponseDTO();
        dto.setRouteId(routeId);
        dto.setDistanceKm(14.0);
        dto.setEstimatedTimeMinutes(25);
        dto.setPriceEstimate(1800);

        return dto;
    }



    public void removeFromFavorites(Long userId, Long routeId) {
        // mock: route removed from favorites
    }
}
