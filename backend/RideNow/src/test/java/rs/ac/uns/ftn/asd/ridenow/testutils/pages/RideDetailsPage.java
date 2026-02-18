package rs.ac.uns.ftn.asd.ridenow.testutils.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class RideDetailsPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public RideDetailsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void waitForPageLoad() {
        wait.until(ExpectedConditions.urlContains("/history-ride-details"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".card-header")));
    }

    /**
     * Check if rating card is displayed
     */
    public boolean isRatingDisplayed() {
        try {
            WebElement ratingCard = driver.findElement(
                    By.xpath("//div[@class='card-header']//h5[contains(text(), 'Rating')]"));
            return ratingCard.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get driver rating value (e.g., "4/5")
     */
    public String getDriverRatingValue() {
        try {
            WebElement driverRatingValue = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("[data-test='driver-rating-value']")));
            return driverRatingValue.getText().trim();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get driver rating as integer (from "4/5" -> 4)
     */
    public int getDriverRating() {
        String ratingValue = getDriverRatingValue();
        if (ratingValue != null && ratingValue.contains("/")) {
            return Integer.parseInt(ratingValue.split("/")[0]);
        }
        return 0;
    }

    /**
     * Get vehicle rating value (e.g., "5/5")
     */
    public String getVehicleRatingValue() {
        try {
            WebElement vehicleRatingValue = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("[data-test='vehicle-rating-value']")));
            return vehicleRatingValue.getText().trim();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get vehicle rating as integer (from "5/5" -> 5)
     */
    public int getVehicleRating() {
        String ratingValue = getVehicleRatingValue();
        if (ratingValue != null && ratingValue.contains("/")) {
            return Integer.parseInt(ratingValue.split("/")[0]);
        }
        return 0;
    }

    /**
     * Get driver comment text
     */
    public String getDriverComment() {
        try {
            WebElement driverComment = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("[data-test='driver-comment']")));
            return driverComment.getText().trim();
        } catch (Exception e) {
            // If there's no comment, it might not exist
            return "";
        }
    }

    /**
     * Get vehicle comment text
     */
    public String getVehicleComment() {
        try {
            WebElement vehicleComment = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("[data-test='vehicle-comment']")));
            return vehicleComment.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Count filled stars for driver rating
     */
    public int countDriverFilledStars() {
        try {
            List<WebElement> filledStars = driver.findElements(
                    By.xpath(
                            "//label[contains(text(), 'Driver Rating')]/following-sibling::div//i[contains(@class, 'bi-star-fill')]"));
            return filledStars.size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Count filled stars for vehicle rating
     */
    public int countVehicleFilledStars() {
        try {
            List<WebElement> filledStars = driver.findElements(
                    By.xpath(
                            "//label[contains(text(), 'Vehicle Rating')]/following-sibling::div//i[contains(@class, 'bi-star-fill')]"));
            return filledStars.size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get route text from ride details
     */
    public String getRoute() {
        try {
            WebElement routeValue = driver.findElement(
                    By.xpath("//label[contains(text(), 'Route')]/following-sibling::p"));
            return routeValue.getText().trim();
        } catch (Exception e) {
            return null;
        }
    }
}
