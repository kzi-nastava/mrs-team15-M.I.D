package rs.ac.uns.ftn.asd.ridenow.testutils.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.asd.ridenow.model.Driver;
import rs.ac.uns.ftn.asd.ridenow.model.Vehicle;
import rs.ac.uns.ftn.asd.ridenow.model.PriceConfig;
import rs.ac.uns.ftn.asd.ridenow.model.enums.DriverStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.VehicleType;
import rs.ac.uns.ftn.asd.ridenow.repository.DriverRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.VehicleRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.PriceRepository;
import rs.ac.uns.ftn.asd.ridenow.model.Location;
import rs.ac.uns.ftn.asd.ridenow.model.PolylinePoint;
import rs.ac.uns.ftn.asd.ridenow.model.RegisteredUser;
import rs.ac.uns.ftn.asd.ridenow.model.Route;
import rs.ac.uns.ftn.asd.ridenow.model.FavoriteRoute;
import rs.ac.uns.ftn.asd.ridenow.model.Ride;
import rs.ac.uns.ftn.asd.ridenow.model.Passenger;
import rs.ac.uns.ftn.asd.ridenow.model.enums.RideStatus;
import rs.ac.uns.ftn.asd.ridenow.model.enums.PassengerRole;
import rs.ac.uns.ftn.asd.ridenow.model.enums.UserRoles;
import rs.ac.uns.ftn.asd.ridenow.repository.RegisteredUserRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.RouteRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.RideRepository;
import rs.ac.uns.ftn.asd.ridenow.repository.PassengerRepository;

@Component
public class RideOrderingFromFavoritesSeeder {

    @Autowired
    private RegisteredUserRepository registeredUserRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private rs.ac.uns.ftn.asd.ridenow.repository.FavoriteRouteRepository favoriteRouteRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String USER_EMAIL = "user1@gmail.com";
    private final String USER_PASSWORD = "superLozinka123@@";

    public void seedAll() {
        clearAll();
        seedPriceConfigs();
        seedUserWithFavoriteRoute();
        seedGuestUser();
        seedAvailableDriverNearRoute();
        seedRides();
    }

    private void seedPriceConfigs() {
        // Create price configuration for each vehicle type
        PriceConfig standardConfig = new PriceConfig(VehicleType.STANDARD, 120.0, 50.0);
        PriceConfig luxuryConfig = new PriceConfig(VehicleType.LUXURY, 200.0, 80.0);
        PriceConfig vanConfig = new PriceConfig(VehicleType.VAN, 150.0, 60.0);

        priceRepository.save(standardConfig);
        priceRepository.save(luxuryConfig);
        priceRepository.save(vanConfig);

        System.out.println("[SEEDER] Price configs created: STANDARD(" + standardConfig.getBasePrice() + "+" + standardConfig.getPricePerKm() + "/km), "
                + "LUXURY(" + luxuryConfig.getBasePrice() + "+" + luxuryConfig.getPricePerKm() + "/km), "
                + "VAN(" + vanConfig.getBasePrice() + "+" + vanConfig.getPricePerKm() + "/km)");
    }

