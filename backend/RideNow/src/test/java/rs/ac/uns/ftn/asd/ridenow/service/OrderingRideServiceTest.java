package rs.ac.uns.ftn.asd.ridenow.service;

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
import rs.ac.uns.ftn.asd.ridenow.model.enums.PassengerRole;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;
import rs.ac.uns.ftn.asd.ridenow.repository.*;

import java.time.LocalDateTime;
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
        mainUser.setEmail("user@example.com");
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
        when(registeredUserRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mainUser));
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

        OrderRideResponseDTO response = rideService.orderRide(validRequest, "user@example.com");

        assertNotNull(response);
        assertEquals(availableDriver.getId(), response.getDriverId());
        assertEquals("user@example.com", response.getMainPassengerEmail());
        assertEquals(validRequest.getStartAddress(), response.getStartAddress());
        assertEquals(validRequest.getEndAddress(), response.getEndAddress());

        verify(rideRepository, times(1)).save(any(Ride.class));
        verify(driverRepository, times(1)).save(availableDriver);
    }

    @Test
    @DisplayName("No available drivers returns response with null driverId")
    void orderRide_noDrivers_shouldReturnNullDriver() {
        when(registeredUserRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mainUser));
        when(driverRepository.findAll()).thenReturn(List.of());
        when(rideRepository.save(any(Ride.class)))
                .thenAnswer(invocation -> {
                    Ride r = invocation.getArgument(0);
                    r.setId(100L);
                    return r;
                });

        OrderRideResponseDTO response = rideService.orderRide(validRequest, "user@example.com");

        assertNotNull(response);
        assertNull(response.getDriverId());
    }

    @Test
    @DisplayName("Invalid stop lists throw IllegalArgumentException")
    void orderRide_invalidStops_shouldThrow() {
        OrderRideRequestDTO dto = new OrderRideRequestDTO();
        dto.setVehicleType("STANDARD");
        dto.setStartAddress("A");
        dto.setEndAddress("B");
        dto.setStartLatitude(1.0);
        dto.setStartLongitude(1.0);
        dto.setEndLatitude(2.0);
        dto.setEndLongitude(2.0);
        dto.setDistanceKm(1.0);
        dto.setEstimatedTimeMinutes(1);
        dto.setPriceEstimate(1.0);
        dto.setRouteLattitudes(List.of(1.0, 2.0));
        dto.setRouteLongitudes(List.of(1.0, 2.0));

        dto.setStopLatitudes(null);
        dto.setStopLongitudes(List.of(1.0));

        assertThrows(IllegalArgumentException.class, () -> rideService.orderRide(dto, "user@example.com"));
    }

    @Test
    @DisplayName("Invalid vehicle type throws IllegalArgumentException")
    void orderRide_invalidVehicleType_shouldThrow() {
        OrderRideRequestDTO dto = new OrderRideRequestDTO();
        dto.setVehicleType("INVALID");
        dto.setStartAddress("A"); dto.setEndAddress("B");
        dto.setStartLatitude(1.0); dto.setStartLongitude(1.0);
        dto.setEndLatitude(2.0); dto.setEndLongitude(2.0);
        dto.setDistanceKm(1.0); dto.setEstimatedTimeMinutes(1); dto.setPriceEstimate(1.0);
        dto.setRouteLattitudes(List.of(1.0)); dto.setRouteLongitudes(List.of(1.0));

        assertThrows(IllegalArgumentException.class, () -> rideService.orderRide(dto, "user@example.com"));
    }

    @Test
    @DisplayName("Linked passengers with invalid email are skipped")
    void orderRide_linkedPassengers_invalidEmail_shouldSkip() throws Exception {
        validRequest.setLinkedPassengers(List.of("missing@example.com"));

        when(registeredUserRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mainUser));
        when(registeredUserRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
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

        OrderRideResponseDTO response = rideService.orderRide(validRequest, "user@example.com");

        assertNotNull(response);
        assertEquals(availableDriver.getId(), response.getDriverId());
    }
}
