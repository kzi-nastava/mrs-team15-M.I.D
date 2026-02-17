package rs.ac.uns.ftn.asd.ridenow.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.OrderRideRequestDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.OrderRideResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RideEstimateResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.model.*;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;
import rs.ac.uns.ftn.asd.ridenow.repository.*;
import rs.ac.uns.ftn.asd.ridenow.websocket.NotificationWebSocketHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RideService - Order Ride Functionality Tests")
public class OrderingRideServiceTest {

    @Mock
    private DriverRepository driverRepository;
    @Mock
    private RideRepository rideRepository;
    @Mock
    private RegisteredUserRepository registeredUserRepository;
    @Mock
    private RouteRepository routeRepository;
    @Mock
    private RoutingService routingService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationWebSocketHandler notificationWebSocketHandler;
    @Mock
    private PanicAlertRepository panicAlertRepository;
    @Mock
    private PriceService priceService;
    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private InconsistencyRepository inconsistencyRepository;
    @Mock
    private PassengerRepository passengerRepository;

    @InjectMocks
    private RideService rideService;

    private OrderRideRequestDTO validRequest;
    private RegisteredUser mainUser;
    private Driver availableDriver;

    @BeforeEach
    void setUp() {
        validRequest = new OrderRideRequestDTO();
        validRequest.setVehicleType("STANDARD");
        validRequest.setStartAddress("Start");
        validRequest.setEndAddress("End");
        validRequest.setStartLatitude(45.0);
        validRequest.setStartLongitude(19.0);
        validRequest.setEndLatitude(45.1);
        validRequest.setEndLongitude(19.1);
        validRequest.setDistanceKm(5.0);
        validRequest.setEstimatedTimeMinutes(10);
        validRequest.setPriceEstimate(100.0);
        validRequest.setRouteLattitudes(List.of(45.0, 45.1));
        validRequest.setRouteLongitudes(List.of(19.0, 19.1));

        mainUser = new RegisteredUser();
        mainUser.setId(1L);
        mainUser.setEmail("user@gmail.com");
        mainUser.setRole(UserRoles.USER);

        availableDriver = new Driver();
        availableDriver.setId(10L);
        Vehicle vehicle = new Vehicle();
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setSeatCount(4);
        vehicle.setLat(45.0);
        vehicle.setLon(19.0);
        availableDriver.setVehicle(vehicle);
        availableDriver.setStatus(DriverStatus.ACTIVE);
        availableDriver.setAvailable(true);
    }

    @Test
    @DisplayName("Valid request returns response with assigned driver")
    void orderRide_validRequest_shouldReturnDriver() throws Exception {
        when(registeredUserRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(mainUser));
        when(driverRepository.findAll()).thenReturn(List.of(availableDriver));
        when(driverRepository.findById(availableDriver.getId()))
                .thenReturn(Optional.of(availableDriver));
        when(rideRepository.findScheduledRidesForDriverInNextHour(anyLong(), any(), any())).thenReturn(List.of());
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new RideEstimateResponseDTO(1, 5));
        when(rideRepository.save(any(Ride.class)))
                .thenAnswer(invocation -> {
                    Ride r = invocation.getArgument(0);
                    r.setId(100L);
                    return r;
                });

        OrderRideResponseDTO response = rideService.orderRide(validRequest, "user@gmail.com");

        assertNotNull(response);
        assertEquals(availableDriver.getId(), response.getDriverId());
        assertEquals("user@gmail.com", response.getMainPassengerEmail());
        assertEquals(validRequest.getStartAddress(), response.getStartAddress());
        assertEquals(validRequest.getEndAddress(), response.getEndAddress());