    private void seedRides() {
        // create one finished ride that uses the already seeded favorite route
        RegisteredUser user = registeredUserRepository.findByEmail(USER_EMAIL).orElse(null);
        if (user == null) return;

        Route favoriteRoute = null;
        for (Route r : routeRepository.findAll()) {
            if (r.getStartLocation() != null && "Mornarska 57, Novi Sad".equals(r.getStartLocation().getAddress())) {
                favoriteRoute = r;
                break;
            }
        }

        if (favoriteRoute != null) {
            Ride ride = new Ride();
            ride.setRoute(favoriteRoute);
            // assign a driver if present
            if (!driverRepository.findAll().isEmpty()) {
                ride.setDriver(driverRepository.findAll().get(0));
            }
            ride.setDistanceKm(favoriteRoute.getDistanceKm());
            java.time.LocalDateTime scheduled = java.time.LocalDateTime.now().minusDays(3);
            ride.setScheduledTime(scheduled);
            ride.setStartTime(scheduled);
            ride.setEndTime(scheduled.plusMinutes((long) favoriteRoute.getEstimatedTimeMin()));
            ride.setPrice(200 + favoriteRoute.getDistanceKm() * 40);
            ride.setStatus(RideStatus.FINISHED);
            rideRepository.save(ride);

            Passenger p = new Passenger();
            p.setRide(ride);
            p.setUser(user);
            p.setRole(PassengerRole.CREATOR);
            passengerRepository.save(p);
        }

        // create second finished ride with the provided route data
        Route newRoute = new Route(5.04, 11.0, new Location(45.2760171, 19.8259933, "Big Novi Sad, Novi Sad"), new Location(45.2457648, 19.8434868, "Big Fashion, Novi Sad"));

        double[][] pts = new double[][]{
            {45.27601,19.825603},{45.274517,19.825654},{45.273982,19.825849},{45.274446,19.828546},{45.274539,19.829083},{45.274544,19.829115},{45.274587,19.829362},{45.273858,19.829634},{45.273908,19.829942},{45.273915,19.829973},{45.273955,19.830164},{45.273864,19.830133},{45.273817,19.830149},{45.273345,19.830309},{45.273313,19.830316},{45.273282,19.830325},{45.273166,19.830356},{45.273046,19.83039},{45.273015,19.8304},{45.272643,19.830532},{45.272511,19.830636},{45.271922,19.830842},{45.271675,19.830923},{45.270894,19.831192},{45.270577,19.831307},{45.270305,19.831404},{45.270246,19.831424},{45.270138,19.831418},{45.269884,19.831496},{45.26952,19.831606},{45.269489,19.831615},{45.269455,19.831624},{45.269336,19.831661},{45.269229,19.831696},{45.269087,19.831742},{45.269056,19.831754},{45.268778,19.831858},{45.268512,19.831991},{45.268415,19.832132},{45.268216,19.832285},{45.268106,19.832375},{45.268021,19.832462},{45.267891,19.832611},{45.267713,19.832839},{45.267509,19.833122},{45.267477,19.833168},{45.267373,19.833313},{45.26717,19.833598},{45.26693,19.833931},{45.266723,19.834221},{45.266631,19.834286},{45.266206,19.834838},{45.266181,19.834871},{45.26608,19.835007},{45.266008,19.835104},{45.265911,19.835234},{45.265885,19.83527},{45.265714,19.835528},{45.26561,19.835697},{45.265494,19.835923},{45.265221,19.8363},{45.264976,19.836654},{45.264871,19.836794},{45.264584,19.83717},{45.264477,19.83731},{45.26429,19.837566},{45.26325,19.838982},{45.262951,19.839389},{45.262929,19.83942},{45.262906,19.839451},{45.262891,19.839471},{45.262803,19.839595},{45.262724,19.839703},{45.262671,19.839776},{45.262567,19.839916},{45.262055,19.840609},{45.261969,19.840727},{45.261655,19.841154},{45.261377,19.841527},{45.261215,19.841767},{45.261079,19.841994},{45.260923,19.842272},{45.260822,19.842428},{45.260794,19.842472},{45.260715,19.842542},{45.260637,19.842646},{45.260512,19.842806},{45.260495,19.842828},{45.260369,19.842988},{45.26024,19.842954},{45.260175,19.842928},{45.260127,19.842922},{45.260078,19.842922},{45.2599,19.842827},{45.259465,19.842614},{45.258993,19.842416},{45.258739,19.842316},{45.258199,19.842105},{45.258148,19.842085},{45.258125,19.842076},{45.258027,19.842038},{45.257887,19.841988},{45.257869,19.841982},{45.25776,19.841943},{45.257561,19.841841},{45.257448,19.841795},{45.257272,19.841722},{45.257213,19.841697},{45.257098,19.841659},{45.257077,19.84162},{45.257044,19.841594},{45.256998,19.841578},{45.256628,19.841547},{45.256489,19.841549},{45.256428,19.841541},{45.256334,19.84153},{45.256258,19.841517},{45.256167,19.841502},{45.256092,19.841493},{45.255993,19.841482},{45.255828,19.841477},{45.255717,19.841489},{45.255591,19.841531},{45.255506,19.841567},{45.25542,19.84161},{45.255247,19.841711},{45.255221,19.8363}
        };
        for (double[] p : pts) {
            newRoute.getPolylinePoints().add(new PolylinePoint(p[0], p[1]));
        }

        newRoute = routeRepository.save(newRoute);

        Ride ride2 = new Ride();
        ride2.setRoute(newRoute);
        if (!driverRepository.findAll().isEmpty()) ride2.setDriver(driverRepository.findAll().get(0));
        ride2.setDistanceKm(5.04);
        java.time.LocalDateTime scheduled2 = java.time.LocalDateTime.now().minusDays(1);
        ride2.setScheduledTime(scheduled2);
        ride2.setStartTime(scheduled2);
        ride2.setEndTime(scheduled2.plusMinutes(11));
        ride2.setPrice(401.612);
        ride2.setStatus(RideStatus.FINISHED);
        rideRepository.save(ride2);

        Passenger p2 = new Passenger();
        p2.setRide(ride2);
        p2.setUser(user);
        p2.setRole(PassengerRole.CREATOR);
        passengerRepository.save(p2);
        
            // create third finished ride (reverse of favorite with two stops) so it appears in history
            Route revRoute = new Route(10.743, 22.0, new Location(45.2624525, 19.8165981, "Cika Stevina 4, Novi Sad"), new Location(45.2328905, 19.8216559, "Mornarska 57, Novi Sad"));

            // add two stops
            revRoute.addStopLocation(new Location(45.2760171, 19.8259933, "Big Novi Sad, Novi Sad"));
            revRoute.addStopLocation(new Location(45.2457648, 19.8434868, "Big Fashion, Novi Sad"));

            double[][] revPts = new double[][]{
                {45.262366,19.816446},{45.263037,19.815668},{45.263091,19.815606},{45.263101,19.815594},{45.263175,19.815509},{45.263586,19.816211},{45.263961,19.816869},{45.263979,19.816904},{45.26399,19.816924},{45.264063,19.817059},{45.264132,19.817177},{45.264197,19.81729},{45.26421,19.817312},{45.264345,19.817546},{45.264776,19.818294},{45.264856,19.818433},{45.265059,19.818787},{45.265275,19.819154},{45.265405,19.819376},{45.265738,19.819982},{45.266139,19.820674},{45.266549,19.821365},{45.266757,19.821732},{45.266946,19.822053},{45.266962,19.822082},{45.267041,19.822222},{45.26737,19.822808},{45.267497,19.823051},{45.267578,19.823205},{45.267779,19.823566},{45.26804,19.824051},{45.268059,19.824087},{45.268079,19.824124},{45.268157,19.82427},{45.268217,19.824382},{45.268298,19.824273},{45.268381,19.824159},{45.268406,19.824124},{45.268443,19.824069},{45.268494,19.824008},{45.268536,19.823965},{45.268583,19.823933},{45.268638,19.823922},{45.268791,19.823866},{45.269133,19.823864},{45.269636,19.823841},{45.269902,19.823834},{45.271114,19.823791},{45.271678,19.82377},{45.271819,19.823764},{45.272032,19.823762},{45.27219,19.82376},{45.272221,19.823759},{45.273109,19.823715},{45.273453,19.823706},{45.274468,19.823655},{45.274983,19.823631},{45.276046,19.823622},{45.27677,19.82359},{45.277701,19.82357},{45.277885,19.823565},{45.278231,19.823543},{45.278638,19.823543},{45.278736,19.823553},{45.278859,19.82359},{45.278969,19.823635},{45.27901,19.823653},{45.279061,19.823694},{45.279105,19.823754},{45.27913,19.823836},{45.279135,19.823908},{45.279124,19.823966},{45.278973,19.824563},{45.278889,19.824897},{45.278806,19.824902},{45.276762,19.824963},{45.276714,19.824971},{45.276701,19.825033},{45.276699,19.825466},{45.276683,19.825558},{45.276642,19.825582},{45.27601,19.825603},{45.274517,19.825654},{45.273982,19.825849},{45.274446,19.828546},{45.274539,19.829083},{45.274544,19.829115},{45.274587,19.829362},{45.273858,19.829634},{45.273908,19.829942},{45.273915,19.829973},{45.273955,19.830164},{45.273864,19.830133},{45.273817,19.830149},{45.273345,19.830309},{45.273313,19.830316},{45.273282,19.830325},{45.273166,19.830356},{45.273046,19.83039},{45.273015,19.8304},{45.272643,19.830532},{45.272511,19.830636},{45.271922,19.830842},{45.271675,19.830923},{45.270894,19.831192},{45.270577,19.831307},{45.270305,19.831404},{45.270246,19.831424},{45.270138,19.831418},{45.269884,19.831496},{45.26952,19.831606},{45.269489,19.831615},{45.269455,19.831624},{45.269336,19.831661},{45.269229,19.831696},{45.269087,19.831742},{45.269056,19.831754},{45.268778,19.831858},{45.268512,19.831991},{45.268415,19.832132},{45.268216,19.832285},{45.268106,19.832375},{45.268021,19.832462},{45.267891,19.832611},{45.267713,19.832839},{45.267509,19.833122},{45.267477,19.833168},{45.267373,19.833313},{45.26717,19.833598},{45.26693,19.833931},{45.266723,19.834221},{45.266631,19.834286},{45.266206,19.834838}
            };
            for (double[] p : revPts) {
                revRoute.getPolylinePoints().add(new PolylinePoint(p[0], p[1]));
            }

            revRoute = routeRepository.save(revRoute);

            Ride ride3 = new Ride();
            ride3.setRoute(revRoute);
            if (!driverRepository.findAll().isEmpty()) ride3.setDriver(driverRepository.findAll().get(0));
            ride3.setDistanceKm(10.743);
            java.time.LocalDateTime scheduled3 = java.time.LocalDateTime.now().minusDays(2);
            ride3.setScheduledTime(scheduled3);
            ride3.setStartTime(scheduled3);
            ride3.setEndTime(scheduled3.plusMinutes(22));
            ride3.setPrice(629.716);
            ride3.setStatus(RideStatus.FINISHED);
            rideRepository.save(ride3);

            Passenger p3 = new Passenger();
            p3.setRide(ride3);
            p3.setUser(user);
            p3.setRole(PassengerRole.CREATOR);
            passengerRepository.save(p3);
    }

