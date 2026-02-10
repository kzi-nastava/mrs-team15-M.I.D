package repository;

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
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.repository.RideRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.RouteRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@ContextConfiguration(classes = RideNowApplication.class)
@DisplayName("RideRepository - stopRide() Tests")
public class StopRideRepository {

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private Driver driver;
    private Ride inProgressRide;
    private Ride cancelledRide;
    private Ride finishedRide;
    private Ride requestedRide;
    private Route route;

    @BeforeEach
    void setUp() {
        route = createRoute();
        driver = createDriver();
        inProgressRide = createRide(route, RideStatus.IN_PROGRESS);
        cancelledRide = createRide(route, RideStatus.CANCELLED);
        finishedRide = createRide(route, RideStatus.FINISHED);
        requestedRide = createRide(route, RideStatus.REQUESTED);

        testEntityManager.flush();
    }

    @Test
    @DisplayName("Should find ride when driver has IN_PROGRESS ride")
    void findCurrentRideByDriverReturnsRideWhenDriverHasInProgressRide() {
        inProgressRide.setDriver(driver);
        testEntityManager.persist(inProgressRide);
        testEntityManager.flush();

        Optional<Ride> result = rideRepository.findCurrentRideByDriver(driver.getId());

        assertTrue(result.isPresent());
        assertEquals(RideStatus.IN_PROGRESS, result.get().getStatus());
        assertEquals(inProgressRide.getId(), result.get().getId());
        assertEquals(driver.getId(), result.get().getDriver().getId());
    }

    @Test
    @DisplayName("Should return empty when driver has no IN_PROGRESS ride")
    void findCurrentRideByDriverReturnsEmptyWhenDriverHasNoInProgressRide() {
        Optional<Ride> result = rideRepository.findCurrentRideByDriver(driver.getId());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty when driver has only FINISHED rides")
    void findCurrentRideByDriverReturnsEmptyWhenDriverHasOnlyFinishedRides() {
        finishedRide.setDriver(driver);
        testEntityManager.persist(finishedRide);
        testEntityManager.flush();

        Optional<Ride> result = rideRepository.findCurrentRideByDriver(driver.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty when driver ID does not exist")
    void findCurrentRideByDriverReturnsEmptyWhenDriverIdDoesNotExist() {
        Optional<Ride> result = rideRepository.findCurrentRideByDriver(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return only IN_PROGRESS ride when driver has multiple rides")
    void findCurrentRideByDriverReturnsOnlyInProgressRideWhenDriverHasMultipleRides() {
        finishedRide.setDriver(driver);
        testEntityManager.persist(finishedRide);

        inProgressRide.setDriver(driver);
        testEntityManager.persist(inProgressRide);

        cancelledRide.setDriver(driver);
        testEntityManager.persist(cancelledRide);

        requestedRide.setDriver(driver);
        testEntityManager.persist(requestedRide);

        testEntityManager.flush();

        Optional<Ride> result = rideRepository.findCurrentRideByDriver(driver.getId());

        assertTrue(result.isPresent());
        assertEquals(RideStatus.IN_PROGRESS, result.get().getStatus());
        assertEquals(inProgressRide.getId(), result.get().getId());
        assertEquals(driver.getId(), result.get().getDriver().getId());
    }

    @Test
    @DisplayName("Should find route when start and end addresses match")
    void findByStartAndEndAddressReturnsRouteWhenAddressesMatch() {
        String startAddress = route.getStartLocation().getAddress();
        String endAddress = route.getEndLocation().getAddress();

        Optional<Route> result = routeRepository.findByStartAndEndAddress(startAddress, endAddress);

        assertTrue(result.isPresent());
        assertEquals(startAddress, result.get().getStartLocation().getAddress());
        assertEquals(endAddress, result.get().getEndLocation().getAddress());
        assertEquals(route.getId(), result.get().getId());
    }

    @Test
    @DisplayName("Should return empty when addresses do not match")
    void findByStartAndEndAddressReturnsEmptyWhenAddressesDoNotMatch() {
        String startAddress = "Temerinska 55, Novi Sad";
        String endAddress = "Futoška 12, Novi Sad";

        Optional<Route> result = routeRepository.findByStartAndEndAddress(startAddress, endAddress);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty when only start address matches")
    void findByStartAndEndAddressReturnsEmptyWhenOnlyStartMatches() {
        String startAddress = route.getStartLocation().getAddress();
        String endAddress = "Futoška 12, Novi Sad";

        Optional<Route> result = routeRepository.findByStartAndEndAddress(startAddress, endAddress);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return empty when only end address matches")
    void findByStartAndEndAddressReturnsEmptyWhenOnlyEndMatches() {
        String startAddress = "Temerinska 55, Novi Sad";
        String endAddress = route.getEndLocation().getAddress();

        Optional<Route> result = routeRepository.findByStartAndEndAddress(startAddress, endAddress);

        assertTrue(result.isEmpty());
    }

    // ============= Helper Methods =============

    private Route createRoute() {
        Route route = new Route();

        Location start = new Location();
        start.setAddress("Dunavska 1, Novi Sad");
        start.setLatitude(45.2575);
        start.setLongitude(19.8455);
        route.setStartLocation(start);

        Location end = new Location();
        end.setAddress("Bulevar oslobođenja 102, Novi Sad");
        end.setLatitude(45.2450);
        end.setLongitude(19.8360);
        route.setEndLocation(end);

        route.setDistanceKm(5.0);
        route.setEstimatedTimeMin(10.0);

        return testEntityManager.persist(route);
    }

    private Ride createRide(Route route, RideStatus status) {
        Ride ride = new Ride();
        ride.setRoute(route);
        ride.setStatus(status);
        ride.setScheduledTime(LocalDateTime.now());
        ride.setPrice(0.0);
        ride.setDistanceKm(0.0);

        return testEntityManager.persist(ride);
    }

    private Driver createDriver() {
        Driver driver = new Driver();
        driver.setFirstName("Pera");
        driver.setLastName("Perić");
        driver.setEmail("peraperic@gmail.com");
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

        return testEntityManager.persist(driver);
    }
}