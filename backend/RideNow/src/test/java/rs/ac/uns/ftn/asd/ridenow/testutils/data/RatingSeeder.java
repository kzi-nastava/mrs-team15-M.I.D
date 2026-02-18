package rs.ac.uns.ftn.asd.ridenow.testutils.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.asd.ridenow.model.*;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.PassengerRole;
import rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.repository.*;

import java.time.LocalDateTime;

/**
 * Seeder specifically for E2E testing of rating functionality
 * Creates a user and driver with specific rides that can be rated
 */
@Component
public class RatingSeeder {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String USER_EMAIL = "testuser@ridenow.com";
    private final String USER_PASSWORD = "superNiceStrOngPassword!132";
    private final String DRIVER_EMAIL = "testdriver@ridenow.com";
    private final String DRIVER_PASSWORD = "superNiceStrOngPassword!132";

    public void seedAll() {
        clearAll();
        seedUsers();
        seedRateableRide();
    }

    private void seedUsers() {
        // Create test passenger user
        RegisteredUser testUser = new RegisteredUser();
        testUser.setFirstName("Test");
        testUser.setLastName("Passenger");
        testUser.setEmail(USER_EMAIL);
        testUser.setPassword(passwordEncoder.encode(USER_PASSWORD));
        testUser.setRole(UserRoles.USER);
        testUser.setActive(true);
        testUser.setPhoneNumber("+381601234567");
        testUser.setAddress("Test Street 123, Novi Sad");
        testUser.setBlocked(false);
        userRepository.save(testUser);

        // Create test driver
        Driver testDriver = new Driver();
        testDriver.setFirstName("Test");
        testDriver.setLastName("Driver");
        testDriver.setEmail(DRIVER_EMAIL);
        testDriver.setPassword(passwordEncoder.encode(DRIVER_PASSWORD));
        testDriver.setRole(UserRoles.DRIVER);
        testDriver.setActive(true);
        testDriver.setPhoneNumber("+381609876543");
        testDriver.setAddress("Driver Avenue 456, Novi Sad");
        testDriver.setBlocked(false);
        testDriver.setAvailable(true);
        testDriver.setRating(4.5);
        testDriver.setWorkingHoursLast24(5.0);
        testDriver.setStatus(DriverStatus.ACTIVE);
        userRepository.save(testDriver);
    }

    /**
     * Creates a ride that finished less than 3 days ago and can be rated
     */
    private void seedRateableRide() {
        RegisteredUser user = (RegisteredUser) userRepository.findByEmail(USER_EMAIL)
                .orElseThrow(() -> new RuntimeException("Test user not found"));
        Driver driver = (Driver) userRepository.findByEmail(DRIVER_EMAIL)
                .orElseThrow(() -> new RuntimeException("Test driver not found"));

        // Create a route
        Location startLocation = new Location(45.2551, 19.8451, "Bulevar oslobodjenja 46, Novi Sad");
        Location endLocation = new Location(45.2671, 19.8335, "Futoska 32, Novi Sad");
        Route route = new Route(3.2, 8.0, startLocation, endLocation);
        routeRepository.save(route);

        // Create a ride that ended 1 day ago (within 3 day rating window)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime rideStart = now.minusDays(1).minusHours(1);
        LocalDateTime rideEnd = now.minusDays(1);

        Ride ride = new Ride();
        ride.setRoute(route);
        ride.setDriver(driver);
        ride.setDistanceKm(route.getDistanceKm());
        ride.setScheduledTime(rideStart);
        ride.setStartTime(rideStart);
        ride.setEndTime(rideEnd);
        ride.setPrice(350.0);
        ride.setCancelled(false);
        ride.setStatus(RideStatus.FINISHED);
        ride = rideRepository.save(ride);

        // Add passenger to the ride
        Passenger passenger = new Passenger();
        passenger.setRide(ride);
        passenger.setUser(user);
        passenger.setRole(PassengerRole.CREATOR);
        passengerRepository.save(passenger);

        // Do NOT create a rating - this ride should be rateable
    }