    private void seedAvailableDriverNearRoute() {
        // Create first driver with STANDARD vehicle near the seeded route
        Driver driver1 = new Driver();
        driver1.setFirstName("Test");
        driver1.setLastName("Driver");
        driver1.setEmail("available.driver@example.com");
        driver1.setPassword(passwordEncoder.encode("driverPass123"));
        driver1.setActive(true);
        driver1.setBlocked(false);
        driver1.setPhoneNumber("+381609000001");
        driver1.setAddress("Driver Street");
        driver1.setStatus(DriverStatus.ACTIVE);
        driver1.setAvailable(true);
        driver1.setWorkingHoursLast24(1.0);
        driver1.setRating(4.2);
        driver1.setJwtTokenValid(true);

        Vehicle vehicle1 = new Vehicle();
        vehicle1.setLicencePlate("TEST-123");
        vehicle1.setModel("Toyota Prius");
        vehicle1.setLat(45.255); // near seeded route coords
        vehicle1.setLon(19.845);
        vehicle1.setAvailable(true);
        vehicle1.setRating(4.0);
        vehicle1.setPetFriendly(true);
        vehicle1.setChildFriendly(true);
        vehicle1.setSeatCount(4);
        vehicle1.setType(VehicleType.STANDARD);

        // assign bidirectional relation
        vehicle1.assignDriver(driver1);

        // save driver and vehicle
        driverRepository.save(driver1);
        vehicleRepository.save(vehicle1);

        // Create second driver with STANDARD vehicle (backup)
        Driver driver2 = new Driver();
        driver2.setFirstName("Backup");
        driver2.setLastName("Driver");
        driver2.setEmail("backup.driver@example.com");
        driver2.setPassword(passwordEncoder.encode("driverPass456"));
        driver2.setActive(true);
        driver2.setBlocked(false);
        driver2.setPhoneNumber("+381609000002");
        driver2.setAddress("Driver Street 2");
        driver2.setStatus(DriverStatus.ACTIVE);
        driver2.setAvailable(true);
        driver2.setWorkingHoursLast24(2.0);
        driver2.setRating(4.5);
        driver2.setJwtTokenValid(true);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setLicencePlate("TEST-456");
        vehicle2.setModel("Honda Civic");
        vehicle2.setLat(45.260); // near seeded route coords
        vehicle2.setLon(19.850);
        vehicle2.setAvailable(true);
        vehicle2.setRating(4.3);
        vehicle2.setPetFriendly(true);
        vehicle2.setChildFriendly(true);
        vehicle2.setSeatCount(4);
        vehicle2.setType(VehicleType.STANDARD);

        // assign bidirectional relation
        vehicle2.assignDriver(driver2);

        // save driver and vehicle
        driverRepository.save(driver2);
        vehicleRepository.save(vehicle2);

        // Create third driver with LUXURY vehicle
        Driver driver3 = new Driver();
        driver3.setFirstName("Premium");
        driver3.setLastName("Driver");
        driver3.setEmail("premium.driver@example.com");
        driver3.setPassword(passwordEncoder.encode("driverPass789"));
        driver3.setActive(true);
        driver3.setBlocked(false);
        driver3.setPhoneNumber("+381609000003");
        driver3.setAddress("Driver Street 3");
        driver3.setStatus(DriverStatus.ACTIVE);
        driver3.setAvailable(true);
        driver3.setWorkingHoursLast24(0.5);
        driver3.setRating(4.8);
        driver3.setJwtTokenValid(true);

        Vehicle vehicle3 = new Vehicle();
        vehicle3.setLicencePlate("TEST-789");
        vehicle3.setModel("Mercedes S-Class");
        vehicle3.setLat(45.245); // near seeded route coords
        vehicle3.setLon(19.840);
        vehicle3.setAvailable(true);
        vehicle3.setRating(4.7);
        vehicle3.setPetFriendly(false);
        vehicle3.setChildFriendly(true);
        vehicle3.setSeatCount(4);
        vehicle3.setType(VehicleType.LUXURY);

        // assign bidirectional relation
        vehicle3.assignDriver(driver3);

        // save driver and vehicle
        driverRepository.save(driver3);
        vehicleRepository.save(vehicle3);
    }

