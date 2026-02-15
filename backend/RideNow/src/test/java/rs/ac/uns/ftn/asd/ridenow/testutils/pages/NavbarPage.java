package rs.ac.uns.ftn.asd.ridenow.testutils.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class NavbarPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public NavbarPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void logout() {
        try {
            // Logout button is app-button with text "Log out"
            WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//app-button[.//button[contains(text(), 'Log out')]]//button")));
            logoutButton.click();

            // Wait for redirect to login page
            wait.until(ExpectedConditions.urlContains("/login"));
        } catch (Exception e) {
            System.err.println("Error during logout: " + e.getMessage());
            throw e;
        }
    }

    private void openNavbarMenu() {
        try {
            // Click the navbar toggler to open the menu
            WebElement navbarToggler = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button.navbar-toggler")));
            navbarToggler.click();

            // Wait for menu to be visible (has .show class)
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".navbar-collapse.show")));

        } catch (Exception e) {
            System.err.println("Error opening navbar menu: " + e.getMessage());
        }
    }

    public void navigateToHistory() {
        openNavbarMenu();

        // For USER role: Click on "Ride History" link that goes to /user-history
        // XPath: find <a> with text 'Ride History'
        WebElement historyLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), 'Ride History')]")));
        historyLink.click();

        wait.until(ExpectedConditions.urlContains("/user-history"));
    }

    public void navigateToDriverHistory() {
        openNavbarMenu();

        // For DRIVER role: Click on "Ride History" link
        WebElement historyLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), 'Ride History')]")));
        historyLink.click();

        wait.until(ExpectedConditions.urlContains("/driver-history"));
    }


    public void navigateToOrdering() {
        openNavbarMenu();

        // Click on "Order a Ride" which routes to /ride-ordering
        WebElement orderLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(), 'Order a Ride') or contains(text(), 'Ride Now')]")
        ));
        orderLink.click();

        wait.until(ExpectedConditions.urlContains("/ride-ordering"));
    }
    public boolean isLoggedIn() {
        try {
            // Check multiple indicators of being logged in
            String currentUrl = driver.getCurrentUrl();

            // If on login page, definitely not logged in
            if (currentUrl.contains("/login")) {
                return false;
            }

            // Try to find logout button, which should only be present when logged in
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//app-button[.//button[contains(text(), 'Log out')]]//button")));
                return true;
            } catch (Exception e) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
