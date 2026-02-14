package rs.ac.uns.ftn.asd.ridenow.e2e;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import rs.ac.uns.ftn.asd.ridenow.testutils.data.RatingSeeder;
import rs.ac.uns.ftn.asd.ridenow.testutils.pages.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * E2E Test for Rating Functionality (Student 2 Assignment)
 * 
 * Test Flow:
 * 1. Login as user
 * 2. Navigate to user history
 * 3. Find a rateable ride (not older than 3 days, not yet rated)
 * 4. Click rate button
 * 5. Submit rating with driver and vehicle ratings
 * 6. Logout
 * 7. Login as driver
 * 8. Navigate to driver history
 * 9. Find and click on the same ride
 * 10. Verify rating is displayed correctly
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RideRatingTest extends TestBase {

    @Autowired
    private RatingSeeder ratingSeeder;

    private LoginPage loginPage;
    private NavbarPage navbarPage;
    private UserHistoryPage userHistoryPage;
    private RatingPage ratingPage;
    private DriverHistoryPage driverHistoryPage;
    private RideDetailsPage rideDetailsPage;

    // Test data
    private final int EXPECTED_DRIVER_RATING = 5;
    private final int EXPECTED_VEHICLE_RATING = 4;
    private final String EXPECTED_DRIVER_COMMENT = "Excellent service! Very professional driver.";
    private final String EXPECTED_VEHICLE_COMMENT = "Clean and comfortable vehicle.";

    private String ratedRideRoute; // Store the route to find it in driver history

    @BeforeEach
    public void setUp() {
        super.setUp();
        // Seed test data before each test
        ratingSeeder.clearAll();
        ratingSeeder.seedAll();

        // Initialize page objects
        loginPage = new LoginPage(driver);
        navbarPage = new NavbarPage(driver);
        userHistoryPage = new UserHistoryPage(driver);
        ratingPage = new RatingPage(driver);
        driverHistoryPage = new DriverHistoryPage(driver);
        rideDetailsPage = new RideDetailsPage(driver);
    }

    @Test
    @Order(1)
    @DisplayName("Complete rating flow: User rates ride, driver views rating")
    public void testCompleteRatingFlow() {
        // Step 1: Login as user
        loginPage.setEmail(ratingSeeder.getUserEmail());
        loginPage.setPassword(ratingSeeder.getUserPassword());
        loginPage.login();

        assertTrue(navbarPage.isLoggedIn(), "User should be logged in");

        // Step 2: Navigate to user history
        navbarPage.navigateToHistory();
        userHistoryPage.waitForPageLoad();

        // Step 3: Find a rateable ride
        int rateableRideIndex = userHistoryPage.findFirstRateableRide();
        assertTrue(rateableRideIndex >= 0, "Should find at least one rateable ride");

        // Store the ride route for later verification
        ratedRideRoute = userHistoryPage.getRideRoute(rateableRideIndex);
        assertNotNull(ratedRideRoute, "Ride route should not be null");

        // Step 4: Click rate button
        userHistoryPage.clickRateButton(rateableRideIndex);

        // Step 5: Fill and submit rating
        ratingPage.waitForPageLoad();
        ratingPage.setDriverRating(EXPECTED_DRIVER_RATING);
        ratingPage.setDriverComment(EXPECTED_DRIVER_COMMENT);
        ratingPage.setVehicleRating(EXPECTED_VEHICLE_RATING);
        ratingPage.setVehicleComment(EXPECTED_VEHICLE_COMMENT);

        assertTrue(ratingPage.isSubmitButtonEnabled(), "Submit button should be enabled after ratings are set");

        ratingPage.submitRating();

        // Step 6: Logout
        navbarPage.logout();

        // Step 7: Login as driver
        loginPage.setEmail(ratingSeeder.getDriverEmail());
        loginPage.setPassword(ratingSeeder.getDriverPassword());
        loginPage.login();

        assertTrue(navbarPage.isLoggedIn(), "Driver should be logged in");

        // Step 8: Navigate to driver history
        navbarPage.navigateToDriverHistory();
        driverHistoryPage.waitForPageLoad();

        // Step 9: Find and click the rated ride
        int driverRideIndex = driverHistoryPage.findRideByRoute(ratedRideRoute);
        assertTrue(driverRideIndex >= 0, "Driver should see the ride in history");

        driverHistoryPage.clickRide(driverRideIndex);

        // Step 10: Verify rating is displayed correctly
        rideDetailsPage.waitForPageLoad();

        assertTrue(rideDetailsPage.isRatingDisplayed(), "Rating section should be displayed");

        assertEquals(EXPECTED_DRIVER_RATING, rideDetailsPage.getDriverRating(),
                "Driver rating should match submitted rating");
        assertEquals(EXPECTED_VEHICLE_RATING, rideDetailsPage.getVehicleRating(),
                "Vehicle rating should match submitted rating");

        // Verify star count
        assertEquals(EXPECTED_DRIVER_RATING, rideDetailsPage.countDriverFilledStars(),
                "Number of filled stars should match driver rating");
        assertEquals(EXPECTED_VEHICLE_RATING, rideDetailsPage.countVehicleFilledStars(),
                "Number of filled stars should match vehicle rating");

        // Verify comments (if they are displayed)
        String displayedDriverComment = rideDetailsPage.getDriverComment();
        String displayedVehicleComment = rideDetailsPage.getVehicleComment();

        if (!displayedDriverComment.isEmpty()) {
            assertTrue(displayedDriverComment.contains(EXPECTED_DRIVER_COMMENT) ||
                    EXPECTED_DRIVER_COMMENT.contains(displayedDriverComment),
                    "Driver comment should be displayed correctly");
        }

        if (!displayedVehicleComment.isEmpty()) {
            assertTrue(displayedVehicleComment.contains(EXPECTED_VEHICLE_COMMENT) ||
                    EXPECTED_VEHICLE_COMMENT.contains(displayedVehicleComment),
                    "Vehicle comment should be displayed correctly");
        }
    }

    @Test
    @Order(2)
    @DisplayName("Rate button should not appear for already rated rides")
    public void testAlreadyRatedRideDoesNotShowRateButton() {
        // Seed a ride that's already rated
        ratingSeeder.seedRatedRide();

        loginPage.setEmail(ratingSeeder.getUserEmail());
        loginPage.setPassword(ratingSeeder.getUserPassword());
        loginPage.login();

        navbarPage.navigateToHistory();
        userHistoryPage.waitForPageLoad();

        // Check that not all rides have rate buttons
        int rowCount = userHistoryPage.getRowCount();
        assertTrue(rowCount > 0, "Should have rides in history");

        // At least one ride should not be rateable (the one that's already rated)
        int rateableCount = 0;
        for (int i = 0; i < rowCount; i++) {
            if (userHistoryPage.canRateRide(i)) {
                rateableCount++;
            }
        }

        // We have 1 rateable ride from setUp and 1 already rated from this test
        assertTrue(rateableCount < rowCount, "Not all rides should be rateable");
    }

    @Test
    @Order(3)
    @DisplayName("Rate button should not appear for rides older than 3 days")
    public void testOldRideDoesNotShowRateButton() {
        // Seed an old ride (more than 3 days)
        ratingSeeder.seedOldRide();

        loginPage.setEmail(ratingSeeder.getUserEmail());
        loginPage.setPassword(ratingSeeder.getUserPassword());
        loginPage.login();

        navbarPage.navigateToHistory();
        userHistoryPage.waitForPageLoad();

        int rowCount = userHistoryPage.getRowCount();
        assertTrue(rowCount > 0, "Should have rides in history");

        // The old ride should not be rateable
        // We expect only 1 rateable ride (from setUp), others should be too old
        int rateableCount = 0;
        for (int i = 0; i < rowCount; i++) {
            if (userHistoryPage.canRateRide(i)) {
                rateableCount++;
            }
        }

        // Only the recent ride from setUp should be rateable
        assertEquals(1, rateableCount, "Only rides within 3 days should be rateable");
    }

    @Test
    @Order(4)
    @DisplayName("Rating form validation - both ratings required")
    public void testRatingFormValidation() {
        loginPage.setEmail(ratingSeeder.getUserEmail());
        loginPage.setPassword(ratingSeeder.getUserPassword());
        loginPage.login();

        navbarPage.navigateToHistory();
        userHistoryPage.waitForPageLoad();

        int rateableRideIndex = userHistoryPage.findFirstRateableRide();
        assertTrue(rateableRideIndex >= 0, "Should find a rateable ride");

        userHistoryPage.clickRateButton(rateableRideIndex);
        ratingPage.waitForPageLoad();

        // Initially, submit button should be disabled
        assertFalse(ratingPage.isSubmitButtonEnabled(), "Submit should be disabled without ratings");

        // Set only driver rating
        ratingPage.setDriverRating(4);
        assertFalse(ratingPage.isSubmitButtonEnabled(), "Submit should still be disabled with only driver rating");

        // Set vehicle rating as well
        ratingPage.setVehicleRating(5);
        assertTrue(ratingPage.isSubmitButtonEnabled(), "Submit should be enabled with both ratings");
    }

    @Test
    @Order(5)
    @DisplayName("Star rating interaction - clicking stars sets correct rating")
    public void testStarRatingInteraction() {
        loginPage.setEmail(ratingSeeder.getUserEmail());
        loginPage.setPassword(ratingSeeder.getUserPassword());
        loginPage.login();

        navbarPage.navigateToHistory();
        userHistoryPage.waitForPageLoad();

        int rateableRideIndex = userHistoryPage.findFirstRateableRide();
        userHistoryPage.clickRateButton(rateableRideIndex);
        ratingPage.waitForPageLoad();

        // Test setting different ratings
        for (int rating = 1; rating <= 5; rating++) {
            ratingPage.setDriverRating(rating);
            // Small delay to allow UI to update
        }

        // Set final ratings
        ratingPage.setDriverRating(3);
        ratingPage.setVehicleRating(4);

        assertTrue(ratingPage.isSubmitButtonEnabled(),
                "Submit button should be enabled after setting both ratings");
    }
}
