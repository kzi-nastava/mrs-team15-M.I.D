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
import rs.ac.uns.ftn.asd.ridenow.model.enums.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@ContextConfiguration(classes = RideNowApplication.class)
@DisplayName("RideRepository - Upcoming Rides Functionality Tests")
public class UpcomingRidesRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private RideRepository rideRepository;

    private Driver driver1;
    private Driver driver2;
    private Vehicle vehicle1;
    private Vehicle vehicle2;
    private Route route;
    private RegisteredUser passenger1;
    private RegisteredUser passenger2;
    private Ride scheduledRide1;
    private Ride scheduledRide2;
    private Ride finishedRide;
    private Ride inProgressRide;
    private Ride cancelledRide;
    private Location startLocation;
    private Location endLocation;

    @BeforeEach
    void setUp() {
        // Create test locations
        startLocation = new Location();
        startLocation.setAddress("Start Address, City, Country");
        startLocation.setLatitude(45.2396);
        startLocation.setLongitude(19.8227);

        endLocation = new Location();
        endLocation.setAddress("End Address, City, Country");
        endLocation.setLatitude(45.2500);
        endLocation.setLongitude(19.8300);

        // Create test route
        route = new Route();
        route.setStartLocation(startLocation);
        route.setEndLocation(endLocation);
        route.setDistanceKm(5.0);
        route.setEstimatedTimeMin(15.0);

        // Create test drivers first (without vehicles initially)
        driver1 = new Driver();
        driver1.setFirstName("John");
        driver1.setLastName("Driver");
        driver1.setEmail("john.driver@example.com");
        driver1.setPassword("password123");
        driver1.setPhoneNumber("+381641234567");
        driver1.setAddress("123 Driver Street");
        driver1.setActive(true);
        driver1.setBlocked(false);
        driver1.setAvailable(true);
        driver1.setStatus(DriverStatus.ACTIVE);
        driver1.setRating(4.5);
        driver1.setWorkingHoursLast24(8.0);
        driver1.setRole(UserRoles.DRIVER);
        driver1.setJwtTokenValid(false);

        driver2 = new Driver();
        driver2.setFirstName("Jane");
        driver2.setLastName("Smith");
        driver2.setEmail("jane.smith@example.com");
        driver2.setPassword("password123");
        driver2.setPhoneNumber("+381651234567");
        driver2.setAddress("456 Another Street");
        driver2.setActive(true);
        driver2.setBlocked(false);
        driver2.setAvailable(true);
        driver2.setStatus(DriverStatus.ACTIVE);
        driver2.setRating(4.7);
        driver2.setWorkingHoursLast24(6.0);
        driver2.setRole(UserRoles.DRIVER);
        driver2.setJwtTokenValid(false);

        // Create test passengers
        passenger1 = new RegisteredUser();
        passenger1.setFirstName("Alice");
        passenger1.setLastName("Passenger");
        passenger1.setEmail("alice@example.com");
        passenger1.setPassword("password123");
        passenger1.setPhoneNumber("+381611234567");
        passenger1.setAddress("789 Passenger Street");
        passenger1.setActive(true);
        passenger1.setBlocked(false);
        passenger1.setRole(UserRoles.USER);
        passenger1.setJwtTokenValid(false);

        passenger2 = new RegisteredUser();
        passenger2.setFirstName("Bob");
        passenger2.setLastName("Traveler");
        passenger2.setEmail("bob@example.com");
        passenger2.setPassword("password123");
        passenger2.setPhoneNumber("+381621234567");
        passenger2.setAddress("101 Traveler Avenue");
        passenger2.setActive(true);
        passenger2.setBlocked(false);
        passenger2.setRole(UserRoles.USER);
        passenger2.setJwtTokenValid(false);

        // Persist users first
        testEntityManager.persist(driver1);
        testEntityManager.persist(driver2);
        testEntityManager.persist(passenger1);
        testEntityManager.persist(passenger2);
        testEntityManager.flush();

        // Create test vehicles with all required fields and driver references
        vehicle1 = new Vehicle();
        vehicle1.setLicencePlate("NS123AB");
        vehicle1.setModel("Test Model 1");
        vehicle1.setType(VehicleType.STANDARD);
        vehicle1.setAvailable(true);
        vehicle1.setChildFriendly(false);
        vehicle1.setPetFriendly(false);
        vehicle1.setRating(4.2);
        vehicle1.setLat(45.2396);
        vehicle1.setLon(19.8227);
        vehicle1.setSeatCount(4);
        vehicle1.setDriver(driver1);

        vehicle2 = new Vehicle();
        vehicle2.setLicencePlate("NS456CD");
        vehicle2.setModel("Test Model 2");
        vehicle2.setType(VehicleType.LUXURY);
        vehicle2.setAvailable(true);
        vehicle2.setChildFriendly(true);
        vehicle2.setPetFriendly(true);
        vehicle2.setRating(4.8);
        vehicle2.setLat(45.2400);
        vehicle2.setLon(19.8230);
        vehicle2.setSeatCount(5);
        vehicle2.setDriver(driver2);

        // Set bidirectional relationships
        driver1.setVehicle(vehicle1);
        driver2.setVehicle(vehicle2);

        // Persist vehicles
        testEntityManager.persist(vehicle1);
        testEntityManager.persist(vehicle2);
        testEntityManager.flush();

        // Persist route
        testEntityManager.persist(route);
        testEntityManager.flush();

        // Create test rides
        LocalDateTime now = LocalDateTime.now();

        // Scheduled ride 1 (future, requested)
        scheduledRide1 = new Ride();
        scheduledRide1.setStatus(RideStatus.REQUESTED);
        scheduledRide1.setScheduledTime(now.plusHours(2));
        scheduledRide1.setPrice(500.0);
        scheduledRide1.setDistanceKm(5.0);
        scheduledRide1.setRoute(route);
        scheduledRide1.setDriver(driver1);

        // Scheduled ride 2 (future, requested)
        scheduledRide2 = new Ride();
        scheduledRide2.setStatus(RideStatus.REQUESTED);
        scheduledRide2.setScheduledTime(now.plusHours(4));
        scheduledRide2.setPrice(600.0);
        scheduledRide2.setDistanceKm(6.0);
        scheduledRide2.setRoute(route);
        scheduledRide2.setDriver(driver1);

        // Finished ride (should not appear in scheduled rides)
        finishedRide = new Ride();
        finishedRide.setStatus(RideStatus.FINISHED);
        finishedRide.setScheduledTime(now.minusHours(2));
        finishedRide.setStartTime(now.minusHours(2));
        finishedRide.setEndTime(now.minusHours(1));
        finishedRide.setPrice(400.0);
        finishedRide.setDistanceKm(4.0);
        finishedRide.setRoute(route);
        finishedRide.setDriver(driver1);

        // In progress ride (should not appear in scheduled rides)
        inProgressRide = new Ride();
        inProgressRide.setStatus(RideStatus.IN_PROGRESS);
        inProgressRide.setScheduledTime(now.minusMinutes(30));
        inProgressRide.setStartTime(now.minusMinutes(30));
        inProgressRide.setPrice(450.0);
        inProgressRide.setDistanceKm(4.5);
        inProgressRide.setRoute(route);
        inProgressRide.setDriver(driver1);

        // Cancelled ride (should not appear in scheduled rides)
        cancelledRide = new Ride();
        cancelledRide.setStatus(RideStatus.CANCELLED);
        cancelledRide.setScheduledTime(now.plusHours(1));
        cancelledRide.setPrice(300.0);
        cancelledRide.setDistanceKm(3.0);
        cancelledRide.setRoute(route);
        cancelledRide.setDriver(driver1);
        cancelledRide.setCancelled(true);

        // Persist rides first
        testEntityManager.persist(scheduledRide1);
        testEntityManager.persist(scheduledRide2);
        testEntityManager.persist(finishedRide);
        testEntityManager.persist(inProgressRide);
        testEntityManager.persist(cancelledRide);
        testEntityManager.flush();

        // Create passengers for rides and set relationships directly
        Passenger passengerEntry1 = new Passenger();
        passengerEntry1.setRide(scheduledRide1);
        passengerEntry1.setUser(passenger1);
        passengerEntry1.setRole(PassengerRole.CREATOR);

        Passenger passengerEntry2 = new Passenger();
        passengerEntry2.setRide(scheduledRide2);
        passengerEntry2.setUser(passenger1);
        passengerEntry2.setRole(PassengerRole.CREATOR);

        Passenger passengerEntry3 = new Passenger();
        passengerEntry3.setRide(scheduledRide2);
        passengerEntry3.setUser(passenger2);
        passengerEntry3.setRole(PassengerRole.PASSENGER);

        // Set up bidirectional relationships manually to avoid circular reference issues
        scheduledRide1.getPassengers().add(passengerEntry1);
        scheduledRide2.getPassengers().add(passengerEntry2);
        scheduledRide2.getPassengers().add(passengerEntry3);

        testEntityManager.persist(passengerEntry1);
        testEntityManager.persist(passengerEntry2);
        testEntityManager.persist(passengerEntry3);
        testEntityManager.flush();
    }

    @Test
    @DisplayName("Should find all scheduled rides for driver")
    void findScheduledRidesByDriver_ShouldReturnAllRequestedRidesForDriver() {
        List<Ride> result = rideRepository.findScheduledRidesByDriver(driver1);

        assertNotNull(result);
        assertEquals(2, result.size());

        // All returned rides should be REQUESTED status
        result.forEach(ride -> assertEquals(RideStatus.REQUESTED, ride.getStatus()));

        // All returned rides should belong to driver1
        result.forEach(ride -> assertEquals(driver1.getId(), ride.getDriver().getId()));

        // Should include our two scheduled rides
        assertTrue(result.stream().anyMatch(ride -> ride.getId().equals(scheduledRide1.getId())));
        assertTrue(result.stream().anyMatch(ride -> ride.getId().equals(scheduledRide2.getId())));
    }

    @Test
    @DisplayName("Should return empty list when driver has no scheduled rides")
    void findScheduledRidesByDriver_ShouldReturnEmptyWhenNoScheduledRides() {
        List<Ride> result = rideRepository.findScheduledRidesByDriver(driver2);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should not include FINISHED rides in scheduled rides")
    void findScheduledRidesByDriver_ShouldNotIncludeFinishedRides() {
        List<Ride> result = rideRepository.findScheduledRidesByDriver(driver1);

        assertNotNull(result);
        assertEquals(2, result.size());

        // Should not include the finished ride
        assertFalse(result.stream().anyMatch(ride -> ride.getId().equals(finishedRide.getId())));
    }

    @Test
    @DisplayName("Should not include IN_PROGRESS rides in scheduled rides")
    void findScheduledRidesByDriver_ShouldNotIncludeInProgressRides() {
        List<Ride> result = rideRepository.findScheduledRidesByDriver(driver1);

        assertNotNull(result);
        assertEquals(2, result.size());

        // Should not include the in-progress ride
        assertFalse(result.stream().anyMatch(ride -> ride.getId().equals(inProgressRide.getId())));
    }

    @Test
    @DisplayName("Should not include CANCELLED rides in scheduled rides")
    void findScheduledRidesByDriver_ShouldNotIncludeCancelledRides() {
        List<Ride> result = rideRepository.findScheduledRidesByDriver(driver1);

        assertNotNull(result);
        assertEquals(2, result.size());

        // Should not include the cancelled ride
        assertFalse(result.stream().anyMatch(ride -> ride.getId().equals(cancelledRide.getId())));
    }

    @Test
    @DisplayName("Should load ride with passengers correctly")
    void findScheduledRidesByDriver_ShouldLoadRideWithPassengers() {
        List<Ride> result = rideRepository.findScheduledRidesByDriver(driver1);

        assertNotNull(result);
        assertEquals(2, result.size());

        // Debug: Print all rides and their details
        for (Ride r : result) {
            System.out.println("Ride ID: " + r.getId() + ", ScheduledTime: " + r.getScheduledTime() +
                    ", Status: " + r.getStatus() + ", Driver: " + r.getDriver().getId());
            System.out.println("Passengers count: " + (r.getPassengers() != null ? r.getPassengers().size() : "NULL"));
        }

        // Find the ride with multiple passengers (scheduledRide2)
        Optional<Ride> rideWithMultiplePassengers = result.stream()
                .filter(ride -> ride.getId().equals(scheduledRide2.getId()))
                .findFirst();

        assertTrue(rideWithMultiplePassengers.isPresent());
        Ride ride = rideWithMultiplePassengers.get();

        // Should have passengers loaded
        assertNotNull(ride.getPassengers());
        assertEquals(2, ride.getPassengers().size());
    }

    @Test
    @DisplayName("Should load ride with route correctly")
    void findScheduledRidesByDriver_ShouldLoadRideWithRoute() {
        List<Ride> result = rideRepository.findScheduledRidesByDriver(driver1);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        Ride ride = result.get(0);

        // Should have route loaded
        assertNotNull(ride.getRoute());
        assertNotNull(ride.getRoute().getStartLocation());
        assertNotNull(ride.getRoute().getEndLocation());
        assertEquals("Start Address, City, Country", ride.getRoute().getStartLocation().getAddress());
        assertEquals("End Address, City, Country", ride.getRoute().getEndLocation().getAddress());
    }

    @Test
    @DisplayName("Should handle multiple drivers with different scheduled rides")
    void findScheduledRidesByDriver_ShouldHandleMultipleDrivers() {
        // Add a scheduled ride for driver2
        Ride driver2Ride = new Ride();
        driver2Ride.setStatus(RideStatus.REQUESTED);
        driver2Ride.setScheduledTime(LocalDateTime.now().plusHours(3));
        driver2Ride.setPrice(700.0);
        driver2Ride.setDistanceKm(7.0);
        driver2Ride.setRoute(route);
        driver2Ride.setDriver(driver2);

        testEntityManager.persist(driver2Ride);
        testEntityManager.flush();

        // Test driver1 rides
        List<Ride> driver1Rides = rideRepository.findScheduledRidesByDriver(driver1);
        assertNotNull(driver1Rides);
        assertEquals(2, driver1Rides.size());
        driver1Rides.forEach(ride -> assertEquals(driver1.getId(), ride.getDriver().getId()));

        // Test driver2 rides
        List<Ride> driver2Rides = rideRepository.findScheduledRidesByDriver(driver2);
        assertNotNull(driver2Rides);
        assertEquals(1, driver2Rides.size());
        assertEquals(driver2.getId(), driver2Rides.get(0).getDriver().getId());
    }

    @Test
    @DisplayName("Should handle edge case with rides scheduled in the past but still REQUESTED")
    void findScheduledRidesByDriver_ShouldIncludePastRequestedRides() {
        // Create a ride scheduled in the past but still with REQUESTED status
        // This might happen in edge cases
        Ride pastRequestedRide = new Ride();
        pastRequestedRide.setStatus(RideStatus.REQUESTED);
        pastRequestedRide.setScheduledTime(LocalDateTime.now().minusHours(1));
        pastRequestedRide.setPrice(350.0);
        pastRequestedRide.setDistanceKm(3.5);
        pastRequestedRide.setRoute(route);
        pastRequestedRide.setDriver(driver1);

        testEntityManager.persist(pastRequestedRide);
        testEntityManager.flush();

        List<Ride> result = rideRepository.findScheduledRidesByDriver(driver1);

        assertNotNull(result);
        assertEquals(3, result.size()); // 2 original + 1 past requested

        // Should include the past requested ride
        assertTrue(result.stream().anyMatch(ride -> ride.getId().equals(pastRequestedRide.getId())));
    }
}