    private void seedUserWithFavoriteRoute() {
        RegisteredUser user = new RegisteredUser();
        user.setFirstName("User1");
        user.setLastName("User1");
        user.setEmail(USER_EMAIL);
        user.setPassword(passwordEncoder.encode(USER_PASSWORD));
        user.setRole(UserRoles.USER);
        user.setActive(true);
        user.setPhoneNumber("+381640000001");
        user.setAddress("User1 Street");
        user.setBlocked(false);
        user.setJwtTokenValid(true);  // Required for JWT authentication to work
        System.out.println("[SEEDER] Setting jwtTokenValid=true for user: " + USER_EMAIL);

        // create route that will be used as favorite (data provided)
        Location start = new Location(45.2328905, 19.8216559, "Mornarska 57, Novi Sad");
        Location end = new Location(45.2624525, 19.8165981, "Cika Stevina 4, Novi Sad");
        Route route = new Route(7.379, 13.0, start, end);
        // add stops
        route.addStopLocation(new Location(45.2457648, 19.8434868, "Big Fashion, Novi Sad"));

        // add polyline points (trimmed list)
        double[][] pts = new double[][]{
            {45.232946,19.821583},{45.232832,19.821411},{45.23276,19.821302},{45.232813,19.82123},{45.232967,19.82103},
            {45.233792,19.819952},{45.23381,19.819929},{45.233968,19.819718},{45.234003,19.819672},{45.234052,19.819619},
            {45.234555,19.818978},{45.23501,19.818399},{45.235599,19.819284},{45.236174,19.820147},{45.23661,19.820821},
            {45.236731,19.821008},{45.23675,19.821038},{45.23691,19.821285},{45.237111,19.821597},{45.237329,19.821931},
            {45.237847,19.822742},{45.238489,19.823745},{45.238566,19.823895},{45.238601,19.823995},{45.238623,19.824089},
            {45.238653,19.82423},{45.238692,19.824488},{45.238715,19.824956},{45.238789,19.825486},{45.238795,19.825527},
            {45.238801,19.825571},{45.238827,19.825737},{45.238854,19.825862},{45.238877,19.825852},{45.238942,19.825823},
            {45.239047,19.825774},{45.239074,19.825763},{45.239419,19.82561},{45.239656,19.825503},{45.239688,19.82549},
            {45.23972,19.825482},{45.239833,19.825434},{45.239862,19.825604},{45.239873,19.825644},{45.240338,19.827356},
            {45.24035,19.827401},{45.240374,19.827496},{45.240811,19.829163},{45.240895,19.829476},{45.241237,19.83074},
            {45.241243,19.830761},{45.241253,19.830808},{45.241264,19.83085},{45.241311,19.831045},{45.24134,19.831165},
            {45.241398,19.831407},{45.241409,19.83145},{45.24144,19.831571},{45.241691,19.832795},{45.241875,19.833635},
            {45.241952,19.833977},{45.242292,19.835448},{45.242474,19.836299},{45.242486,19.836352},{45.242496,19.836396},
            {45.242546,19.836618},{45.242599,19.836847},{45.242608,19.836886},{45.242991,19.838438},{45.243157,19.839091},
            {45.24329,19.839611},{45.243702,19.841189},{45.243716,19.841244},{45.243728,19.841296},{45.243769,19.841462},
            {45.243805,19.841613},{45.243845,19.841777},{45.243856,19.841823},{45.244075,19.842668},{45.244114,19.84283},
            {45.244624,19.844819},{45.244635,19.844861},{45.244646,19.844903},{45.244724,19.845217},{45.244784,19.845463},
            {45.244883,19.845862},{45.244951,19.846088},{45.245008,19.846299},{45.245067,19.846513},{45.245092,19.84662},
            {45.245108,19.846712},{45.245114,19.846764},{45.24511,19.846859},{45.245093,19.846899},{45.245081,19.846966},
            {45.245083,19.847019},{45.245088,19.847068},{45.245101,19.847115},{45.245146,19.847187},{45.245175,19.84721},
            {45.245207,19.847224},{45.245241,19.847227},{45.245274,19.84722},{45.245304,19.847204},{45.24533,19.84718},
            {45.245352,19.847148},{45.245369,19.847111},{45.245381,19.84707},{45.245386,19.847026},{45.245384,19.846969},
            {45.245371,19.846915},{45.245346,19.846842},{45.245315,19.846805},{45.245279,19.84678},{45.24524,19.846658},
            {45.245207,19.846578},{45.245177,19.846494},{45.245126,19.846331},{45.245073,19.846115},{45.24497,19.845731},
            {45.24483,19.845182},{45.244852,19.845063},{45.244885,19.845006},{45.244902,19.844987},{45.244933,19.844955},
            {45.245257,19.844789},{45.245297,19.844741},{45.245319,19.844688},{45.245389,19.844667},{45.24595,19.844383},
            {45.245997,19.844299},{45.24602,19.844218},{45.246028,19.8442},{45.24602,19.844218},{45.245997,19.844299},
            {45.24595,19.844383},{45.245389,19.844667},{45.245319,19.844688},{45.244896,19.844894},{45.24488,19.844901},
            {45.24486,19.844911},{45.244771,19.844951},{45.244746,19.844851},{45.244735,19.844808},{45.244661,19.844523},
            {45.244193,19.842711},{45.243961,19.841819},{45.243951,19.841776},{45.243939,19.841727},{45.2439,19.841564},
            {45.244005,19.841509},{45.244041,19.841491},{45.244635,19.84115},{45.245279,19.840787},{45.246385,19.840184},
            {45.246577,19.840077},{45.24667,19.840025},{45.246683,19.840018},{45.246713,19.840001},{45.247249,19.839702},
            {45.24774,19.839419},{45.247771,19.839403},{45.247808,19.839383},{45.247919,19.839324},{45.248025,19.839268},
            {45.248147,19.839203},{45.248177,19.839187},{45.24885,19.838813},{45.250194,19.838066},{45.250212,19.838056},
            {45.250236,19.838042},{45.250398,19.837952},{45.251718,19.837211},{45.251732,19.837205},{45.251758,19.837189},
            {45.251768,19.837189},{45.251899,19.837115},{45.251998,19.837057},{45.252122,19.836994},{45.252158,19.836974},
            {45.253283,19.836348},{45.253665,19.836135},{45.254012,19.835944},{45.254145,19.835871},{45.254484,19.835683},
            {45.254659,19.835587},{45.25497,19.835415},{45.254997,19.8354},{45.25503,19.835382},{45.255201,19.83529},
            {45.255237,19.835267},{45.255342,19.835209},{45.255374,19.835192},{45.255727,19.834996},{45.256631,19.834496},
            {45.256834,19.834385},{45.257002,19.834292},{45.259154,19.833103},{45.259522,19.832899},{45.259659,19.832823},
            {45.259813,19.832739},{45.260159,19.832547},{45.260217,19.832515},{45.260438,19.832394},{45.260482,19.832369},
            {45.260507,19.832355},{45.260619,19.83229},{45.260705,19.832238},{45.260656,19.832057},{45.26062,19.831925},
            {45.260598,19.831845},{45.260465,19.831357},{45.260343,19.830908},{45.260093,19.829994},{45.260084,19.82996},
            {45.26008,19.829942},{45.260073,19.829917},{45.260023,19.829735},{45.259983,19.829589},{45.259936,19.829416},
            {45.25993,19.829394},{45.259679,19.828471},{45.259636,19.828256},{45.259613,19.828087},{45.259599,19.827962},
            {45.259502,19.827095},{45.25948,19.826894},{45.25941,19.826634},{45.259396,19.826484},{45.259294,19.825499},
            {45.259277,19.82535},{45.259216,19.824808},{45.259232,19.824533},{45.259211,19.824342},{45.259188,19.824127},
            {45.259169,19.823968},{45.25909,19.823271},{45.259081,19.823169},{45.259076,19.823119},{45.259061,19.822946},
            {45.259162,19.82284},{45.25919,19.82281},{45.259969,19.821888},{45.260038,19.82181},{45.261116,19.820566},
            {45.262254,19.819271},{45.262604,19.818875},{45.262639,19.818836},{45.262662,19.818809},{45.262719,19.818749},
            {45.262798,19.81866},{45.262882,19.818573},{45.262852,19.818544},{45.26282,19.81853},{45.262785,19.818527},
            {45.262727,19.818432},{45.26272,19.818419},{45.262708,19.8184},{45.262685,19.818361},{45.262672,19.818338},
            {45.262525,19.818082},{45.262224,19.817561},{45.262112,19.817368},{45.261893,19.816992},{45.261927,19.816953},
            {45.261937,19.816941},{45.262006,19.816862},{45.262366,19.816446}
        };
        for (double[] p : pts) {
            route.getPolylinePoints().add(new PolylinePoint(p[0], p[1]));
        }

        route = routeRepository.save(route);

        // persist user (do not create any FavoriteRoute initially)
        RegisteredUser savedUser = registeredUserRepository.save(user);
        System.out.println("[SEEDER] User saved: " + savedUser.getEmail() + ", jwtTokenValid=" + savedUser.isJwtTokenValid() + ", role=" + savedUser.getRole());
    }