    /**
     * Creates an additional ride that already has a rating (should not show Rate
     * button)
     */
    public void seedRatedRide() {
        RegisteredUser user = (RegisteredUser) userRepository.findByEmail(USER_EMAIL)
                .orElseThrow(() -> new RuntimeException("Test user not found"));
        Driver driver = (Driver) userRepository.findByEmail(DRIVER_EMAIL)
                .orElseThrow(() -> new RuntimeException("Test driver not found"));

        // Create another route
        Location startLocation = new Location(45.2451, 19.8551, "Narodnih heroja 5, Novi Sad");
        Location endLocation = new Location(45.2571, 19.8435, "Partizanska 12, Novi Sad");
        Route route = new Route(2.5, 6.0, startLocation, endLocation);
        routeRepository.save(route);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime rideStart = now.minusHours(10);
        LocalDateTime rideEnd = now.minusHours(9);

        Ride ride = new Ride();
        ride.setRoute(route);
        ride.setDriver(driver);
        ride.setDistanceKm(route.getDistanceKm());
        ride.setScheduledTime(rideStart);
        ride.setStartTime(rideStart);
        ride.setEndTime(rideEnd);
        ride.setPrice(280.0);
        ride.setCancelled(false);
        ride.setStatus(RideStatus.FINISHED);
        ride = rideRepository.save(ride);

        Passenger passenger = new Passenger();
        passenger.setRide(ride);
        passenger.setUser(user);
        passenger.setRole(PassengerRole.CREATOR);
        passengerRepository.save(passenger);

        // Add a rating to this ride
        Rating rating = new Rating();
        rating.setRide(ride);
        rating.setDriverRating(5);
        rating.setVehicleRating(4);
        rating.setDriverComment("Excellent driver!");
        rating.setVehicleComment("Clean and comfortable.");
        rating.setCreatedAt(rideEnd.plusHours(1));
        ratingRepository.save(rating);
    }

    /**
     * Creates a ride that's older than 3 days (should not be rateable)
     */
    public void seedOldRide() {
        RegisteredUser user = (RegisteredUser) userRepository.findByEmail(USER_EMAIL)
                .orElseThrow(() -> new RuntimeException("Test user not found"));
        Driver driver = (Driver) userRepository.findByEmail(DRIVER_EMAIL)
                .orElseThrow(() -> new RuntimeException("Test driver not found"));

        Location startLocation = new Location(45.2351, 19.8651, "Maksima Gorkog 8, Novi Sad");
        Location endLocation = new Location(45.2471, 19.8535, "Kralja Petra 20, Novi Sad");
        Route route = new Route(1.8, 4.0, startLocation, endLocation);
        routeRepository.save(route);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime rideStart = now.minusDays(5); // More than 3 days ago
        LocalDateTime rideEnd = rideStart.plusMinutes(20);

        Ride ride = new Ride();
        ride.setRoute(route);
        ride.setDriver(driver);
        ride.setDistanceKm(route.getDistanceKm());
        ride.setScheduledTime(rideStart);
        ride.setStartTime(rideStart);
        ride.setEndTime(rideEnd);
        ride.setPrice(220.0);
        ride.setCancelled(false);
        ride.setStatus(RideStatus.FINISHED);
        ride = rideRepository.save(ride);

        Passenger passenger = new Passenger();
        passenger.setRide(ride);
        passenger.setUser(user);
        passenger.setRole(PassengerRole.CREATOR);
        passengerRepository.save(passenger);
    }

    public void clearAll() {
        ratingRepository.deleteAll();
        passengerRepository.deleteAll();
        rideRepository.deleteAll();
        routeRepository.deleteAll();
        userRepository.deleteAll();
    }

    public String getUserEmail() {
        return USER_EMAIL;
    }

    public String getUserPassword() {
        return USER_PASSWORD;
    }

    public String getDriverEmail() {
        return DRIVER_EMAIL;
    }

    public String getDriverPassword() {
        return DRIVER_PASSWORD;
    }
}
