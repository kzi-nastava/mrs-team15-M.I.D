package rs.ac.uns.ftn.asd.ridenow.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.UpcomingRideDTO;
import rs.ac.uns.ftn.asd.ridenow.model.*;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.PassengerRole;
import rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;
import rs.ac.uns.ftn.asd.ridenow.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DriverService - findScheduledRides() Tests")
public class FindScheduledRidesServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private RideRepository rideRepository;

    @InjectMocks
    private DriverService driverService;

    private Driver driver;
    private Vehicle vehicle;
    private RegisteredUser user1, user2;
    private Passenger passenger1, passenger2;
    private Route route1, route2;
    private Ride scheduledRide1, scheduledRide2;
    private Location startLocation1, endLocation1, startLocation2, endLocation2;

    @BeforeEach
    void setUp() {
        // Setup Driver
        driver = new Driver();
        driver.setId(1L);
        driver.setFirstName("John");
        driver.setLastName("Doe");
        driver.setEmail("john.doe@example.com");
        driver.setAvailable(true);
        driver.setStatus(DriverStatus.ACTIVE);

        // Setup Vehicle
        vehicle = new Vehicle();
        vehicle.setLicencePlate("NS001AA");
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setDriver(driver);
        driver.setVehicle(vehicle);

        // Setup Users
        user1 = new RegisteredUser();
        user1.setId(10L);
        user1.setFirstName("Alice");
        user1.setLastName("Smith");
        user1.setEmail("alice.smith@example.com");

        user2 = new RegisteredUser();
        user2.setId(11L);
        user2.setFirstName("Bob");
        user2.setLastName("Johnson");
        user2.setEmail("bob.johnson@example.com");

        // Setup Passengers
        passenger1 = new Passenger();
        passenger1.setId(20L);
        passenger1.setUser(user1);
        passenger1.setRole(PassengerRole.CREATOR);

        passenger2 = new Passenger();
        passenger2.setId(21L);
        passenger2.setUser(user2);
        passenger2.setRole(PassengerRole.PASSENGER);

        // Setup Locations for first ride
        startLocation1 = new Location();
        startLocation1.setLatitude(45.2396);
        startLocation1.setLongitude(19.8227);
        startLocation1.setAddress("123, Bulevar oslobođenja, Centar, MZ Centar, Novi Sad, City of Novi Sad, South Backa Administrative District, Vojvodina, 21102, Serbia");

        endLocation1 = new Location();
        endLocation1.setLatitude(45.2671);
        endLocation1.setLongitude(19.8335);
        endLocation1.setAddress("456, Trg Dositeja Obradovića, Stari grad, MZ Stari grad, Novi Sad, City of Novi Sad, South Backa Administrative District, Vojvodina, 21101, Serbia");

        // Setup Route for first ride
        route1 = new Route();
        route1.setId(1L);
        route1.setStartLocation(startLocation1);
        route1.setEndLocation(endLocation1);
        route1.setDistanceKm(5.2);
        route1.setEstimatedTimeMin(15.0);

        // Setup Locations for second ride
        startLocation2 = new Location();
        startLocation2.setLatitude(45.2542);
        startLocation2.setLongitude(19.8451);
        startLocation2.setAddress("789, Futoška, Telep, MZ Telep, Novi Sad, City of Novi Sad, South Backa Administrative District, Vojvodina, 21000, Serbia");

        endLocation2 = new Location();
        endLocation2.setLatitude(45.2789);
        endLocation2.setLongitude(19.8523);
        endLocation2.setAddress("321, Maksima Gorkog, Detelinara, MZ Detelinara, Novi Sad, City of Novi Sad, South Backa Administrative District, Vojvodina, 21137, Serbia");

        // Setup Route for second ride
        route2 = new Route();
        route2.setId(2L);
        route2.setStartLocation(startLocation2);
        route2.setEndLocation(endLocation2);
        route2.setDistanceKm(3.8);
        route2.setEstimatedTimeMin(12.0);

        // Setup first scheduled ride
        scheduledRide1 = new Ride();
        scheduledRide1.setId(100L);
        scheduledRide1.setDriver(driver);
        scheduledRide1.setRoute(route1);
        scheduledRide1.setStatus(RideStatus.REQUESTED);
        scheduledRide1.setScheduledTime(LocalDateTime.of(2026, 2, 15, 14, 30));
        scheduledRide1.setPrice(500.0);
        scheduledRide1.setPassengers(List.of(passenger1));

        // Setup second scheduled ride
        scheduledRide2 = new Ride();
        scheduledRide2.setId(101L);
        scheduledRide2.setDriver(driver);
        scheduledRide2.setRoute(route2);
        scheduledRide2.setStatus(RideStatus.REQUESTED);
        scheduledRide2.setScheduledTime(LocalDateTime.of(2026, 2, 15, 16, 45));
        scheduledRide2.setPrice(350.0);
        scheduledRide2.setPassengers(List.of(passenger1, passenger2));
    }

    @Test
    @DisplayName("Should return properly formatted upcoming rides for existing driver")
    void findScheduledRides_ExistingDriverWithRides_ShouldReturnFormattedRides() {
        // Arrange
        Long driverId = 1L;
        List<Ride> scheduledRides = List.of(scheduledRide1, scheduledRide2);

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(rideRepository.findScheduledRidesByDriver(driver)).thenReturn(scheduledRides);

        // Act
        List<UpcomingRideDTO> result = driverService.findScheduledRides(driverId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify first ride
        UpcomingRideDTO firstRide = result.get(0);
        assertEquals(100L, firstRide.getId());
        assertEquals("15/02/2026 14:30", firstRide.getStartTime());
        assertEquals("Alice Smith", firstRide.getPassengers());
        assertEquals("123 Bulevar oslobođenja, Novi Sad → 456 Trg Dositeja Obradovića, Novi Sad", firstRide.getRoute());
        assertTrue(firstRide.isCanCancel());

        // Verify second ride
        UpcomingRideDTO secondRide = result.get(1);
        assertEquals(101L, secondRide.getId());
        assertEquals("15/02/2026 16:45", secondRide.getStartTime());
        assertEquals("Alice Smith, Bob Johnson", secondRide.getPassengers());
        assertEquals("789 Futoška, Novi Sad → 321 Maksima Gorkog, Novi Sad", secondRide.getRoute());
        assertTrue(secondRide.isCanCancel());

        // Verify repository calls
        verify(driverRepository).findById(driverId);
        verify(rideRepository).findScheduledRidesByDriver(driver);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when driver does not exist")
    void findScheduledRides_DriverNotFound_ShouldThrowEntityNotFoundException() {
        // Arrange
        Long driverId = 999L;

        when(driverRepository.findById(driverId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> driverService.findScheduledRides(driverId));

        assertEquals("Driver with id " + driverId + " not found", exception.getMessage());

        // Verify repository calls
        verify(driverRepository).findById(driverId);
        verify(rideRepository, never()).findScheduledRidesByDriver(any(Driver.class));
    }

    @Test
    @DisplayName("Should return empty list when driver has no scheduled rides")
    void findScheduledRides_NoScheduledRides_ShouldReturnEmptyList() {
        // Arrange
        Long driverId = 1L;

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(rideRepository.findScheduledRidesByDriver(driver)).thenReturn(new ArrayList<>());

        // Act
        List<UpcomingRideDTO> result = driverService.findScheduledRides(driverId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verify repository calls
        verify(driverRepository).findById(driverId);
        verify(rideRepository).findScheduledRidesByDriver(driver);
    }

    @Test
    @DisplayName("Should handle ride with no passengers")
    void findScheduledRides_RideWithNoPassengers_ShouldHandleEmptyPassengerList() {
        // Arrange
        Long driverId = 1L;
        Ride rideWithoutPassengers = new Ride();
        rideWithoutPassengers.setId(102L);
        rideWithoutPassengers.setDriver(driver);
        rideWithoutPassengers.setRoute(route1);
        rideWithoutPassengers.setStatus(RideStatus.REQUESTED);
        rideWithoutPassengers.setScheduledTime(LocalDateTime.of(2026, 2, 16, 10, 0));
        rideWithoutPassengers.setPassengers(new ArrayList<>());

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(rideRepository.findScheduledRidesByDriver(driver)).thenReturn(List.of(rideWithoutPassengers));

        // Act
        List<UpcomingRideDTO> result = driverService.findScheduledRides(driverId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        UpcomingRideDTO ride = result.get(0);
        assertEquals("", ride.getPassengers());
        assertEquals("16/02/2026 10:00", ride.getStartTime());
    }

    @Test
    @DisplayName("Should handle ride with single passenger correctly")
    void findScheduledRides_RideWithSinglePassenger_ShouldFormatCorrectly() {
        // Arrange
        Long driverId = 1L;
        Ride singlePassengerRide = new Ride();
        singlePassengerRide.setId(103L);
        singlePassengerRide.setDriver(driver);
        singlePassengerRide.setRoute(route1);
        singlePassengerRide.setStatus(RideStatus.REQUESTED);
        singlePassengerRide.setScheduledTime(LocalDateTime.of(2026, 2, 17, 9, 15));
        singlePassengerRide.setPassengers(List.of(passenger1));

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(rideRepository.findScheduledRidesByDriver(driver)).thenReturn(List.of(singlePassengerRide));

        // Act
        List<UpcomingRideDTO> result = driverService.findScheduledRides(driverId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        UpcomingRideDTO ride = result.get(0);
        assertEquals("Alice Smith", ride.getPassengers());
        assertFalse(ride.getPassengers().contains(","));
    }

    @Test
    @DisplayName("Should format multiple passengers with comma separation")
    void findScheduledRides_RideWithMultiplePassengers_ShouldFormatWithCommas() {
        // Arrange
        Long driverId = 1L;
        RegisteredUser user3 = new RegisteredUser();
        user3.setFirstName("Charlie");
        user3.setLastName("Brown");

        Passenger passenger3 = new Passenger();
        passenger3.setUser(user3);
        passenger3.setRole(PassengerRole.PASSENGER);

        Ride multiPassengerRide = new Ride();
        multiPassengerRide.setId(104L);
        multiPassengerRide.setDriver(driver);
        multiPassengerRide.setRoute(route1);
        multiPassengerRide.setStatus(RideStatus.REQUESTED);
        multiPassengerRide.setScheduledTime(LocalDateTime.of(2026, 2, 18, 11, 0));
        multiPassengerRide.setPassengers(List.of(passenger1, passenger2, passenger3));

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(rideRepository.findScheduledRidesByDriver(driver)).thenReturn(List.of(multiPassengerRide));

        // Act
        List<UpcomingRideDTO> result = driverService.findScheduledRides(driverId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        UpcomingRideDTO ride = result.get(0);
        assertEquals("Alice Smith, Bob Johnson, Charlie Brown", ride.getPassengers());
        assertEquals(2, ride.getPassengers().split(",").length - 1); // Should have 2 commas for 3 passengers
    }

    @Test
    @DisplayName("Should correctly format addresses using address utility")
    void findScheduledRides_ShouldFormatAddressesCorrectly() {
        // Arrange
        Long driverId = 1L;

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(rideRepository.findScheduledRidesByDriver(driver)).thenReturn(List.of(scheduledRide1));

        // Act
        List<UpcomingRideDTO> result = driverService.findScheduledRides(driverId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        UpcomingRideDTO ride = result.get(0);
        // The formatAddress utility should shorten the long addresses
        assertEquals("123 Bulevar oslobođenja, Novi Sad → 456 Trg Dositeja Obradovića, Novi Sad", ride.getRoute());
        assertTrue(ride.getRoute().contains("→")); // Arrow separator should be present
    }

    @Test
    @DisplayName("Should set canCancel to true for all rides")
    void findScheduledRides_ShouldSetCanCancelToTrue() {
        // Arrange
        Long driverId = 1L;

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(rideRepository.findScheduledRidesByDriver(driver)).thenReturn(List.of(scheduledRide1, scheduledRide2));

        // Act
        List<UpcomingRideDTO> result = driverService.findScheduledRides(driverId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        result.forEach(ride -> assertTrue(ride.isCanCancel()));
    }

    @Test
    @DisplayName("Should handle rides with different scheduled times correctly")
    void findScheduledRides_DifferentScheduledTimes_ShouldFormatCorrectly() {
        // Arrange
        Long driverId = 1L;

        // Create rides with various time formats
        Ride earlyMorningRide = new Ride();
        earlyMorningRide.setId(105L);
        earlyMorningRide.setDriver(driver);
        earlyMorningRide.setRoute(route1);
        earlyMorningRide.setStatus(RideStatus.REQUESTED);
        earlyMorningRide.setScheduledTime(LocalDateTime.of(2026, 2, 20, 6, 5));
        earlyMorningRide.setPassengers(List.of(passenger1));

        Ride lateNightRide = new Ride();
        lateNightRide.setId(106L);
        lateNightRide.setDriver(driver);
        lateNightRide.setRoute(route2);
        lateNightRide.setStatus(RideStatus.REQUESTED);
        lateNightRide.setScheduledTime(LocalDateTime.of(2026, 12, 31, 23, 59));
        lateNightRide.setPassengers(List.of(passenger2));

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(rideRepository.findScheduledRidesByDriver(driver)).thenReturn(List.of(earlyMorningRide, lateNightRide));

        // Act
        List<UpcomingRideDTO> result = driverService.findScheduledRides(driverId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("20/02/2026 06:05", result.get(0).getStartTime());
        assertEquals("31/12/2026 23:59", result.get(1).getStartTime());
    }

    @Test
    @DisplayName("Should handle null route addresses gracefully")
    void findScheduledRides_NullRouteAddresses_ShouldHandleGracefully() {
        // Arrange
        Long driverId = 1L;

        // Create ride with null addresses
        Location nullStartLocation = new Location();
        nullStartLocation.setLatitude(45.0);
        nullStartLocation.setLongitude(19.0);
        nullStartLocation.setAddress(null);

        Location nullEndLocation = new Location();
        nullEndLocation.setLatitude(45.1);
        nullEndLocation.setLongitude(19.1);
        nullEndLocation.setAddress(null);

        Route nullAddressRoute = new Route();
        nullAddressRoute.setStartLocation(nullStartLocation);
        nullAddressRoute.setEndLocation(nullEndLocation);

        Ride rideWithNullAddresses = new Ride();
        rideWithNullAddresses.setId(107L);
        rideWithNullAddresses.setDriver(driver);
        rideWithNullAddresses.setRoute(nullAddressRoute);
        rideWithNullAddresses.setStatus(RideStatus.REQUESTED);
        rideWithNullAddresses.setScheduledTime(LocalDateTime.of(2026, 2, 25, 12, 0));
        rideWithNullAddresses.setPassengers(List.of(passenger1));

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(rideRepository.findScheduledRidesByDriver(driver)).thenReturn(List.of(rideWithNullAddresses));

        // Act & Assert
        // This should not throw an exception, but handle null addresses gracefully
        assertDoesNotThrow(() -> {
            List<UpcomingRideDTO> result = driverService.findScheduledRides(driverId);
            assertNotNull(result);
            assertEquals(1, result.size());
        });
    }

    @Test
    @DisplayName("Should preserve ride order from repository")
    void findScheduledRides_ShouldPreserveRideOrder() {
        // Arrange
        Long driverId = 1L;
        List<Ride> ridesInOrder = List.of(scheduledRide2, scheduledRide1); // Note: reversed order

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(rideRepository.findScheduledRidesByDriver(driver)).thenReturn(ridesInOrder);

        // Act
        List<UpcomingRideDTO> result = driverService.findScheduledRides(driverId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Should maintain the order from repository
        assertEquals(101L, result.get(0).getId()); // scheduledRide2 should be first
        assertEquals(100L, result.get(1).getId()); // scheduledRide1 should be second
    }
}