    private void seedGuestUser() {
        // Create guest user that can be added as linked passenger
        RegisteredUser guest = new RegisteredUser();
        guest.setFirstName("Guest");
        guest.setLastName("User");
        guest.setEmail("user2@gmail.com");
        guest.setPassword(passwordEncoder.encode(USER_PASSWORD));
        guest.setRole(UserRoles.USER);
        guest.setActive(true);
        guest.setPhoneNumber("+381640000002");
        guest.setAddress("Guest Street");
        guest.setBlocked(false);
        guest.setJwtTokenValid(true);
        System.out.println("[SEEDER] Setting jwtTokenValid=true for guest: " + guest.getEmail());
        
        RegisteredUser savedGuest = registeredUserRepository.save(guest);
        System.out.println("[SEEDER] Guest saved: " + savedGuest.getEmail() + ", jwtTokenValid=" + savedGuest.isJwtTokenValid());
    }

    public void clearAll() {
        // remove routes and users created by this seeder
        // Note: this will remove all registered users and routes in test DB; it's acceptable for isolated test profile
        // delete favorite entries first to avoid FK constraint when deleting routes
        try { favoriteRouteRepository.deleteAll(); } catch (Exception ignored) {}
        try { passengerRepository.deleteAll(); } catch (Exception ignored) {}
        try { rideRepository.deleteAll(); } catch (Exception ignored) {}
        try { vehicleRepository.deleteAll(); } catch (Exception ignored) {}
        try { driverRepository.deleteAll(); } catch (Exception ignored) {}
        try { priceRepository.deleteAll(); } catch (Exception ignored) {}
        try { routeRepository.deleteAll(); } catch (Exception ignored) {}
        try { registeredUserRepository.deleteAll(); } catch (Exception ignored) {}
    }

    public String getUserEmail() {
        return USER_EMAIL;
    }

    public String getUserPassword() {
        return USER_PASSWORD;
    }
}
