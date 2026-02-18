package rs.ac.uns.ftn.asd.ridenow.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import rs.ac.uns.ftn.asd.ridenow.testutils.data.RideOrderingFromFavoritesSeeder;
import rs.ac.uns.ftn.asd.ridenow.testutils.pages.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class RideOrderingFromFavoritesTest extends TestBase {

    @Autowired
    private RideOrderingFromFavoritesSeeder seeder;

    private final String pickUp1 = "Cika Stevina 4, Novi Sad";
    private final String destination1 = "Mornarska 57, Novi Sad";
    private final List<String> stops1 = List.of("Big Novi Sad, Novi Sad", "Big Fashion, Novi Sad");
    private final String expectedDistance1 = "10.743";
    private final String expectedTime1 = "22";

    private final String pickUp2 = "Big Novi Sad, Novi Sad";
    private final String destination2 = "Big Fashion, Novi Sad";
    private final List<String> stops2 = List.of();
    private final String expectedDistance2 = "5.04";
    private final String expectedTime2 = "11";
    private final Double expectedStandardPrice2 = 372.0;
    private final Double expectedLuxuryPrice2 = 603.2;
    private final Double expectedVanPrice2 = 452.4;


    private final String driverName1 = "Marko";
    private final String driverName2 = "Ana";

    private final String vehicleType = "STANDARD";
    private final String guestEmail = "user2@gmail.com";
    private final int hoursInAdvance = 2;

    private LoginPage loginPage;
    private NavbarPage navbarPage;
    private UserHistoryPage historyPage;
    private RideOrderingPage orderingPage;
    private RidePreferencePage preferencePage;
    private FindingDriverPage findingDriverPage;

    @BeforeEach
    public void beforeEach() {
        // seed test data
        seeder.clearAll();
        seeder.seedAll();

        loginPage = new LoginPage(driver);
        navbarPage = new NavbarPage(driver);
    }

    @Test
    @Order(1)
    void rideOrderingFromFavorites_test_no_stops() {
        // Login
        loginPage.setEmail(seeder.getUserEmail());
        loginPage.setPassword(seeder.getUserPassword());
        loginPage.login();

        assertTrue(navbarPage.isLoggedIn(), "User should be logged in");

        // Navigate to Ride History via navbar
        navbarPage.navigateToHistory();
        historyPage = new UserHistoryPage(driver);
        historyPage.waitForPageLoad();
        historyPage.toggleFavorite(pickUp2, destination2, stops2);

        // Return to ordering via navbar and initialize ordering page
        navbarPage.navigateToOrdering();
        orderingPage = new RideOrderingPage(driver);
        orderingPage.isPageOpened();

        // Choose the seeded favorite route
        orderingPage.openFavoritesMenu();
        orderingPage.chooseFavoriteRoute(pickUp2, destination2, stops2);

        // Verify inputs are autofilled from the favorite
        assertTrue(orderingPage.checkAutoinput(pickUp2, destination2, stops2), "Pickup/destination/stops should be populated from favorite");
        
        // Verify stops list is empty
        assertTrue(orderingPage.getStopAddresses().isEmpty(),
                "Stop addresses should be empty for route without stops");

        // Verify estimate
        assertTrue(orderingPage.isRouteEstimateShown(expectedDistance2, expectedTime2));

        // Verify route is drawn on map
        assertTrue(orderingPage.isRouteDrawnOnMap(stops2.size()), "Route should be drawn on the map");

        orderingPage.chooseRoute();

        // Initialize preference page after navigation
        preferencePage = new RidePreferencePage(driver);
        assertTrue(preferencePage.isPageOpened());

        // Choose vehicle type, pet and baby friendly options
        preferencePage.openVehicleTypesMenu();
        preferencePage.chooseVehicleType(vehicleType);
        preferencePage.setPetFriendly(true);
        preferencePage.setBabyFriendly(false);
        // Add a guest
        preferencePage.addGuest(guestEmail);
        // Schedule ride
        LocalDateTime scheduled = LocalDateTime.now().plusHours(hoursInAdvance);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String scheduledStr = scheduled.format(formatter);
        preferencePage.enterScheduledDateTime(scheduledStr);

        // Order ride
        preferencePage.orderRide();

        // Initialize finding driver page and wait for searching UI
        findingDriverPage = new FindingDriverPage(driver);
        boolean searchingShown2 = findingDriverPage.waitForFindingShown(10);
        assertTrue(searchingShown2);

        // Driver found
        boolean anyFound = findingDriverPage.waitForDriverFound(25);
        assertTrue(anyFound, "Driver info should appear after ordering the ride");


        String driverName = findingDriverPage.getDriverName();
        assertNotNull(driverName, "Driver name should be displayed when driver is found");
        System.out.println(driverName);
        assertEquals(driverName, driverName2);
    }

    @Test
    @Order(2)
    void rideOrderingFromFavorites_test_multiple_stops() {
        // Login
        loginPage.setEmail(seeder.getUserEmail());
        loginPage.setPassword(seeder.getUserPassword());
        loginPage.login();

        assertTrue(navbarPage.isLoggedIn(), "User should be logged in");

        // Navigate to Ride History via navbar
        navbarPage.navigateToHistory();
        historyPage = new UserHistoryPage(driver);
        historyPage.waitForPageLoad();
        historyPage.toggleFavorite(pickUp1, destination1, stops1);

        // Return to ordering via navbar and initialize ordering page
        navbarPage.navigateToOrdering();
        orderingPage = new RideOrderingPage(driver);
        orderingPage.isPageOpened();

        // Choose the seeded favorite route
        orderingPage.openFavoritesMenu();
        orderingPage.chooseFavoriteRoute(pickUp1, destination1, stops1);

        // Verify inputs are autofilled from the favorite
        assertTrue(orderingPage.checkAutoinput(pickUp1, destination1, stops1), "Pickup/destination/stops should be populated from favorite");

        // Verify estimate
        assertTrue(orderingPage.isRouteEstimateShown(expectedDistance1, expectedTime1));

        // Verify route is drawn on map
        assertTrue(orderingPage.isRouteDrawnOnMap(stops1.size()), "Route should be drawn on the map");

        orderingPage.chooseRoute();

        // Initialize preference page after navigation
        preferencePage = new RidePreferencePage(driver);
        assertTrue(preferencePage.isPageOpened());

        // Choose vehicle type, pet and baby friendly options
        preferencePage.openVehicleTypesMenu();
        preferencePage.chooseVehicleType(vehicleType);
        preferencePage.setPetFriendly(true);
        preferencePage.setBabyFriendly(false);
        // Add a guest
        preferencePage.addGuest(guestEmail);
        // Schedule ride
        LocalDateTime scheduled = LocalDateTime.now().plusHours(hoursInAdvance);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String scheduledStr = scheduled.format(formatter);
        preferencePage.enterScheduledDateTime(scheduledStr);

        // Order ride
        preferencePage.orderRide();

        // Initialize finding driver page and wait for searching UI
        findingDriverPage = new FindingDriverPage(driver);
        boolean searchingShown2 = findingDriverPage.waitForFindingShown(10);
        assertTrue(searchingShown2);

        // Driver found
        boolean anyFound = findingDriverPage.waitForDriverFound(25);
        assertTrue(anyFound, "Driver info should appear after ordering the ride");


        String driverName = findingDriverPage.getDriverName();
        assertNotNull(driverName, "Driver name should be displayed when driver is found");
        System.out.println(driverName);
        assertEquals(driverName, driverName1);
    }

    @Test
    @Order(3)
    void rideOrderingFromFavorites_noDriverForVehicleType_showsNoDriversAvailable() {
        // Login
        loginPage.setEmail(seeder.getUserEmail());
        loginPage.setPassword(seeder.getUserPassword());
        loginPage.login();

        assertTrue(navbarPage.isLoggedIn(), "User should be logged in");

        // Add favorite and navigate to preferences
        navbarPage.navigateToHistory();
        historyPage = new UserHistoryPage(driver);
        historyPage.waitForPageLoad();
        historyPage.toggleFavorite(pickUp2, destination2, stops2);

        navbarPage.navigateToOrdering();
        orderingPage = new RideOrderingPage(driver);
        orderingPage.isPageOpened();
        orderingPage.openFavoritesMenu();
        orderingPage.chooseFavoriteRoute(pickUp2, destination2, stops2);
        orderingPage.chooseRoute();

        preferencePage = new RidePreferencePage(driver);
        assertTrue(preferencePage.isPageOpened(), "Preference page should be opened");

        // Select VAN vehicle type (no drivers available for this type in seeder)
        preferencePage.openVehicleTypesMenu();
        preferencePage.chooseVehicleType("VAN");

        // Schedule ride for valid time
        LocalDateTime scheduled = LocalDateTime.now().plusHours(hoursInAdvance);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        String scheduledStr = scheduled.format(formatter);
        preferencePage.enterScheduledDateTime(scheduledStr);

        // Order ride
        preferencePage.orderRide();

        // Initialize finding driver page
        findingDriverPage = new FindingDriverPage(driver);

        // Wait for searching UI to appear
        boolean searchingShown = findingDriverPage.waitForFindingShown(10);
        assertTrue(searchingShown, "Finding driver page should be shown");

        // Wait for "No drivers available"
        boolean noDriversShown = findingDriverPage.waitForNoDriversAvailable(30);
        assertTrue(noDriversShown, "No drivers available message should appear when no driver exists for VAN type");

        // Verify the message is displayed
        assertTrue(findingDriverPage.isNoDriversAvailableShown(),
                "No drivers available message should be visible");

        // Verify we did NOT find a driver
        assertFalse(findingDriverPage.isDriverFound(),
                "No driver should be found when none are available for the vehicle type");
    }

    @Test
    @Order(4)
    void rideOrderingFromFavorites_test_addThenRemove_favoriteNotShownOnOrdering() {
        // Login
        loginPage.setEmail(seeder.getUserEmail());
        loginPage.setPassword(seeder.getUserPassword());
        loginPage.login();

        assertTrue(navbarPage.isLoggedIn(), "User should be logged in");

        // Add favorite from history
        navbarPage.navigateToHistory();
        historyPage = new UserHistoryPage(driver);
        historyPage.waitForPageLoad();
        historyPage.toggleFavorite(pickUp1, destination1, stops1);
        navbarPage.navigateToOrdering();
        orderingPage = new RideOrderingPage(driver);
        orderingPage.isPageOpened();
        orderingPage.openFavoritesMenu();
        orderingPage.chooseFavoriteRoute(pickUp1, destination1, stops1);

        navbarPage.navigateToHistory();
        historyPage.waitForPageLoad();
        // Now remove it
        historyPage.removeFavorite(pickUp1, destination1);

        // Return to ordering and verify favorite is not present
        navbarPage.navigateToOrdering();
        orderingPage = new RideOrderingPage(driver);
        orderingPage.openFavoritesMenu();

        assertThrows(NotFoundException.class, () -> orderingPage.chooseFavoriteRoute(pickUp1, destination1, stops1));
    }

    @Test
    @Order(5)
    void rideOrderingFromFavorites_switchBetweenMultipleFavorites_correctAutofill() {
        // Login
        loginPage.setEmail(seeder.getUserEmail());
        loginPage.setPassword(seeder.getUserPassword());
        loginPage.login();

        assertTrue(navbarPage.isLoggedIn(), "User should be logged in");

        // Add two different favorites
        navbarPage.navigateToHistory();
        historyPage = new UserHistoryPage(driver);
        historyPage.waitForPageLoad();
        historyPage.toggleFavorite(pickUp1, destination1, stops1);
        historyPage.toggleFavorite(pickUp2, destination2, stops2);

        // Navigate to ordering
        navbarPage.navigateToOrdering();
        orderingPage = new RideOrderingPage(driver);
        orderingPage.isPageOpened();

        // Choose first favorite
        orderingPage.openFavoritesMenu();
        orderingPage.chooseFavoriteRoute(pickUp1, destination1, stops1);
        assertTrue(orderingPage.checkAutoinput(pickUp1, destination1, stops1),
                "First favorite should autofill correctly");
        assertTrue(orderingPage.isRouteEstimateShown(expectedDistance1, expectedTime1),
                "First favorite should show correct estimate");

        // Switch to second favorite
        orderingPage.openFavoritesMenu();
        orderingPage.chooseFavoriteRoute(pickUp2, destination2, stops2);
        assertTrue(orderingPage.checkAutoinput(pickUp2, destination2, stops2),
                "Second favorite should autofill correctly");
        assertTrue(orderingPage.isRouteEstimateShown(expectedDistance2, expectedTime2),
                "Second favorite should show correct estimate");

        // Switch back to first favorite
        orderingPage.openFavoritesMenu();
        orderingPage.chooseFavoriteRoute(pickUp1, destination1, stops1);
        assertTrue(orderingPage.checkAutoinput(pickUp1, destination1, stops1),
                "Switching back to first favorite should work correctly");


        // Open favorites and choose the placeholder entry
        orderingPage.choosePlaceholderFavorite();
        // Inputs should not be autofilled
        String actualPickup = orderingPage.getPickupAddress();
        String actualDestination = orderingPage.getDestinationAddress();
        assertTrue(actualPickup == null || actualPickup.isEmpty(), "Pickup should be empty after choosing placeholder");
        assertTrue(actualDestination == null || actualDestination.isEmpty(), "Destination should be empty after choosing placeholder");
        assertTrue(orderingPage.getStopAddresses().isEmpty(), "Stops should be empty after choosing placeholder");
        orderingPage.waitForEstimateToDisappear(expectedDistance2, expectedTime2);
    }


    @Test
    @Order(6)
    void rideOrderingFromFavorites_dynamicPriceChange_differentVehicleTypes() {
        // Login
        loginPage.setEmail(seeder.getUserEmail());
        loginPage.setPassword(seeder.getUserPassword());
        loginPage.login();

        assertTrue(navbarPage.isLoggedIn(), "User should be logged in");

        // Add favorite from history
        navbarPage.navigateToHistory();
        historyPage = new UserHistoryPage(driver);
        historyPage.waitForPageLoad();
        historyPage.toggleFavorite(pickUp2, destination2, stops2);

        // Navigate to ordering and select favorite
        navbarPage.navigateToOrdering();
        orderingPage = new RideOrderingPage(driver);
        orderingPage.isPageOpened();
        orderingPage.openFavoritesMenu();
        orderingPage.chooseFavoriteRoute(pickUp2, destination2, stops2);

        // Verify inputs are autofilled
        assertTrue(orderingPage.checkAutoinput(pickUp2, destination2, stops2),
                "Pickup/destination should be populated from favorite");

        // Go to preferences page
        orderingPage.chooseRoute();
        preferencePage = new RidePreferencePage(driver);
        assertTrue(preferencePage.isPageOpened(), "Preference page should be opened");

        // Test STANDARD vehicle type - get initial price
        preferencePage.openVehicleTypesMenu();
        preferencePage.chooseVehicleType("STANDARD");
        String standardPrice = preferencePage.getCurrentPrice();
        assertNotNull(standardPrice, "Standard price should be displayed");
        assertFalse(standardPrice.equals("-"), "Standard price should be a valid number");
        Double price = Double.parseDouble(standardPrice.substring(0, standardPrice.length() - 3));
        assertEquals(expectedStandardPrice2,price);

        // Test LUXURY vehicle type
        preferencePage.openVehicleTypesMenu();
        preferencePage.chooseVehicleType("LUXURY");
        String luxuryPrice = preferencePage.getCurrentPrice();
        assertNotNull(luxuryPrice, "Luxury price should be displayed");
        assertFalse(luxuryPrice.equals("-"), "Luxury price should be a valid number");
        assertNotEquals(standardPrice, luxuryPrice, "Luxury price should be different from standard price");
        price  = Double.parseDouble(luxuryPrice.substring(0, luxuryPrice.length() - 3));
        assertEquals(expectedLuxuryPrice2,price);

        // Test VAN vehicle type
        preferencePage.openVehicleTypesMenu();
        preferencePage.chooseVehicleType("VAN");
        String vanPrice = preferencePage.getCurrentPrice();
        assertNotNull(vanPrice, "Van price should be displayed");
        assertFalse(vanPrice.equals("-"), "Van price should be a valid number");
        assertNotEquals(standardPrice, vanPrice, "Van price should be different from standard price");
        assertNotEquals(luxuryPrice, vanPrice, "Van price should be different from luxury price");
        price   = Double.parseDouble(vanPrice.substring(0, vanPrice.length() - 3));
        assertEquals(expectedVanPrice2,price);

        // Switch back to STANDARD and verify price returns to original
        preferencePage.openVehicleTypesMenu();
        preferencePage.chooseVehicleType("STANDARD");
        String standardPriceAgain = preferencePage.getCurrentPrice();
        assertEquals(standardPrice, standardPriceAgain,
                "Standard price should remain consistent when switching back");
    }

}
