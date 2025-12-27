package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.route.RouteResponseDTO;

import java.util.List;

@Service
public class RouteService {

    public void addToFavorites(Long routeId) {
        // mock – no logic
    }

    public void removeFromFavorites(Long routeId) {
        // mock – no logic
    }

    public List<RouteResponseDTO> getFavoriteRoutes() {
        RouteResponseDTO route1 = new RouteResponseDTO();
        route1.setId(1L);
        route1.setStartAddress("Bulevar Oslobođenja 45, Novi Sad");
        route1.setEndAddress("Narodnog fronta 12, Novi Sad");
        route1.setStopAddresses(List.of("Trg slobode 3"));
        route1.setTotalDistanceKm(6.4);
        route1.setEstimatedDurationMin(18);
        route1.setFavorite(true);

        RouteResponseDTO route2 = new RouteResponseDTO();
        route2.setId(2L);
        route2.setStartAddress("Bulevar Evrope 10, Novi Sad");
        route2.setEndAddress("Zmaj Jovina 5, Novi Sad");
        route2.setStopAddresses(List.of());
        route2.setTotalDistanceKm(4.1);
        route2.setEstimatedDurationMin(12);
        route2.setFavorite(true);

        return List.of(route1, route2);
    }

    public RouteResponseDTO getFavoriteRoute(Long routeId) {
        RouteResponseDTO route = new RouteResponseDTO();
        route.setId(routeId);
        route.setStartAddress("Bulevar Oslobođenja 45, Novi Sad");
        route.setEndAddress("Narodnog fronta 12, Novi Sad");
        route.setStopAddresses(List.of("Trg slobode 3"));
        route.setTotalDistanceKm(6.4);
        route.setEstimatedDurationMin(18);
        route.setFavorite(true);

        return route;
    }
}
