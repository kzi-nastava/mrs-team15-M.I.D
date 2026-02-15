package rs.ac.uns.ftn.asd.ridenow.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        // Only initialize pages that don't wait for a specific page state.
        loginPage = new LoginPage(driver);
        navbarPage = new NavbarPage(driver);
    }

    @Test
    void rideOrderingFromFavorites_test_multiple_stops() {
        // Login
        loginPage.setEmail(seeder.getUserEmail());
        loginPage.setPassword(seeder.getUserPassword());
        loginPage.login();

        assertTrue(navbarPage.isLoggedIn(), "User should be logged in");

        // Navigate to Ride History via navbar and add the first history entry to favorites
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