        verify(rideRepository, times(1)).save(any(Ride.class));
        verify(driverRepository, times(1)).save(availableDriver);
    }

    @Test
    @DisplayName("No available drivers returns response with null driverId")
    void orderRide_noDrivers_shouldReturnNullDriver() {
        when(registeredUserRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(mainUser));
        when(driverRepository.findAll()).thenReturn(List.of());

        OrderRideResponseDTO response = rideService.orderRide(validRequest, "user@gmail.com");

        assertNotNull(response);
        assertNull(response.getDriverId());
        assertEquals(RideStatus.CANCELLED, response.getStatus());
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    @DisplayName("Invalid stop lists throw IllegalArgumentException")
    void orderRide_invalidStops_shouldThrow() {
        validRequest.setStopLatitudes(null);
        validRequest.setStopLongitudes(List.of(1.0));

        assertThrows(IllegalArgumentException.class, () -> rideService.orderRide(validRequest, "user@gmail.com"));
    }

    @Test
    @DisplayName("Invalid vehicle type throws IllegalArgumentException")
    void orderRide_invalidVehicleType_shouldThrow() {
        validRequest.setVehicleType("INVALID");

        assertThrows(IllegalArgumentException.class, () -> rideService.orderRide(validRequest, "user@gmail.com"));
    }

    @Test
    @DisplayName("Linked passengers with invalid email are skipped")
    void orderRide_linkedPassengers_invalidEmail_shouldSkip() throws Exception {
        validRequest.setLinkedPassengers(List.of("missing@gmail.com"));

        when(registeredUserRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(mainUser));
        when(registeredUserRepository.findByEmail("missing@gmail.com")).thenReturn(Optional.empty());
        when(driverRepository.findAll()).thenReturn(List.of(availableDriver));
        when(driverRepository.findById(availableDriver.getId()))
                .thenReturn(Optional.of(availableDriver));
        when(rideRepository.findScheduledRidesForDriverInNextHour(anyLong(), any(), any())).thenReturn(List.of());
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new RideEstimateResponseDTO(1, 5));
        when(rideRepository.save(any(Ride.class)))
                .thenAnswer(invocation -> {
                    Ride r = invocation.getArgument(0);
                    r.setId(100L);
                    return r;
                });

        OrderRideResponseDTO response = rideService.orderRide(validRequest, "user@gmail.com");

        assertNotNull(response);
        assertEquals(availableDriver.getId(), response.getDriverId());
    }

    // ==================== SCHEDULED TIME VALIDATION ====================

    @Test
    @DisplayName("Scheduled time in the past throws IllegalArgumentException")
    void orderRide_scheduledTimeInPast_shouldThrow() {
        validRequest.setScheduledTime(LocalDateTime.now().minusHours(1));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> rideService.orderRide(validRequest, "user@gmail.com")
        );
        assertTrue(exception.getMessage().contains("past"));
    }

    @Test
    @DisplayName("Scheduled time more than 5 hours ahead throws IllegalArgumentException")
    void orderRide_scheduledTimeMoreThan5Hours_shouldThrow() {
        validRequest.setScheduledTime(LocalDateTime.now().plusHours(6));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> rideService.orderRide(validRequest, "user@gmail.com")
        );
        assertTrue(exception.getMessage().contains("5 hours"));
    }

    @Test
    @DisplayName("Scheduled time within 5 hours is valid")
    void orderRide_scheduledTimeWithin5Hours_shouldSucceed() throws Exception {
        validRequest.setScheduledTime(LocalDateTime.now().plusHours(3));

        when(registeredUserRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(mainUser));
        when(driverRepository.findAll()).thenReturn(List.of(availableDriver));
        when(driverRepository.findById(availableDriver.getId())).thenReturn(Optional.of(availableDriver));
        when(rideRepository.findScheduledRidesForDriverInNextHour(anyLong(), any(), any())).thenReturn(List.of());
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new RideEstimateResponseDTO(1, 5));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        OrderRideResponseDTO response = rideService.orderRide(validRequest, "user@gmail.com");

        assertNotNull(response);
        assertNotNull(response.getScheduledTime());
    }

    // ==================== MAIN PASSENGER VALIDATION ====================

    @Test
    @DisplayName("Non-existent main passenger throws EntityNotFoundException")
    void orderRide_mainPassengerNotFound_shouldThrow() throws Exception {
        when(registeredUserRepository.findByEmail("nonexistent@gmail.com")).thenReturn(Optional.empty());
        when(driverRepository.findAll()).thenReturn(List.of(availableDriver));
        when(rideRepository.findScheduledRidesForDriverInNextHour(anyLong(), any(), any())).thenReturn(List.of());
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new RideEstimateResponseDTO(1, 5));

        assertThrows(EntityNotFoundException.class,
                () -> rideService.orderRide(validRequest, "nonexistent@gmail.com"));
    }

    // ==================== ROUTE VALIDATION ====================

    @Test
    @DisplayName("Route latitudes and longitudes size mismatch throws IllegalArgumentException")
    void orderRide_routeCoordinatesMismatch_shouldThrow() {
        validRequest.setRouteLattitudes(List.of(45.0, 45.1));
        validRequest.setRouteLongitudes(List.of(19.0)); // mismatch

        assertThrows(IllegalArgumentException.class,
                () -> rideService.orderRide(validRequest, "user@gmail.com"));
    }

    @Test
    @DisplayName("Stop coordinates size mismatch throws IllegalArgumentException")
    void orderRide_stopCoordinatesMismatch_shouldThrow() {
        validRequest.setStopLatitudes(List.of(45.05, 45.06));
        validRequest.setStopLongitudes(List.of(19.05)); // mismatch
        validRequest.setStopAddresses(List.of("Stop1", "Stop2"));

        assertThrows(IllegalArgumentException.class,
                () -> rideService.orderRide(validRequest, "user@gmail.com"));
    }

    @Test
    @DisplayName("Stop addresses size mismatch throws IllegalArgumentException")
    void orderRide_stopAddressesMismatch_shouldThrow() {
        validRequest.setStopLatitudes(List.of(45.05, 45.06));
        validRequest.setStopLongitudes(List.of(19.05, 19.06));
        validRequest.setStopAddresses(List.of("Stop1")); // mismatch

        assertThrows(IllegalArgumentException.class,
                () -> rideService.orderRide(validRequest, "user@gmail.com"));
    }

    @Test
    @DisplayName("Favorite route ID loads existing route from repository")
    void orderRide_withFavoriteRoute_shouldLoadRoute() throws Exception {
        validRequest.setFavoriteRouteId(5L);
        Route favoriteRoute = new Route();
        favoriteRoute.setId(5L);
        favoriteRoute.setDistanceKm(10.0);
        favoriteRoute.setEstimatedTimeMin(20.0);
        Location start = new Location(45.0, 19.0, "Start");
        Location end = new Location(45.1, 19.1, "End");
        favoriteRoute.setStartLocation(start);
        favoriteRoute.setEndLocation(end);
        favoriteRoute.setPolylinePoints(new ArrayList<>());

        when(routeRepository.findById(5L)).thenReturn(Optional.of(favoriteRoute));
        when(registeredUserRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(mainUser));
        when(driverRepository.findAll()).thenReturn(List.of(availableDriver));
        when(driverRepository.findById(availableDriver.getId())).thenReturn(Optional.of(availableDriver));
        when(rideRepository.findScheduledRidesForDriverInNextHour(anyLong(), any(), any())).thenReturn(List.of());
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new RideEstimateResponseDTO(1, 5));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        OrderRideResponseDTO response = rideService.orderRide(validRequest, "user@gmail.com");

        assertNotNull(response);
        verify(routeRepository, times(1)).findById(5L);
        verify(routeRepository, never()).save(any(Route.class));
    }

    @Test
    @DisplayName("Route with stop locations is created successfully")
    void orderRide_withStopLocations_shouldCreateRoute() throws Exception {
        validRequest.setStopLatitudes(List.of(45.05));
        validRequest.setStopLongitudes(List.of(19.05));
        validRequest.setStopAddresses(List.of("Stop1"));

        when(registeredUserRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(mainUser));
        when(driverRepository.findAll()).thenReturn(List.of(availableDriver));
        when(driverRepository.findById(availableDriver.getId())).thenReturn(Optional.of(availableDriver));
        when(rideRepository.findScheduledRidesForDriverInNextHour(anyLong(), any(), any())).thenReturn(List.of());
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new RideEstimateResponseDTO(1, 5));
        when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> {
            Route r = invocation.getArgument(0);
            r.setId(50L);
            return r;
        });
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        OrderRideResponseDTO response = rideService.orderRide(validRequest, "user@gmail.com");

        assertNotNull(response);
        assertEquals(List.of("Stop1"), response.getStopAddresses());
        verify(routeRepository, times(1)).save(any(Route.class));
    }

    // ==================== DRIVER STATUS ====================

    @Test
    @DisplayName("Immediate ride sets driver status to INACTIVE")
    void orderRide_immediateRide_shouldSetDriverInactive() throws Exception {
        validRequest.setScheduledTime(null); // immediate

        when(registeredUserRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(mainUser));
        when(driverRepository.findAll()).thenReturn(List.of(availableDriver));
        when(driverRepository.findById(availableDriver.getId())).thenReturn(Optional.of(availableDriver));
        when(rideRepository.findScheduledRidesForDriverInNextHour(anyLong(), any(), any())).thenReturn(List.of());
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new RideEstimateResponseDTO(1, 5));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        rideService.orderRide(validRequest, "user@gmail.com");

        assertEquals(DriverStatus.INACTIVE, availableDriver.getStatus());
        verify(driverRepository, times(1)).save(availableDriver);
    }

    @Test
    @DisplayName("Scheduled ride keeps driver status ACTIVE")
    void orderRide_scheduledRide_shouldKeepDriverActive() throws Exception {
        validRequest.setScheduledTime(LocalDateTime.now().plusHours(2));
        availableDriver.setStatus(DriverStatus.ACTIVE);

        when(registeredUserRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(mainUser));
        when(driverRepository.findAll()).thenReturn(List.of(availableDriver));
        when(driverRepository.findById(availableDriver.getId())).thenReturn(Optional.of(availableDriver));
        when(rideRepository.findScheduledRidesForDriverInNextHour(anyLong(), any(), any())).thenReturn(List.of());
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new RideEstimateResponseDTO(1, 5));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        rideService.orderRide(validRequest, "user@gmail.com");

        assertEquals(DriverStatus.ACTIVE, availableDriver.getStatus());
        verify(driverRepository, times(1)).save(availableDriver);
    }

    // ==================== LINKED PASSENGERS ====================

    @Test
    @DisplayName("Valid linked passengers are added successfully")
    void orderRide_validLinkedPassengers_shouldAddAll() throws Exception {
        RegisteredUser linkedUser1 = new RegisteredUser();
        linkedUser1.setId(2L);
        linkedUser1.setEmail("linked1@gmail.com");

        RegisteredUser linkedUser2 = new RegisteredUser();
        linkedUser2.setId(3L);
        linkedUser2.setEmail("linked2@gmail.com");

        validRequest.setLinkedPassengers(List.of("linked1@gmail.com", "linked2@gmail.com"));

        when(registeredUserRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(mainUser));
        when(registeredUserRepository.findByEmail("linked1@gmail.com")).thenReturn(Optional.of(linkedUser1));
        when(registeredUserRepository.findByEmail("linked2@gmail.com")).thenReturn(Optional.of(linkedUser2));
        when(driverRepository.findAll()).thenReturn(List.of(availableDriver));
        when(driverRepository.findById(availableDriver.getId())).thenReturn(Optional.of(availableDriver));
        when(rideRepository.findScheduledRidesForDriverInNextHour(anyLong(), any(), any())).thenReturn(List.of());
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new RideEstimateResponseDTO(1, 5));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        OrderRideResponseDTO response = rideService.orderRide(validRequest, "user@gmail.com");

        assertNotNull(response);
        assertEquals(List.of("linked1@gmail.com", "linked2@gmail.com"), response.getLinkedPassengers());
        verify(registeredUserRepository, times(1)).findByEmail("linked1@gmail.com");
        verify(registeredUserRepository, times(1)).findByEmail("linked2@gmail.com");
    }

    // ==================== NOTIFICATIONS ====================

    @Test
    @DisplayName("No driver available sends NO_DRIVERS_AVAILABLE notification")
    void orderRide_noActiveDrivers_shouldNotifyUser() {
        when(registeredUserRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(mainUser));
        when(driverRepository.findAll()).thenReturn(List.of());

        OrderRideResponseDTO response = rideService.orderRide(validRequest, "user@gmail.com");

        assertNull(response.getDriverId());
        assertEquals(RideStatus.CANCELLED, response.getStatus());
        verify(notificationService, times(1)).createNoDriversAvailableNotification(mainUser);
    }

    @Test
    @DisplayName("Successful ride creation sends notifications to driver and passenger")
    void orderRide_success_shouldSendNotifications() throws Exception {
        when(registeredUserRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(mainUser));
        when(driverRepository.findAll()).thenReturn(List.of(availableDriver));
        when(driverRepository.findById(availableDriver.getId())).thenReturn(Optional.of(availableDriver));
        when(rideRepository.findScheduledRidesForDriverInNextHour(anyLong(), any(), any())).thenReturn(List.of());
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new RideEstimateResponseDTO(1, 5));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        OrderRideResponseDTO response = rideService.orderRide(validRequest, "user@gmail.com");

        assertNotNull(response.getDriverId());
        verify(notificationService, times(1)).createRideAssignedNotification(eq(availableDriver), any(Ride.class));
        verify(notificationService, times(1)).createRideRequestAcceptedNotification(eq(mainUser), any(Ride.class));
    }

    @Test
    @DisplayName("Linked passenger receives notification when added to ride")
    void orderRide_linkedPassenger_shouldReceiveNotification() throws Exception {
        RegisteredUser linkedUser = new RegisteredUser();
        linkedUser.setId(2L);
        linkedUser.setEmail("linked@gmail.com");

        validRequest.setLinkedPassengers(List.of("linked@gmail.com"));

        when(registeredUserRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(mainUser));
        when(registeredUserRepository.findByEmail("linked@gmail.com")).thenReturn(Optional.of(linkedUser));
        when(driverRepository.findAll()).thenReturn(List.of(availableDriver));
        when(driverRepository.findById(availableDriver.getId())).thenReturn(Optional.of(availableDriver));
        when(rideRepository.findScheduledRidesForDriverInNextHour(anyLong(), any(), any())).thenReturn(List.of());
        when(routingService.getRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new RideEstimateResponseDTO(1, 5));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride r = invocation.getArgument(0);
            r.setId(100L);
            return r;
        });

        rideService.orderRide(validRequest, "user@gmail.com");

        verify(notificationService, times(1)).createAndSendPassengerAddedNotification(eq(linkedUser), any(Ride.class));
    }
}
