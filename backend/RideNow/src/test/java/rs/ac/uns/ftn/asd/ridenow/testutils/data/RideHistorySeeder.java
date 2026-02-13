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
import java.util.List;
import java.util.Random;

@Component
public class RideHistorySeeder {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private PanicAlertRepository panicAlertRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private InconsistencyRepository inconsistencyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Random random = new Random(42);

    public void seedAll() {
        seedUsers();
        seedRoutes();
        seedRides();
    }

    public void seedUsers() {
        Administrator admin = new Administrator();
        admin.setFirstName("Admin");
        admin.setLastName("Admin");
        admin.setEmail("admin@gmail.com");
        admin.setPassword(passwordEncoder.encode("123123"));
        admin.setRole(UserRoles.ADMIN);
        admin.setActive(true);
        admin.setPhoneNumber("+381640000000");
        admin.setAddress("Admin Street 1");
        admin.setBlocked(false);
        userRepository.save(admin);

        RegisteredUser testUser = new RegisteredUser();
        testUser.setFirstName("User");
        testUser.setLastName("User");
        testUser.setEmail("user@gmail.com");
        testUser.setPassword(passwordEncoder.encode("123123"));
        testUser.setRole(UserRoles.USER);
        testUser.setActive(true);
        testUser.setPhoneNumber("+381640000000");
        testUser.setAddress("User Street 1");
        testUser.setBlocked(false);
        userRepository.save(testUser);

        for (int i = 1; i <= 5; i++) {
            RegisteredUser user = new RegisteredUser();
            user.setFirstName("User" + i);
            user.setLastName("User" + i);
            user.setEmail("user" + i + "@gmail.com");
            user.setPassword(passwordEncoder.encode("123123"));
            user.setRole(UserRoles.USER);
            user.setActive(true);
            user.setPhoneNumber("+381640000000");
            user.setAddress("User Street " + i);
            user.setBlocked(false);
            userRepository.save(user);
        }

        for (int i = 1; i <= 3; i++) {
            Driver driver = new Driver();
            driver.setFirstName("Driver" + i);
            driver.setLastName("Driver" + i);
            driver.setEmail("driver" + i + "@gmail.com");
            driver.setPassword(passwordEncoder.encode("123123"));
            driver.setRole(UserRoles.DRIVER);
            driver.setActive(true);
            driver.setPhoneNumber("+381640000000");
            driver.setAddress("Driver Street " + i);
            driver.setBlocked(false);
            driver.setAvailable(true);
            driver.setRating(5.0);
            driver.setWorkingHoursLast24(0.0);
            driver.setStatus(DriverStatus.ACTIVE);
            userRepository.save(driver);
        }
    }

