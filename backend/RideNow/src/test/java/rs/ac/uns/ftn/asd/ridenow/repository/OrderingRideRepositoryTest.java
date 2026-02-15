package rs.ac.uns.ftn.asd.ridenow.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import rs.ac.uns.ftn.asd.ridenow.RideNowApplication;
import rs.ac.uns.ftn.asd.ridenow.model.*;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.PassengerRole;
import rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@ContextConfiguration(classes = RideNowApplication.class)
@DisplayName("RideRepository - Ordering Ride Tests")
class OrderingRideRepositoryTest {

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private EntityManager em;

    private Driver driver;
    private RegisteredUser user;
    private Ride requestedRide;
    private Ride inProgressRide;
    private Route route;

    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        route = createRoute();
        driver = createDriver();  // persistovan
        user = createUser();      // persistovan

        requestedRide = createRide(RideStatus.REQUESTED);
        inProgressRide = createRide(RideStatus.IN_PROGRESS);

        em.flush();
        em.clear();
    }


    // ================= scheduled rides =================

    @Test
    @DisplayName("Should return scheduled rides by driver")
    void findScheduledRidesByDriver_shouldReturnRides() {
        List<Ride> rides = rideRepository.findScheduledRidesByDriver(driver);

        assertFalse(rides.isEmpty());
        assertEquals(RideStatus.REQUESTED, rides.get(0).getStatus());
    }

    @Test
    @DisplayName("Should return scheduled rides in next hour")
    void findScheduledRidesForDriverInNextHour_shouldReturnRides() {
        List<Ride> rides = rideRepository.findScheduledRidesForDriverInNextHour(
                driver.getId(),
                now.minusMinutes(5),
                now.plusHours(1)
        );

        assertNotNull(rides);
    }

    // ================= current ride =================

    @Test
    @DisplayName("Should find current ride by driver")
    void findCurrentRideByDriver_shouldReturnRide() {
        Optional<Ride> result = rideRepository.findCurrentRideByDriver(driver.getId());

        assertTrue(result.isPresent());
        assertEquals(RideStatus.IN_PROGRESS, result.get().getStatus());
    }

    @Test
    @DisplayName("Should find current ride by user")
    void findCurrentRideByUser_shouldReturnRide() {
        Optional<Ride> result = rideRepository.findCurrentRideByUser(user.getId());

        assertTrue(result.isPresent());
        assertEquals(RideStatus.IN_PROGRESS, result.get().getStatus());
    }

    @Test
    @DisplayName("Should return active rides")
    void findActiveRides_shouldReturnList() {
        List<Ride> rides = rideRepository.findActiveRides();

        assertFalse(rides.isEmpty());
    }

    // ================= helpers =================

    private Driver createDriver() {
        Driver driver = new Driver();
        driver.setFirstName("Pera");
        driver.setLastName("Perić");
        driver.setEmail("driver@test.com");
        driver.setPassword("123123");
        driver.setAddress("Novi Sad");
        driver.setPhoneNumber("0641234567");
        driver.setActive(true);
        driver.setBlocked(false);
        driver.setJwtTokenValid(true);
        driver.setRole(UserRoles.DRIVER);
        driver.setAvailable(true);
        driver.setWorkingHoursLast24(0.0);
        driver.setStatus(DriverStatus.ACTIVE);

        em.persist(driver);
        return driver;
    }


    private RegisteredUser createUser() {
        RegisteredUser user = new RegisteredUser();
        user.setFirstName("Mika");
        user.setLastName("Mikic");
        user.setEmail("user@test.com");
        user.setPassword("123123");
        user.setAddress("Novi Sad");
        user.setPhoneNumber("0641234567");
        user.setActive(true);
        user.setBlocked(false);
        user.setJwtTokenValid(true);
        user.setRole(UserRoles.USER);

        em.persist(user);
        return user;
    }

    private Route createRoute() {
        Route route = new Route();

        Location start = new Location();
        start.setAddress("Start");
        start.setLatitude(45.0);
        start.setLongitude(19.0);

        Location end = new Location();
        end.setAddress("End");
        end.setLatitude(45.1);
        end.setLongitude(19.1);

        route.setStartLocation(start);
        route.setEndLocation(end);
        route.setDistanceKm(5.0);
        route.setEstimatedTimeMin(10.0);

        em.persist(route);
        return route;
    }

    private Ride createRide(RideStatus status) {
        Ride ride = new Ride();
        ride.setRoute(route);
        ride.setDriver(driver); // driver mora biti već persistovan
        ride.setStatus(status);
        ride.setScheduledTime(LocalDateTime.now());
        ride.setPrice(100.0);
        ride.setDistanceKm(5.0);

        if (status == RideStatus.IN_PROGRESS) {
            ride.setStartTime(LocalDateTime.now().minusMinutes(5));
        }

        // Napravi Passenger i poveži sa ride i user
        Passenger passenger = new Passenger();
        passenger.setUser(user);                 // user mora biti persistovan
        passenger.setRole(PassengerRole.PASSENGER);
        passenger.setRide(ride);

        ride.getPassengers().add(passenger);

        em.persist(ride); // cascade će persistovati Passenger
        return ride;
    }

}
