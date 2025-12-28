package rs.ac.uns.ftn.asd.ridenow.service;

import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.*;
import rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus;

import java.time.LocalDateTime;

@Service
public class RideService {

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

        //  test ride data
        response.setId(1L);
        response.setStatus(RideStatus.valueOf("REQUESTED"));
        response.setRouteId(1L);
        response.setScheduledTime(LocalDateTime.parse("2024-07-01T10:00:00"));
        response.setVehicleType("Standard");
        response.setBabyFriendly(true);
        response.setPetFriendly(false);

        return response;
    }

    public void startRide(Long rideId) {
        // mock – no logic
    }
}