    public void seedRoutes() {
        String[][] routeData = {
                {"Bulevar kralja Petra I 12, Novi Sad", "Novosadskog sajma 4, Novi Sad", "1.05", "2"},
                {"Mornarska 43, Novi Sad", "Bate Brkica 13, Novi Sad", "7.65", "12"},
                {"Cirpanova 14, Novi Sad", "Laze Nancica 3, Novi Sad", "1.93", "3"},
                {"Rumenacka 5, Novi Sad", "Pavla Papa 23, Novi Sad", "1.86", "3"},
                {"Sutjeska 2, Novi Sad", "Jerneja Kopitara 4, Novi Sad", "2.78", "6"},
                {"Futoska 43, Novi Sad", "Bulevar Evrope 5, Novi Sad", "1.52", "3"},
                {"Cankareva 5, Novi Sad", "Vrsacka 2, Novi Sad", "1.82", "3"},
                {"Berislava Berica 4, Novi Sad", "Hadzi Ruvimova 12, Novi Sad", "1.74", "3"},
                {"Ruzin gaj 5, Novi Sad", "Janka Cmelika 4, Novi Sad", "1.88", "4"},
                {"Dinarska 3, Novi Sad", "Brace Dronjak 4, Novi Sad", "2.23", "4"},
                {"Seljackih buna 21, Novi Sad", "Brace Lucic 5, Novi Sad", "3.05", "5"},
                {"Jozefa Marcoka 2, Novi Sad", "Orlovica Pavla 5, Novi Sad", "4.20", "7"},
                {"Vladike Cirica 4, Novi Sad", "Njegoseva 4, Novi Sad", "3.88", "7"},
                {"Lasla Gala, Novi Sad", "Turgenjeva 4, Novi Sad", "2.16", "4"},
                {"Banijska 18, Novi Sad", "Cirpanova 5, Novi Sad", "3.71", "6"}
        };

        for (String[] data : routeData) {
            Location startLocation = new Location(45.25 + random.nextDouble() * 0.02, 19.83 + random.nextDouble() * 0.02, data[0]);
            Location endLocation = new Location(45.25 + random.nextDouble() * 0.02, 19.83 + random.nextDouble() * 0.02, data[1]);
            Route route = new Route(Double.parseDouble(data[2]), Double.parseDouble(data[3]), startLocation, endLocation);
            routeRepository.save(route);
        }
    }
    public void seedRides() {
        RegisteredUser testUser = (RegisteredUser) userRepository.findByEmail("user@gmail.com").orElseThrow(() -> new RuntimeException("Test user not found"));
        Driver driver1 = (Driver) userRepository.findByEmail("driver1@gmail.com").orElseThrow(() -> new RuntimeException("Driver1 not found"));
        Driver driver2 = (Driver) userRepository.findByEmail("driver2@gmail.com").orElseThrow(() -> new RuntimeException("Driver2 not found"));

        List<User> otherUsers = userRepository.findByRole(UserRoles.USER);
        List<Route> routes = routeRepository.findAll();

        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 35; i++) {
            Route route = routes.get(random.nextInt(routes.size()));
            Driver driver = (i % 2 == 0) ? driver1 : driver2;
            Ride ride = new Ride();
            ride.setRoute(route);
            ride.setDriver(driver);
            ride.setDistanceKm(route.getDistanceKm());

            LocalDateTime rideDate;
            if (i < 5) {
                rideDate = now.minusHours(i);
            } else {
                rideDate = now.minusDays(random.nextInt(60) + 1);
            }

            ride.setScheduledTime(rideDate);
            ride.setStartTime(rideDate);
            ride.setEndTime(rideDate.plusMinutes((long) (route.getEstimatedTimeMin() + random.nextInt(10))));

            double price = 200 + (route.getDistanceKm() * 40);
            ride.setPrice(price);

            if (random.nextDouble() < 0.2) {
                ride.setCancelled(true);
                ride.setCancelledBy(random.nextBoolean() ? "PASSENGER" : "DRIVER");
                ride.setCancelReason(i % 2 == 0 ? "Too far away" : "Change of plans");
                ride.setStatus(RideStatus.CANCELLED);
            } else {
                ride.setCancelled(false);
                ride.setStatus(RideStatus.FINISHED);
            }

            ride = rideRepository.save(ride);

            Passenger passenger = new Passenger();
            passenger.setRide(ride);
            passenger.setUser((RegisteredUser) testUser);
            passenger.setRole(PassengerRole.CREATOR);
            passengerRepository.save(passenger);

            if (random.nextDouble() < 0.3 && !otherUsers.isEmpty()) {
                RegisteredUser additionalUser = (RegisteredUser) otherUsers.get(random.nextInt(otherUsers.size()));
                if (!additionalUser.equals(testUser)) {
                    Passenger additionalPassenger = new Passenger();
                    additionalPassenger.setRide(ride);
                    additionalPassenger.setUser(additionalUser);
                    additionalPassenger.setRole(PassengerRole.PASSENGER);
                    passengerRepository.save(additionalPassenger);
                }
            }

            if (!ride.getCancelled() && random.nextDouble() < 0.1) {
                PanicAlert panic = new PanicAlert();
                panic.setRide(ride);
                panic.setPanicBy("USER");
                panic.setTime(ride.getStartTime().plusMinutes(random.nextInt(10)));
                panic.setResolved(true);
                panicAlertRepository.save(panic);
            }

            if (!ride.getCancelled() && random.nextDouble() < 0.6) {
                Rating rating = new Rating();
                rating.setRide(ride);
                rating.setDriverRating(random.nextInt(3) + 3);
                rating.setVehicleRating(random.nextInt(3) + 3);
                rating.setDriverComment(getRatingComment());
                rating.setVehicleComment(getRatingComment());
                rating.setCreatedAt(ride.getEndTime().plusHours(1));
                ratingRepository.save(rating);
            }

            if (!ride.getCancelled() && random.nextDouble() < 0.05) {
                Inconsistency inconsistency = new Inconsistency();
                inconsistency.setRide(ride);
                inconsistency.setPassenger(passenger);
                inconsistency.setDescription(getInconsistencyDescription());
                inconsistencyRepository.save(inconsistency);
            }
        }
    }

    private String getRatingComment() {
        String[] comments = {
                "Great service!",
                "Very professional",
                "Could be better",
                "Excellent driver",
                "Clean vehicle",
                "Smooth ride",
                "Good experience",
                ""
        };
        return comments[random.nextInt(comments.length)];
    }

    private String getInconsistencyDescription() {
        String[] descriptions = {
                "Driver arrived late",
                "Wrong route taken",
                "Unprofessional behavior",
                "Vehicle not clean",
                "Detour without notification"
        };
        return descriptions[random.nextInt(descriptions.length)];
    }

    public void clearAll() {
        inconsistencyRepository.deleteAll();
        ratingRepository.deleteAll();
        panicAlertRepository.deleteAll();
        passengerRepository.deleteAll();
        rideRepository.deleteAll();
        routeRepository.deleteAll();
        userRepository.deleteAll();
    }
}