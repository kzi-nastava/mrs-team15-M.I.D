package rs.ac.uns.ftn.asd.ridenow.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import rs.ac.uns.ftn.asd.ridenow.RideNowApplication;
import rs.ac.uns.ftn.asd.ridenow.model.*;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@ContextConfiguration(classes = RideNowApplication.class)
@DisplayName("RideRepository - Finish Ride Functionality Tests")
public class FinishRideRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private RideRepository rideRepository;

    private Driver driver;
    private Vehicle vehicle;
    private Route route;
    private Ride finishedRide;
    private Ride requestedRide;
    private Ride scheduledRide1;
    private Ride scheduledRide2;
    private Location startLocation;
    private Location endLocation;

    @BeforeEach
    void setUp() {
        // Create test driver first (without vehicle initially)
        driver = new Driver();
        driver.setFirstName("John");
        driver.setLastName("Driver");
        driver.setEmail("john.driver@example.com");
        driver.setPassword("password123");
        driver.setPhoneNumber("+381641234567");
        driver.setAddress("123 Driver Street");
        driver.setActive(true);
        driver.setBlocked(false);
        driver.setAvailable(true);
        driver.setStatus(DriverStatus.ACTIVE);
        driver.setRating(4.5);
        driver.setWorkingHoursLast24(8.0);
        driver.setRole(rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles.DRIVER);
        driver.setJwtTokenValid(false);

        // Persist driver first
        testEntityManager.persist(driver);
        testEntityManager.flush();

        // Create test vehicle with driver reference and all required fields
        vehicle = new Vehicle();
        vehicle.setLicencePlate("NS123AB");
        vehicle.setModel("Test Model");
        vehicle.setType(VehicleType.STANDARD);
        vehicle.setAvailable(true);
        vehicle.setChildFriendly(false);
        vehicle.setPetFriendly(false);
        vehicle.setRating(4.2);
        vehicle.setLat(45.2396);
        vehicle.setLon(19.8227);
        vehicle.setSeatCount(4);
        vehicle.setDriver(driver);

        // Set bidirectional relationship
        driver.setVehicle(vehicle);

        // Persist vehicle
        testEntityManager.persist(vehicle);
        testEntityManager.flush();

        // Create test locations
        startLocation = new Location();
        startLocation.setAddress("Start Address");
        startLocation.setLatitude(45.2396);
        startLocation.setLongitude(19.8227);

        endLocation = new Location();
        endLocation.setAddress("End Address");
        endLocation.setLatitude(45.2500);
        endLocation.setLongitude(19.8300);

        // Create test route
        route = new Route();
        route.setStartLocation(startLocation);
        route.setEndLocation(endLocation);
        route.setDistanceKm(5.0);
        route.setEstimatedTimeMin(15.0);

        // Create test rides
        LocalDateTime now = LocalDateTime.now();

        finishedRide = new Ride();
        finishedRide.setStatus(RideStatus.FINISHED);
        finishedRide.setScheduledTime(now.minusHours(2));
        finishedRide.setStartTime(now.minusHours(2));
        finishedRide.setEndTime(now.minusHours(1));
        finishedRide.setPrice(500.0);
        finishedRide.setDistanceKm(5.0);
        finishedRide.setRoute(route);
        finishedRide.setDriver(driver);

        requestedRide = new Ride();
        requestedRide.setStatus(RideStatus.REQUESTED);
        requestedRide.setScheduledTime(now.plusMinutes(10));
        requestedRide.setPrice(400.0);
        requestedRide.setDistanceKm(4.0);
        requestedRide.setRoute(route);
        requestedRide.setDriver(driver);

        scheduledRide1 = new Ride();
        scheduledRide1.setStatus(RideStatus.REQUESTED);
        scheduledRide1.setScheduledTime(now.plusMinutes(30));
        scheduledRide1.setPrice(600.0);
        scheduledRide1.setDistanceKm(6.0);
        scheduledRide1.setRoute(route);
        scheduledRide1.setDriver(driver);

        scheduledRide2 = new Ride();
        scheduledRide2.setStatus(RideStatus.REQUESTED);
        scheduledRide2.setScheduledTime(now.plusHours(2));
        scheduledRide2.setPrice(700.0);
        scheduledRide2.setDistanceKm(7.0);
        scheduledRide2.setRoute(route);
        scheduledRide2.setDriver(driver);

        // Persist remaining entities
        testEntityManager.persist(route);
        testEntityManager.persist(finishedRide);
        testEntityManager.persist(requestedRide);
        testEntityManager.persist(scheduledRide1);
        testEntityManager.persist(scheduledRide2);
        testEntityManager.flush();
    }

    @Test
    @DisplayName("Should find scheduled rides for driver in next hour")
    void findScheduledRidesForDriverInNextHour_ShouldReturnRidesInTimeRange() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextHour = now.plusHours(1);

        List<Ride> result = rideRepository.findScheduledRidesForDriverInNextHour(
                driver.getId(), now, nextHour);

        assertNotNull(result);
        assertEquals(2, result.size()); // requestedRide (10 min) and scheduledRide1 (30 min) are both within 1 hour
    }

    @Test
    @DisplayName("Should return empty list when no scheduled rides in next hour")
    void findScheduledRidesForDriverInNextHour_ShouldReturnEmptyWhenNoRidesInTimeRange() {
        LocalDateTime futureTime = LocalDateTime.now().plusHours(3);
        LocalDateTime futureTimeEnd = futureTime.plusHours(1);

        List<Ride> result = rideRepository.findScheduledRidesForDriverInNextHour(
                driver.getId(), futureTime, futureTimeEnd);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return rides ordered by scheduled time ascending")
    void findScheduledRidesForDriverInNextHour_ShouldReturnRidesOrderedByScheduledTime() {
        LocalDateTime now = LocalDateTime.now().minusMinutes(5);
        LocalDateTime nextHour = now.plusHours(1);

        List<Ride> result = rideRepository.findScheduledRidesForDriverInNextHour(
                driver.getId(), now, nextHour);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getScheduledTime().isBefore(result.get(1).getScheduledTime()));
    }

    @Test
    @DisplayName("Should only return REQUESTED rides, not FINISHED rides")
    void findScheduledRidesForDriverInNextHour_ShouldOnlyReturnRequestedRides() {
        // Create a FINISHED ride within the time range
        LocalDateTime now = LocalDateTime.now();
        Ride finishedRideInRange = new Ride();
        finishedRideInRange.setStatus(RideStatus.FINISHED);
        finishedRideInRange.setScheduledTime(now.plusMinutes(15));
        finishedRideInRange.setStartTime(now.plusMinutes(15));
        finishedRideInRange.setEndTime(now.plusMinutes(30));
        finishedRideInRange.setPrice(300.0);
        finishedRideInRange.setDistanceKm(3.0);
        finishedRideInRange.setRoute(route);
        finishedRideInRange.setDriver(driver);

        testEntityManager.persist(finishedRideInRange);
        testEntityManager.flush();

        LocalDateTime nextHour = now.plusHours(1);

        List<Ride> result = rideRepository.findScheduledRidesForDriverInNextHour(
                driver.getId(), now, nextHour);

        assertNotNull(result);
        // Should find the 2 REQUESTED rides (requestedRide and scheduledRide1), not the FINISHED one
        assertEquals(2, result.size());
        result.forEach(ride -> assertEquals(RideStatus.REQUESTED, ride.getStatus()));
    }

    @Test
    @DisplayName("Should return empty list for non-existent driver")
    void findScheduledRidesForDriverInNextHour_ShouldReturnEmptyForNonExistentDriver() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextHour = now.plusHours(1);
        Long nonExistentDriverId = 99999L;

        List<Ride> result = rideRepository.findScheduledRidesForDriverInNextHour(
                nonExistentDriverId, now, nextHour);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle edge case when time range boundaries are exact")
    void findScheduledRidesForDriverInNextHour_ShouldHandleExactTimeBoundaries() {
        // Use a time that doesn't conflict with existing test rides (45 minutes instead of 30)
        LocalDateTime rideTime = LocalDateTime.now().plusMinutes(45);

        // Create a ride exactly at the boundary time
        Ride boundaryRide = new Ride();
        boundaryRide.setStatus(RideStatus.REQUESTED);
        boundaryRide.setScheduledTime(rideTime);
        boundaryRide.setPrice(500.0);
        boundaryRide.setDistanceKm(5.0);
        boundaryRide.setRoute(route);
        boundaryRide.setDriver(driver);

        testEntityManager.persist(boundaryRide);
        testEntityManager.flush();

        // Test with a small range that includes only this exact time
        LocalDateTime startTime = rideTime.minusSeconds(1);
        LocalDateTime endTime = rideTime.plusSeconds(1);

        List<Ride> result = rideRepository.findScheduledRidesForDriverInNextHour(
                driver.getId(), startTime, endTime);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(boundaryRide.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Should find scheduled rides by driver")
    void findScheduledRidesByDriver_ShouldReturnAllRequestedRidesForDriver() {
        List<Ride> result = rideRepository.findScheduledRidesByDriver(driver);

        assertNotNull(result);
        assertEquals(3, result.size());

        // All returned rides should be REQUESTED status
        result.forEach(ride -> assertEquals(RideStatus.REQUESTED, ride.getStatus()));

        // All returned rides should belong to the driver
        result.forEach(ride -> assertEquals(driver.getId(), ride.getDriver().getId()));
    }

    @Test
    @DisplayName("Should return empty list when driver has no scheduled rides")
    void findScheduledRidesByDriver_ShouldReturnEmptyWhenNoScheduledRides() {
        // Create a new driver with no rides
        Driver newDriver = new Driver();
        newDriver.setFirstName("Jane");
        newDriver.setLastName("Smith");
        newDriver.setEmail("jane.smith@example.com");
        newDriver.setPassword("password123");
        newDriver.setPhoneNumber("+381651234567");
        newDriver.setAddress("456 Another Street");
        newDriver.setActive(true);
        newDriver.setBlocked(false);
        newDriver.setAvailable(true);
        newDriver.setStatus(DriverStatus.ACTIVE);
        newDriver.setRating(4.0);
        newDriver.setWorkingHoursLast24(6.0);
        newDriver.setRole(rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles.DRIVER);
        newDriver.setJwtTokenValid(false);

        testEntityManager.persist(newDriver);
        testEntityManager.flush();

        List<Ride> result = rideRepository.findScheduledRidesByDriver(newDriver);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should only return REQUESTED rides, not other statuses")
    void findScheduledRidesByDriver_ShouldOnlyReturnRequestedRides() {
        // Create rides with different statuses for the same driver
        Ride inProgressRide = new Ride();
        inProgressRide.setStatus(RideStatus.IN_PROGRESS);
        inProgressRide.setScheduledTime(LocalDateTime.now().minusMinutes(10));
        inProgressRide.setStartTime(LocalDateTime.now().minusMinutes(10));
        inProgressRide.setPrice(800.0);
        inProgressRide.setDistanceKm(8.0);
        inProgressRide.setRoute(route);
        inProgressRide.setDriver(driver);

        Ride cancelledRide = new Ride();
        cancelledRide.setStatus(RideStatus.CANCELLED);
        cancelledRide.setScheduledTime(LocalDateTime.now().minusHours(1));
        cancelledRide.setPrice(900.0);
        cancelledRide.setDistanceKm(9.0);
        cancelledRide.setRoute(route);
        cancelledRide.setDriver(driver);
        cancelledRide.setCancelled(true);

        testEntityManager.persist(inProgressRide);
        testEntityManager.persist(cancelledRide);
        testEntityManager.flush();

        List<Ride> result = rideRepository.findScheduledRidesByDriver(driver);

        assertNotNull(result);
        // Should still only return the 3 REQUESTED rides, not the IN_PROGRESS or CANCELLED ones
        assertEquals(3, result.size());
        result.forEach(ride -> assertEquals(RideStatus.REQUESTED, ride.getStatus()));
    }
}
