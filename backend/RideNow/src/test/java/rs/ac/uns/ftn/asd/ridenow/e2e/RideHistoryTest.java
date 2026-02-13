package rs.ac.uns.ftn.asd.ridenow.e2e;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import rs.ac.uns.ftn.asd.ridenow.testutils.data.RideHistorySeeder;
import rs.ac.uns.ftn.asd.ridenow.testutils.pages.AdminOverviewPage;
import rs.ac.uns.ftn.asd.ridenow.testutils.pages.LoginPage;
import rs.ac.uns.ftn.asd.ridenow.testutils.pages.RideHistoryPage;
import java.time.format.DateTimeFormatter;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RideHistoryTest extends TestBase {

    @Autowired
    private RideHistorySeeder dataSeeder;

    private final String ADMIN_EMAIL = "admin@gmail.com";
    private final String ADMIN_PASSWORD = "123123";
    private final String USER_EMAIL = "user@gmail.com";

    @BeforeEach
    public void setTest() {
        dataSeeder.clearAll();
        dataSeeder.seedAll();
        LoginPage loginPage = new LoginPage(driver);
        loginPage.setEmail(ADMIN_EMAIL);
        loginPage.setPassword(ADMIN_PASSWORD);
        loginPage.login();

        AdminOverviewPage adminOverviewPage = new AdminOverviewPage(driver);
        adminOverviewPage.navigateToUsersRideHistory(USER_EMAIL);
    }

    @Test
    @Order(1)
    @DisplayName("Sort ride history by Start Time ascending")
    public void testSortByStartTimeAscending() {
        RideHistoryPage historyPage = new RideHistoryPage(driver);
        historyPage.sortByColumn("Start time");

        String sortIcon = historyPage.getSortIcon("Start time");
        assertEquals("↑", sortIcon, "Sort icon should show ascending");

        assertTrue(historyPage.isDateTimeSortedAscending(historyPage.getAllStartTimes()), "Start times should be sorted in ascending order");
    }

    @Test
    @Order(2)
    @DisplayName("Sort ride history by Start Time descending")
    public void testSortByStartTimeDescending() {
        RideHistoryPage historyPage = new RideHistoryPage(driver);
        historyPage.sortByColumn("Start time");
        historyPage.sortByColumn("Start time");

        String sortIcon = historyPage.getSortIcon("Start time");
        assertEquals("↓", sortIcon, "Sort icon should show descending");

        assertTrue(historyPage.isDateTimeSortedDescending(historyPage.getAllStartTimes()),"Start times should be sorted in descending order");
    }

    @Test
    @Order(3)
    @DisplayName("Sort ride history by End Time ascending")
    public void testSortByEndTimeAscending() {
        RideHistoryPage historyPage = new RideHistoryPage(driver);
        historyPage.sortByColumn("End time");

        String sortIcon = historyPage.getSortIcon("End time");
        assertEquals("↑", sortIcon, "Sort icon should show ascending");

        assertTrue(historyPage.isDateTimeSortedAscending(historyPage.getAllEndTimes()),"End times should be sorted in ascending order");
    }

    @Test
    @Order(4)
    @DisplayName("Sort ride history by End Time descending")
    public void testSortByEndTimeDescending() {
        RideHistoryPage historyPage = new RideHistoryPage(driver);
        historyPage.sortByColumn("End time");
        historyPage.sortByColumn("End time");

        String sortIcon = historyPage.getSortIcon("End time");
        assertEquals("↓", sortIcon, "Sort icon should show descending");

        assertTrue(historyPage.isDateTimeSortedDescending(historyPage.getAllEndTimes()), "End times should be sorted in descending order");
    }

    @Test
    @Order(5)
    @DisplayName("Sort ride history by Cost ascending")
    public void testSortByCostAscending() {
        RideHistoryPage historyPage = new RideHistoryPage(driver);
        historyPage.sortByColumn("Cost");

        String sortIcon = historyPage.getSortIcon("Cost");
        assertEquals("↑", sortIcon, "Sort icon should show ascending");

        assertTrue(historyPage.isIntegerSortedAscending(historyPage.getAllCosts()),"Costs should be sorted in ascending order");
    }

    @Test
    @Order(6)
    @DisplayName("Sort ride history by Cost descending")
    public void testSortByCostDescending() {
        RideHistoryPage historyPage = new RideHistoryPage(driver);

        historyPage.sortByColumn("Cost");
        historyPage.sortByColumn("Cost");

        String sortIcon = historyPage.getSortIcon("Cost");
        assertEquals("↓", sortIcon, "Sort icon should show descending");

        assertTrue(historyPage.isIntegerSortedDescending(historyPage.getAllCosts()),"Costs should be sorted in descending order");
    }

    @Test
    @Order(7)
    @DisplayName("Sort ride history by Route ascending")
    public void testSortByRouteAscending() {
        RideHistoryPage historyPage = new RideHistoryPage(driver);
        historyPage.sortByColumn("Route");

        String sortIcon = historyPage.getSortIcon("Route");
        assertEquals("↑", sortIcon, "Sort icon should show ascending");

        assertTrue(historyPage.isStringSortedAscending(historyPage.getAllRoutes()),"Routes should be sorted in ascending (alphabetical) order");
    }

    @Test
    @Order(8)
    @DisplayName("Sort ride history by Route descending")
    public void testSortByRouteDescending() {
        RideHistoryPage historyPage = new RideHistoryPage(driver);
        historyPage.sortByColumn("Route");
        historyPage.sortByColumn("Route");

        String sortIcon = historyPage.getSortIcon("Route");
        assertEquals("↓", sortIcon, "Sort icon should show descending");

        assertTrue(historyPage.isStringSortedDescending(historyPage.getAllRoutes()), "Routes should be sorted in descending (alphabetical) order");
    }

    @Test
    @Order(9)
    @DisplayName("Sort ride history by Cancelled ascending")
    public void testSortByCancelledAscending() {
        RideHistoryPage historyPage = new RideHistoryPage(driver);
        historyPage.sortByColumn("Cancelled");

        assertEquals("↑", historyPage.getSortIcon("Cancelled"));
        assertTrue(historyPage.isBooleanSortedAscending(historyPage.getAllCancelledStatuses()), "Cancel should be sorted in ascending order");
    }

    @Test
    @Order(10)
    @DisplayName("Sort ride history by Cancelled descending")
    public void testSortByCancelledDescending() {
        RideHistoryPage historyPage = new RideHistoryPage(driver);
        historyPage.sortByColumn("Cancelled");
        historyPage.sortByColumn("Cancelled");

        assertEquals("↓", historyPage.getSortIcon("Cancelled"));
        assertTrue(historyPage.isBooleanSortedDescending(historyPage.getAllCancelledStatuses()), "Cancel should be sorted in descending order");
    }

    @Test
    @Order(11)
    @DisplayName("Sort ride history by Panic ascending")
    public void testSortByPanicAscending() {
        RideHistoryPage historyPage = new RideHistoryPage(driver);
        historyPage.sortByColumn("Panic");

        assertEquals("↑", historyPage.getSortIcon("Panic"));
        assertTrue(historyPage.isBooleanSortedAscending(historyPage.getAllPanicStatuses()), "Panic should be sorted in ascending order");
    }

    @Test
    @Order(12)
    @DisplayName("Sort ride history by Panic descending")
    public void testSortByPanicDescending() {
        RideHistoryPage historyPage = new RideHistoryPage(driver);
        historyPage.sortByColumn("Panic");
        historyPage.sortByColumn("Panic");

        assertEquals("↓", historyPage.getSortIcon("Panic"));
        assertTrue(historyPage.isBooleanSortedDescending(historyPage.getAllPanicStatuses()), "Panic should be sorted in descending order");
    }

    @Test
    @Order(13)
    @DisplayName("Filter ride history by specific date")
    public void testDateFilter() {
        RideHistoryPage historyPage = new RideHistoryPage(driver);

        LocalDate today = LocalDate.now();
        String inputDate = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String displayDate = today.format(DateTimeFormatter.ofPattern("dd.MM.yyyy."));

        historyPage.filterByDate(inputDate);

        assertTrue(historyPage.validateDateFilter(displayDate), "All rides should match the filtered date");
    }

    @Test
    @Order(14)
    public void testDateFilterNoResults() {
        RideHistoryPage historyPage = new RideHistoryPage(driver);

        String futureDate = "2099-12-31";

        assertTrue(historyPage.filterByDateWithNoResults(futureDate), "Should have 0 rides for future date");
    }
}