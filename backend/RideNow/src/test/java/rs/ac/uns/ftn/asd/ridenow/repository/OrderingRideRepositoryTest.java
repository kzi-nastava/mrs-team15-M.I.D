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
    private Route route;

    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        route = createRoute();
        driver = createDriver();
        user = createUser();

        em.flush();
        em.clear();
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

    // ================= BOUNDARY TESTS =================

    @Test
    @DisplayName("Should return empty list when no scheduled rides in time window")
    void findScheduledRidesForDriverInNextHour_noRidesInWindow_shouldReturnEmpty() {
        LocalDateTime futureTime = now.plusDays(2);
        
        List<Ride> rides = rideRepository.findScheduledRidesForDriverInNextHour(
                driver.getId(),
                futureTime,
                futureTime.plusHours(1)
        );

        assertTrue(rides.isEmpty());
    }

    @Test
    @DisplayName("Should handle boundary times for scheduled rides")
    void findScheduledRidesForDriverInNextHour_exactBoundary_shouldInclude() {
        // Create ride at exact boundary
        Ride boundaryRide = createRide(RideStatus.REQUESTED);
        boundaryRide.setScheduledTime(now.plusMinutes(30));
        em.persist(boundaryRide);
        em.flush();
        em.clear();

        List<Ride> rides = rideRepository.findScheduledRidesForDriverInNextHour(
                driver.getId(),
                now.minusMinutes(1),
                now.plusHours(1).plusMinutes(1)
        );

        assertFalse(rides.isEmpty());
        assertTrue(rides.stream().anyMatch(r -> r.getId().equals(boundaryRide.getId())));
    }

    // ================= helpers =================

    private Driver createDriver() {
        Driver driver = new Driver();
        driver.setFirstName("Pera");
        driver.setLastName("Perić");
        driver.setEmail("driver@gmail.com");
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
        user.setEmail("user@gmail.com");
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
        ride.setDriver(driver);
        ride.setStatus(status);
        ride.setScheduledTime(LocalDateTime.now());
        ride.setPrice(100.0);
        ride.setDistanceKm(5.0);

        if (status == RideStatus.IN_PROGRESS) {
            ride.setStartTime(LocalDateTime.now().minusMinutes(5));
        }
        Passenger passenger = new Passenger();
        passenger.setUser(user);
        passenger.setRole(PassengerRole.PASSENGER);
        passenger.setRide(ride);

        ride.getPassengers().add(passenger);

        em.persist(ride);
        return ride;
    }

}
