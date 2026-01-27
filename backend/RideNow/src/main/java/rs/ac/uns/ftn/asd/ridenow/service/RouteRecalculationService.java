package rs.ac.uns.ftn.asd.ridenow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RideEstimateResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RoutePointDTO;
import rs.ac.uns.ftn.asd.ridenow.model.PolylinePoint;
import rs.ac.uns.ftn.asd.ridenow.model.Route;
import rs.ac.uns.ftn.asd.ridenow.repository.RouteRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouteRecalculationService {

    private static final Logger logger = LoggerFactory.getLogger(RouteRecalculationService.class);

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private RoutingService routingService;

    @Transactional
    public void recalculateAllRoutes() {
        List<Route> routes = routeRepository.findAllWithCollections();
        logger.info("Found {} routes to recalculate", routes.size());

        int successCount = 0;
        int errorCount = 0;

        for (Route route : routes) {
            try {
                logger.info("Recalculating route {} - from {} to {}",
                    route.getId(),
                    route.getStartLocation().getAddress(),
                    route.getEndLocation().getAddress());

                // Extract coordinates from route
                double startLat = route.getStartLocation().getLatitude();
                double startLon = route.getStartLocation().getLongitude();
                double endLat = route.getEndLocation().getLatitude();
                double endLon = route.getEndLocation().getLongitude();

                logger.debug("Route {} coordinates: start({}, {}), end({}, {})",
                    route.getId(), startLat, startLon, endLat, endLon);

                // Skip routes with identical start and end coordinates
                if (startLat == endLat && startLon == endLon) {
                    logger.warn("Skipping route {} - identical start and end coordinates: ({}, {})",
                        route.getId(), startLat, startLon);
                    errorCount++;
                    continue;
                }

                // Prepare stop coordinates if any
                List<Double> stopLats = null;
                List<Double> stopLons = null;
                if (route.getStopLocations() != null && !route.getStopLocations().isEmpty()) {
                    stopLats = new ArrayList<>();
                    stopLons = new ArrayList<>();
                    for (var stop : route.getStopLocations()) {
                        stopLats.add(stop.getLatitude());
                        stopLons.add(stop.getLongitude());
                    }
                    logger.debug("Route {} has {} stop locations", route.getId(), stopLats.size());
                }

                logger.info("Calling routing service for route {}", route.getId());

                // Calculate new route with proper polyline points
                RideEstimateResponseDTO routeResponse;
                try {
                    logger.info("About to call routing service for route {} (start: {},{} -> end: {},{})",
                        route.getId(), startLat, startLon, endLat, endLon);

                    long startTime = System.currentTimeMillis();

                    if (stopLats != null && !stopLats.isEmpty()) {
                        routeResponse = routingService.getRouteWithStops(
                            startLat, startLon, endLat, endLon, stopLats, stopLons);
                    } else {
                        routeResponse = routingService.getRoute(
                            startLat, startLon, endLat, endLon);
                    }

                    long duration = System.currentTimeMillis() - startTime;
                    logger.info("Routing service returned successfully for route {} in {}ms", route.getId(), duration);

                } catch (Exception routingException) {
                    logger.error("Routing service failed for route {}: {}. Skipping this route.",
                        route.getId(), routingException.getMessage());
                    errorCount++;
                    continue; // Skip this route and move to the next one
                }

                // Clear existing polyline points and add new ones
                route.getPolylinePoints().clear();

                if (routeResponse.getRoute() != null) {
                    logger.info("Adding {} polyline points to route {}", routeResponse.getRoute().size(), route.getId());
                    for (RoutePointDTO point : routeResponse.getRoute()) {
                        PolylinePoint polylinePoint = new PolylinePoint(point.getLat(), point.getLng());
                        route.getPolylinePoints().add(polylinePoint);
                    }
                } else {
                    logger.warn("No route points returned for route {}", route.getId());
                }

                // Update distance and time from the new calculation
                route.setDistanceKm(routeResponse.getDistanceKm());
                route.setEstimatedTimeMin(routeResponse.getEstimatedDurationMin());

                logger.info("Saving updated route {}", route.getId());
                // Save the updated route
                routeRepository.save(route);

                successCount++;
                logger.info("Successfully recalculated route {}", route.getId());

            } catch (Exception e) {
                errorCount++;
                logger.error("Failed to recalculate route {}: {}", route.getId(), e.getMessage());
            }

            // Add delay to respect API rate limits
            try {
                Thread.sleep(4000); // 1 second delay between requests
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                logger.warn("Route recalculation interrupted");
                break;
            }
        }

        logger.info("Route recalculation summary: {} successful, {} errors", successCount, errorCount);
    }
}
