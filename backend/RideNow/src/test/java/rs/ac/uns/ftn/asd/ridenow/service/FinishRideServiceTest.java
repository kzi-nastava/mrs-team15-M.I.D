package rs.ac.uns.ftn.asd.ridenow.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.ac.uns.ftn.asd.ridenow.model.*;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;
import rs.ac.uns.ftn.asd.ridenow.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RideService - finishRide() Tests")
public class FinishRideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private RideService rideService;

    private Driver driver;
    private Vehicle vehicle;
    private Route route;
    private Ride ride;
    private Location startLocation;
    private Location endLocation;

    @BeforeEach
    void setUp() {
        // Setup Driver
        driver = new Driver();
        driver.setId(1L);
        driver.setFirstName("John");
        driver.setLastName("Doe");
        driver.setEmail("john.doe@example.com");
        driver.setAvailable(false);
        driver.setStatus(DriverStatus.ACTIVE);

        // Setup Vehicle
        vehicle = new Vehicle();
        vehicle.setLicencePlate("NS001AA");
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setDriver(driver);
        driver.setVehicle(vehicle);

        // Setup Locations
        startLocation = new Location();
        startLocation.setLatitude(45.2396);
        startLocation.setLongitude(19.8227);
        startLocation.setAddress("Bulevar oslobođenja 46, Novi Sad");

        endLocation = new Location();
        endLocation.setLatitude(45.2671);
        endLocation.setLongitude(19.8335);
        endLocation.setAddress("Trg Dositeja Obradovića 6, Novi Sad");

        // Setup Route
        route = new Route();
        route.setId(1L);
        route.setStartLocation(startLocation);
        route.setEndLocation(endLocation);
        route.setDistanceKm(5.2);
        route.setEstimatedTimeMin(15.0);

        // Setup Ride
        ride = new Ride();
        ride.setId(1L);
        ride.setDriver(driver);
        ride.setRoute(route);
        ride.setStatus(RideStatus.IN_PROGRESS);
        ride.setStartTime(LocalDateTime.now().minusMinutes(10));
        ride.setScheduledTime(LocalDateTime.now().minusMinutes(15));
        ride.setPrice(500.0);
    }

    @Test
    @DisplayName("Should successfully finish ride when no scheduled rides exist within next hour")
    void finishRide_NoScheduledRides_ShouldMarkDriverAsAvailableAndReturnFalse() {
        // Arrange
        Long rideId = 1L;
        Long driverId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextHour = now.plusHours(1);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);
        when(rideRepository.findScheduledRidesForDriverInNextHour(eq(driverId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        Boolean result = rideService.finishRide(rideId, driverId);

        // Assert
        assertFalse(result);

        // Verify ride was updated
        verify(rideRepository).save(argThat(savedRide ->
            savedRide.getStatus() == RideStatus.FINISHED &&
            savedRide.getEndTime() != null
        ));

        // Verify driver was made available
        verify(driverRepository).save(argThat(savedDriver ->
            savedDriver.getAvailable() == true &&
            savedDriver.getPendingStatus() == null
        ));

        // Verify search for scheduled rides
        verify(rideRepository).findScheduledRidesForDriverInNextHour(eq(driverId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should successfully finish ride and start next scheduled ride")
    void finishRide_HasScheduledRides_ShouldStartNextRideAndReturnTrue() {
        // Arrange
        Long rideId = 1L;
        Long driverId = 1L;

        Ride nextRide = new Ride();
        nextRide.setId(2L);
        nextRide.setDriver(driver);
        nextRide.setStatus(RideStatus.REQUESTED);
        nextRide.setScheduledTime(LocalDateTime.now().plusMinutes(30));

        List<Ride> scheduledRides = List.of(nextRide);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);
        when(rideRepository.findScheduledRidesForDriverInNextHour(eq(driverId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(scheduledRides);

        // Act
        Boolean result = rideService.finishRide(rideId, driverId);

        // Assert
        assertTrue(result);

        // Verify ride was finished
        verify(rideRepository).save(argThat(savedRide ->
            savedRide.getStatus() == RideStatus.FINISHED &&
            savedRide.getEndTime() != null
        ));

        // Verify next ride was started
        verify(rideRepository).save(argThat(savedNextRide ->
            savedNextRide.getId().equals(2L) &&
            savedNextRide.getStatus() == RideStatus.IN_PROGRESS
        ));

        // Verify driver repository was not called (driver stays busy)
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when ride does not exist")
    void finishRide_RideNotFound_ShouldThrowEntityNotFoundException() {
        // Arrange
        Long rideId = 999L;
        Long driverId = 1L;

        when(rideRepository.findById(rideId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> rideService.finishRide(rideId, driverId));

        assertEquals("Ride with id " + rideId + " not found", exception.getMessage());

        // Verify no further operations were performed
        verify(rideRepository, never()).save(any(Ride.class));
        verify(driverRepository, never()).save(any(Driver.class));
        verify(rideRepository, never()).findScheduledRidesForDriverInNextHour(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should handle driver with pending status when no scheduled rides exist")
    void finishRide_DriverWithPendingStatus_ShouldApplyPendingStatusAndClearIt() {
        // Arrange
        Long rideId = 1L;
        Long driverId = 1L;

        driver.setPendingStatus(DriverStatus.INACTIVE);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);
        when(rideRepository.findScheduledRidesForDriverInNextHour(eq(driverId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        Boolean result = rideService.finishRide(rideId, driverId);

        // Assert
        assertFalse(result);

        // Verify driver status was updated correctly
        verify(driverRepository).save(argThat(savedDriver ->
            savedDriver.getAvailable() == true &&
            savedDriver.getStatus() == DriverStatus.INACTIVE &&
            savedDriver.getPendingStatus() == null
        ));
    }

    @Test
    @DisplayName("Should handle null driver gracefully")
    void finishRide_NullDriver_ShouldFinishRideWithoutDriverUpdate() {
        // Arrange
        Long rideId = 1L;
        Long driverId = 1L;

        ride.setDriver(null);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);
        when(rideRepository.findScheduledRidesForDriverInNextHour(eq(driverId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        Boolean result = rideService.finishRide(rideId, driverId);

        // Assert
        assertFalse(result);

        // Verify ride was finished but no driver update
        verify(rideRepository).save(argThat(savedRide ->
            savedRide.getStatus() == RideStatus.FINISHED &&
            savedRide.getEndTime() != null
        ));

        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    @DisplayName("Should handle multiple scheduled rides and start the first one")
    void finishRide_MultipleScheduledRides_ShouldStartFirstRideOnly() {
        // Arrange
        Long rideId = 1L;
        Long driverId = 1L;

        Ride nextRide1 = new Ride();
        nextRide1.setId(2L);
        nextRide1.setStatus(RideStatus.REQUESTED);
        nextRide1.setScheduledTime(LocalDateTime.now().plusMinutes(30));

        Ride nextRide2 = new Ride();
        nextRide2.setId(3L);
        nextRide2.setStatus(RideStatus.REQUESTED);
        nextRide2.setScheduledTime(LocalDateTime.now().plusMinutes(45));

        List<Ride> scheduledRides = List.of(nextRide1, nextRide2);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);
        when(rideRepository.findScheduledRidesForDriverInNextHour(eq(driverId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(scheduledRides);

        // Act
        Boolean result = rideService.finishRide(rideId, driverId);

        // Assert
        assertTrue(result);

        // Verify only the first ride was started
        verify(rideRepository, times(2)).save(any(Ride.class)); // Once for finishing current ride, once for starting next
        verify(rideRepository).save(argThat(savedRide ->
            savedRide.getId().equals(2L) &&
            savedRide.getStatus() == RideStatus.IN_PROGRESS
        ));
    }

    @Test
    @DisplayName("Should handle edge case where scheduled rides list contains null elements")
    void finishRide_ScheduledRidesWithNullElements_ShouldHandleGracefully() {
        // Arrange
        Long rideId = 1L;
        Long driverId = 1L;

        // This shouldn't happen in practice, but test defensive programming
        List<Ride> scheduledRides = new ArrayList<>();
        scheduledRides.add(null);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);
        when(rideRepository.findScheduledRidesForDriverInNextHour(eq(driverId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(scheduledRides);

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> rideService.finishRide(rideId, driverId));
    }

    @Test
    @DisplayName("Should verify exact timing parameters for scheduled rides search")
    void finishRide_ShouldUseCorrectTimeParameters() {
        // Arrange
        Long rideId = 1L;
        Long driverId = 1L;

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);
        when(rideRepository.findScheduledRidesForDriverInNextHour(eq(driverId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        rideService.finishRide(rideId, driverId);

        // Assert - verify the time parameters are approximately correct (within 1 second)
        verify(rideRepository).findScheduledRidesForDriverInNextHour(
            eq(driverId),
            argThat(startTime -> {
                LocalDateTime now = LocalDateTime.now();
                return startTime.isAfter(now.minusSeconds(1)) && startTime.isBefore(now.plusSeconds(1));
            }),
            argThat(endTime -> {
                LocalDateTime expected = LocalDateTime.now().plusHours(1);
                return endTime.isAfter(expected.minusSeconds(1)) && endTime.isBefore(expected.plusSeconds(1));
            })
        );
    }

    @Test
    @DisplayName("Should maintain ride's original properties during finish")
    void finishRide_ShouldPreserveRideProperties() {
        // Arrange
        Long rideId = 1L;
        Long driverId = 1L;

        ride.setPrice(350.0);
        ride.setDistanceKm(7.5);

        when(rideRepository.findById(rideId)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);
        when(rideRepository.findScheduledRidesForDriverInNextHour(eq(driverId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        rideService.finishRide(rideId, driverId);

        // Assert
        verify(rideRepository).save(argThat(savedRide -> {
            // Verify that other properties are preserved
            return Double.valueOf(savedRide.getPrice()).equals(350.0) &&
                   Double.valueOf(savedRide.getDistanceKm()).equals(7.5) &&
                   savedRide.getDriver().equals(driver) &&
                   savedRide.getRoute().equals(route) &&
                   savedRide.getStatus() == RideStatus.FINISHED &&
                   savedRide.getEndTime() != null;
        }));
    }
}
