package rs.ac.uns.ftn.asd.ridenow.service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RideEstimateResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.StopRideResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.*;
import rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;
import rs.ac.uns.ftn.asd.ridenow.repository.RideRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.RouteRepository;
import rs.ac.uns.ftn.asd.ridenow.websocket.NotificationWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RideService - stopRide() Tests")
public class StopRideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private RoutingService routingService;

    @Mock
    private PriceService priceService;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private NotificationWebSocketHandler webSocketHandler;

    @InjectMocks
    private RideService rideService;

    private Driver driver;
    private Vehicle vehicle;
    private Route route;
    private Ride ride;
    private RideEstimateResponseDTO estimation;
    private Route existingRoute;

    @BeforeEach
    void setUp() {
        driver = createDriver();
        vehicle = createVehicle();
        route = createRoute();
        ride = createRide();
        estimation = createRideEstimation();
        existingRoute = createExistingRoute();
    }

    // ============= NEGATIVE TESTS ================

    @Test
    @DisplayName("Should throw exception when user is not a driver")
    void stopRideThrowsExceptionWhenUserIsNotDriver() {
        RegisteredUser regularUser = new RegisteredUser();

        Exception exception = assertThrows(Exception.class, () -> { rideService.stopRide(regularUser);});

        assertEquals("You are not a driver.", exception.getMessage());
        verify(rideRepository, never()).findCurrentRideByDriver(anyLong());
        verify(rideRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when driver has no active ride")
    void stopRideThrowsExceptionWhenNoActiveRide() throws Exception {
        when(rideRepository.findCurrentRideByDriver(driver.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> {rideService.stopRide(driver);});

        assertEquals("You don't have a ride in progress.", exception.getMessage());
        verify(rideRepository, times(1)).findCurrentRideByDriver(driver.getId());
        verify(rideRepository, never()).save(any());
        verify(routingService, never()).getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("Should throw exception when driver has no vehicle")
    void stopRideThrowsExceptionWhenDriverHasNoVehicle() {
        driver.setVehicle(null);
        when(rideRepository.findCurrentRideByDriver(driver.getId())).thenReturn(Optional.of(ride));

        assertThrows(NullPointerException.class, () -> {rideService.stopRide(driver);});

        verify(rideRepository, times(1)).findCurrentRideByDriver(driver.getId());
        verify(rideRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when routing service fails")
    void stopRideThrowsExceptionWhenRoutingServiceFails() throws Exception {
        when(rideRepository.findCurrentRideByDriver(driver.getId())).thenReturn(Optional.of(ride));
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenThrow(new Exception("Routing service unavailable"));

        Exception exception = assertThrows(Exception.class, () -> {rideService.stopRide(driver);});

        assertTrue(exception.getMessage().contains("Routing service unavailable"));
        verify(routingService, times(1)).getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        verify(rideRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when price service fails")
    void stopRideThrowsExceptionWhenPriceServiceFails() throws Exception {
        when(rideRepository.findCurrentRideByDriver(driver.getId())).thenReturn(Optional.of(ride));
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(estimation);
        when(priceService.calculatePrice(any(VehicleType.class), anyDouble())).thenThrow(new RuntimeException("Price calculation failed"));

        Exception exception = assertThrows(Exception.class, () -> {rideService.stopRide(driver);});

        assertTrue(exception.getMessage().contains("Price calculation failed"));
        verify(rideRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when geocoding fails")
    void stopRideThrowsExceptionWhenGeocodingFails() throws Exception {
        when(rideRepository.findCurrentRideByDriver(driver.getId())).thenReturn(Optional.of(ride));
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(estimation);
        when(priceService.calculatePrice(vehicle.getType(), estimation.getDistanceKm())).thenReturn(290.49);
        when(routingService.getReverseGeocode(anyDouble(), anyDouble())).thenReturn("Zlatne grede 4, Novi Sad");
        when(routeRepository.findByStartAndEndAddress(any(), any())).thenReturn(Optional.empty());
        when(routingService.getGeocode(anyString())).thenThrow(new Exception("Geocoding failed"));

        Exception exception = assertThrows(Exception.class, () -> { rideService.stopRide(driver);});

        assertTrue(exception.getMessage().contains("Geocoding failed"));
        verify(routeRepository, never()).save(any());
    }

    // ============= POSITIVE TESTS ====================

    @Test
    @DisplayName("Should successfully stop ride and create new route when route doesn't exist")
    void stopRideSuccessWhenDriverHasActiveRideAndCreatesNewRoute() throws Exception {
        when(rideRepository.findCurrentRideByDriver(driver.getId())).thenReturn(Optional.of(ride));
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(estimation);
        when(priceService.calculatePrice(vehicle.getType(), estimation.getDistanceKm())).thenReturn(290.49);
        when(routingService.getReverseGeocode(anyDouble(), anyDouble())).thenReturn("Zlatne grede 4, Novi Sad");
        when(routeRepository.findByStartAndEndAddress(any(), any())).thenReturn(Optional.empty());
        when(routingService.getGeocode(anyString())).thenReturn(new double[]{45.2671, 19.8335});
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        StopRideResponseDTO result = rideService.stopRide(driver);

        assertNotNull(result);
        assertEquals(5.5, result.getDistanceKm());
        assertEquals(7, result.getEstimatedDurationMin());
        assertEquals(290.49, result.getPrice());
        assertEquals("Zlatne grede 4, Novi Sad", result.getEndAddress());
        assertEquals(RideStatus.FINISHED, ride.getStatus());
        assertNotNull(ride.getEndTime());
        assertEquals(5.5, ride.getDistanceKm());
        assertEquals(290.49, ride.getPrice());

        verify(rideRepository, times(1)).findCurrentRideByDriver(driver.getId());
        verify(routingService, times(2)).getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        verify(priceService, times(1)).calculatePrice(vehicle.getType(), 5.5);
        verify(routingService, times(1)).getReverseGeocode(vehicle.getLat(), vehicle.getLon());
        verify(routingService, times(1)).getGeocode("Zlatne grede 4, Novi Sad");
        verify(routeRepository, times(1)).save(any(Route.class));
        verify(rideRepository, times(1)).save(ride);
    }

    @Test
    @DisplayName("Should successfully stop ride and use existing route when route exists")
    void stopRideSuccessWhenDriverHasActiveRideAndUsesExistingRoute() throws Exception {
        when(rideRepository.findCurrentRideByDriver(driver.getId())).thenReturn(Optional.of(ride));
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(estimation);
        when(priceService.calculatePrice(vehicle.getType(), estimation.getDistanceKm())).thenReturn(290.49);
        when(routingService.getReverseGeocode(anyDouble(), anyDouble())).thenReturn("Zlatne grede 4, Novi Sad");
        when(routeRepository.findByStartAndEndAddress("Bulevar oslobodjenja 46, Novi Sad", "Zlatne grede 4, Novi Sad")).thenReturn(Optional.of(existingRoute));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        StopRideResponseDTO result = rideService.stopRide(driver);

        assertNotNull(result);
        assertEquals(290.49, result.getPrice());
        assertEquals(RideStatus.FINISHED, ride.getStatus());
        assertEquals(existingRoute, ride.getRoute());
        verify(routingService, times(1)).getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        verify(routingService, never()).getGeocode(anyString());
        verify(routeRepository, never()).save(any(Route.class));
        verify(rideRepository, times(1)).save(ride);
    }

    @Test
    @DisplayName("Should calculate correct price for luxury vehicle")
    void stopRideSuccessWithLuxuryVehicle() throws Exception {
        vehicle.setType(VehicleType.LUXURY);
        when(rideRepository.findCurrentRideByDriver(driver.getId())).thenReturn(Optional.of(ride));
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(estimation);
        when(priceService.calculatePrice(VehicleType.LUXURY, estimation.getDistanceKm())).thenReturn(450.0);
        when(routingService.getReverseGeocode(anyDouble(), anyDouble())).thenReturn("Zlatne grede 4, Novi Sad");
        when(routeRepository.findByStartAndEndAddress(any(), any())).thenReturn(Optional.of(existingRoute));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        StopRideResponseDTO result = rideService.stopRide(driver);

        assertEquals(450.0, result.getPrice());
        verify(priceService, times(1)).calculatePrice(VehicleType.LUXURY, 5.5);
    }

    @Test
    @DisplayName("Should set endTime close to current time")
    void stopRideSuccessSetsCorrectEndTime() throws Exception {
        LocalDateTime beforeTest = LocalDateTime.now();
        when(rideRepository.findCurrentRideByDriver(driver.getId())).thenReturn(Optional.of(ride));
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(estimation);
        when(priceService.calculatePrice(any(), anyDouble())).thenReturn(290.49);
        when(routingService.getReverseGeocode(anyDouble(), anyDouble())).thenReturn("Zlatne grede 4, Novi Sad");
        when(routeRepository.findByStartAndEndAddress(any(), any())).thenReturn(Optional.of(existingRoute));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        rideService.stopRide(driver);

        LocalDateTime afterTest = LocalDateTime.now();

        assertNotNull(ride.getEndTime());
        assertTrue(ride.getEndTime().isAfter(beforeTest) || ride.getEndTime().isEqual(beforeTest));
        assertTrue(ride.getEndTime().isBefore(afterTest) || ride.getEndTime().isEqual(afterTest));
    }

    // ============= BOUNDARY TESTS ================

    @Test
    @DisplayName("Should handle very long distance ride")
    void stopRideSuccessWithVeryLongDistance() throws Exception {
        estimation.setDistanceKm(500.0);
        estimation.setEstimatedDurationMin(300);

        when(rideRepository.findCurrentRideByDriver(driver.getId())).thenReturn(Optional.of(ride));
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble())).thenReturn(estimation);
        when(priceService.calculatePrice(any(), eq(500.0))).thenReturn(15000.0);
        when(routingService.getReverseGeocode(anyDouble(), anyDouble())).thenReturn("Beograd");
        when(routeRepository.findByStartAndEndAddress(any(), any())).thenReturn(Optional.empty());
        when(routingService.getGeocode(anyString())).thenReturn(new double[]{46.0, 20.0});
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        StopRideResponseDTO result = rideService.stopRide(driver);

        assertEquals(500.0, result.getDistanceKm());
        assertEquals(15000.0, result.getPrice());
    }

    // ============= Helper Methods =============

    private Driver createDriver() {
        Driver driver = new Driver();
        driver.setId(1L);
        driver.setFirstName("Pera");
        driver.setLastName("Perić");
        return driver;
    }

    private Vehicle createVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setLat(45.2671);
        vehicle.setLon(19.8335);
        driver.setVehicle(vehicle);
        return vehicle;
    }

    private Route createRoute() {
        Location startLocation = new Location();
        startLocation.setAddress("Bulevar oslobodjenja 46, Novi Sad");
        startLocation.setLatitude(45.2550);
        startLocation.setLongitude(19.8450);

        Route route = new Route();
        route.setId(1L);
        route.setStartLocation(startLocation);
        route.setDistanceKm(5.0);
        route.setEstimatedTimeMin(15.0);
        return route;
    }

    private Ride createRide() {
        Ride ride = new Ride();
        ride.setId(1L);
        ride.setRoute(route);
        ride.setDriver(driver);
        ride.setScheduledTime(LocalDateTime.now().minusMinutes(30));
        ride.setStatus(RideStatus.IN_PROGRESS);
        return ride;
    }

    private RideEstimateResponseDTO createRideEstimation() {
        RideEstimateResponseDTO estimation = new RideEstimateResponseDTO();
        estimation.setDistanceKm(5.5);
        estimation.setEstimatedDurationMin(7);
        return estimation;
    }

    private Route createExistingRoute() {
        Location start = new Location();
        start.setAddress("Bulevar oslobodjenja 46, Novi Sad");
        start.setLatitude(45.2550);
        start.setLongitude(19.8450);

        Location end = new Location();
        end.setAddress("Zlatne grede 4, Novi Sad");
        end.setLatitude(45.2671);
        end.setLongitude(19.8335);

        Route existingRoute = new Route();
        existingRoute.setId(999L);
        existingRoute.setStartLocation(start);
        existingRoute.setEndLocation(end);
        existingRoute.setDistanceKm(5.5);
        existingRoute.setEstimatedTimeMin(7.0);
        return existingRoute;
    }
}