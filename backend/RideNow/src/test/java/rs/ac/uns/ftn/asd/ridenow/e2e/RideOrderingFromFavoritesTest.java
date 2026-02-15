package rs.ac.uns.ftn.asd.ridenow.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import rs.ac.uns.ftn.asd.ridenow.testutils.data.RideOrderingFromFavoritesSeeder;
import rs.ac.uns.ftn.asd.ridenow.testutils.pages.FindingDriverPage;
import rs.ac.uns.ftn.asd.ridenow.testutils.pages.LoginPage;
import rs.ac.uns.ftn.asd.ridenow.testutils.pages.RideOrderingPage;
import rs.ac.uns.ftn.asd.ridenow.testutils.pages.RidePreferencePage;
import rs.ac.uns.ftn.asd.ridenow.testutils.pages.NavbarPage;
import rs.ac.uns.ftn.asd.ridenow.testutils.pages.UserHistoryPage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class RideOrderingFromFavoritesTest extends TestBase {

    @Autowired
    private RideOrderingFromFavoritesSeeder seeder;

    private final String pickUp1 = "Cika Stevina 4, Novi Sad";
    private final String destination1 = "Mornarska 57, Novi Sad";
    private final List<String> stops1 = List.of("Big Novi Sad, Novi Sad","Big Fashion, Novi Sad");
    private final String expectedDistance1 = "10.743";
    private final String expectedTime1 = "22";

    private final String pickUp2 = "Big Novi Sad, Novi Sad";
    private final String destination2 = "Big Fashion, Novi Sad";
    private final List<String> stops2 = List.of();
    private final String expectedDistance2 = "5.04";
    private final String expectedTime2 = "11";


    private final String vehicleType = "STANDARD";
    private final String guestEmail = "user2@gmail.com";
    private final int hoursInAdvance = 2;

    private LoginPage loginPage;
    private NavbarPage navbarPage;
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
    @Order(3)
    void rideOrderingFromFavorites_test_addThenRemove_favoriteNotShownOnOrdering() {
        // Login
        loginPage.setEmail(seeder.getUserEmail());
        loginPage.setPassword(seeder.getUserPassword());
        loginPage.login();

        assertTrue(navbarPage.isLoggedIn(), "User should be logged in");

        // Add favorite from history
        navbarPage.navigateToHistory();
        UserHistoryPage historyPage = new UserHistoryPage(driver);
        historyPage.waitForPageLoad();
        historyPage.toggleFavorite(pickUp1, destination1, stops1);
        navbarPage.navigateToOrdering();
        orderingPage = new RideOrderingPage(driver);
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

        assertThrows(NotFoundException.class,() ->orderingPage.chooseFavoriteRoute(pickUp1, destination1, stops1));
    }

    @Test
    void rideOrderingFromFavorites_choosePlaceholder_noAutoFill() {
        // Login
        loginPage.setEmail(seeder.getUserEmail());
        loginPage.setPassword(seeder.getUserPassword());
        loginPage.login();

        assertTrue(navbarPage.isLoggedIn(), "User should be logged in");

        // Navigate to Ride History via navbar
        navbarPage.navigateToHistory();
        UserHistoryPage historyPage2 = new UserHistoryPage(driver);
        historyPage2.waitForPageLoad();
        historyPage2.toggleFavorite(pickUp2, destination2, stops2);

        // Return to ordering via navbar and initialize ordering page
        navbarPage.navigateToOrdering();
        orderingPage = new RideOrderingPage(driver);

        // Choose the seeded favorite route
        orderingPage.openFavoritesMenu();
        orderingPage.chooseFavoriteRoute(pickUp2, destination2, stops2);

        // Verify inputs are autofilled from the favorite
        assertTrue(orderingPage.checkAutoinput(pickUp2, destination2, stops2), "Pickup/destination/stops should be populated from favorite");

        // Verify estimate
        assertTrue(orderingPage.isRouteEstimateShown(expectedDistance2, expectedTime2));

        // Open favorites and choose the placeholder entry
        orderingPage.choosePlaceholderFavorite();


        // Inputs should not be auto-filled
        String actualPickup = orderingPage.getPickupAddress();
        String actualDestination = orderingPage.getDestinationAddress();
        assertTrue(actualPickup == null || actualPickup.isEmpty(), "Pickup should be empty after choosing placeholder");
        assertTrue(actualDestination == null || actualDestination.isEmpty(), "Destination should be empty after choosing placeholder");
        assertTrue(orderingPage.getStopAddresses().isEmpty(), "Stops should be empty after choosing placeholder");
        orderingPage.waitForEstimateToDisappear(expectedDistance2, expectedTime2);

        // No estimate should be shown
        assertFalse(orderingPage.isRouteEstimateShown(expectedDistance2, expectedTime2));
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
        UserHistoryPage historyPage2 = new UserHistoryPage(driver);
        historyPage2.waitForPageLoad();
        historyPage2.toggleFavorite(pickUp1, destination1, stops1);

        // Return to ordering via navbar and initialize ordering page
        navbarPage.navigateToOrdering();
        orderingPage = new RideOrderingPage(driver);

        // Choose the seeded favorite route
        orderingPage.openFavoritesMenu();
        orderingPage.chooseFavoriteRoute(pickUp1, destination1, stops1);

        // Verify inputs are autofilled from the favorite
        assertTrue(orderingPage.checkAutoinput(pickUp1, destination1, stops1), "Pickup/destination/stops should be populated from favorite");

        // Verify estimate
        assertTrue(orderingPage.isRouteEstimateShown(expectedDistance1, expectedTime1));

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String scheduledStr = scheduled.format(formatter);
        preferencePage.enterScheduledDateTime(scheduledStr);

        // Order ride
        preferencePage.orderRide();

        // Initialize finding driver page and wait for searching UI
        findingDriverPage = new FindingDriverPage(driver);
        boolean searchingShown2 = findingDriverPage.waitForFindingShown(10);
        assertTrue(searchingShown2);

        // Wait for a 'good' driver (rating >= 3.0) for up to 25s; fallback to any driver found
        boolean anyFound = findingDriverPage.waitForDriverFound(25);
        assertTrue(anyFound, "Driver info should appear after ordering the ride");


        String driverName = findingDriverPage.getDriverName();
        assertNotNull(driverName, "Driver name should be displayed when driver is found");
    }


    @Test
    @Order(1)
    void rideOrderingFromFavorites_test_no_stops() {
        // Login
        loginPage.setEmail(seeder.getUserEmail());
        loginPage.setPassword(seeder.getUserPassword());
        loginPage.login();

        assertTrue(navbarPage.isLoggedIn(), "User should be logged in");

        // Navigate to Ride History via navbar and add the first history entry to favorites
        navbarPage.navigateToHistory();
        UserHistoryPage historyPage2 = new UserHistoryPage(driver);
        historyPage2.waitForPageLoad();
        historyPage2.toggleFavorite(pickUp2, destination2, stops2);

        // Return to ordering via navbar and initialize ordering page
        navbarPage.navigateToOrdering();
        orderingPage = new RideOrderingPage(driver);

        // Choose the seeded favorite route
        orderingPage.openFavoritesMenu();
        orderingPage.chooseFavoriteRoute(pickUp2, destination2, stops2);

        // Verify inputs are autofilled from the favorite
        assertTrue(orderingPage.checkAutoinput(pickUp2, destination2, stops2), "Pickup/destination/stops should be populated from favorite");

        // Verify estimate
        assertTrue(orderingPage.isRouteEstimateShown(expectedDistance2, expectedTime2));

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String scheduledStr = scheduled.format(formatter);
        preferencePage.enterScheduledDateTime(scheduledStr);

        // Order ride
        preferencePage.orderRide();

        // Initialize finding driver page and wait for searching UI
        findingDriverPage = new FindingDriverPage(driver);
        boolean searchingShown2 = findingDriverPage.waitForFindingShown(10);
        assertTrue(searchingShown2);

        // Wait for a 'good' driver (rating >= 3.0) for up to 25s; fallback to any driver found
        boolean anyFound = findingDriverPage.waitForDriverFound(25);
        assertTrue(anyFound, "Driver info should appear after ordering the ride");


        String driverName = findingDriverPage.getDriverName();
        assertNotNull(driverName, "Driver name should be displayed when driver is found");
    }


}
