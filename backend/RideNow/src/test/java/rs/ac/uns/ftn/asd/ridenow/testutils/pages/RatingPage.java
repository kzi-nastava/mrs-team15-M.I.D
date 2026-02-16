package rs.ac.uns.ftn.asd.ridenow.testutils.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class RatingPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public RatingPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    /**
     * Wait for rating page to load
     */
    public void waitForPageLoad() {
        wait.until(ExpectedConditions.urlContains("/rating"));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h4[contains(text(), 'Rate Your Driver')]")));
    }

    /**
     * Set driver rating by clicking on star
     *
     * @param rating 1-5 stars
     */
    public void setDriverRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        try {
            // Find the driver rating star-rating component
            List<WebElement> starRatingComponents = driver.findElements(By.cssSelector("app-star-rating"));
            if (starRatingComponents.isEmpty()) {
                throw new RuntimeException("Star rating component not found");
            }

            WebElement driverStarRating = starRatingComponents.get(0); // First one is driver rating

            // Find stars within this component and click the nth star
            List<WebElement> stars = driverStarRating.findElements(By.cssSelector("span.star"));
            if (stars.size() >= rating) {
                WebElement targetStar = stars.get(rating - 1);
                wait.until(ExpectedConditions.elementToBeClickable(targetStar));
                targetStar.click();
            } else {
                throw new RuntimeException(
                        "Not enough stars found. Expected at least " + rating + ", found " + stars.size());
            }
        } catch (Exception e) {
            System.err.println("Error setting driver rating: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Set vehicle rating by clicking on star
     *
     * @param rating 1-5 stars
     */
    public void setVehicleRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        try {
            // Find the vehicle rating star-rating component
            List<WebElement> starRatingComponents = driver.findElements(By.cssSelector("app-star-rating"));
            if (starRatingComponents.size() < 2) {
                throw new RuntimeException("Vehicle star rating component not found");
            }

            WebElement vehicleStarRating = starRatingComponents.get(1); // Second one is vehicle rating

            // Find stars within this component and click the nth star
            List<WebElement> stars = vehicleStarRating.findElements(By.cssSelector("span.star"));
            if (stars.size() >= rating) {
                WebElement targetStar = stars.get(rating - 1);
                wait.until(ExpectedConditions.elementToBeClickable(targetStar));
                targetStar.click();
            } else {
                throw new RuntimeException(
                        "Not enough stars found. Expected at least " + rating + ", found " + stars.size());
            }
        } catch (Exception e) {
            System.err.println("Error setting vehicle rating: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Set driver comment
     */
    public void setDriverComment(String comment) {
        WebElement driverCommentTextarea = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("driverComment")));
        driverCommentTextarea.clear();
        driverCommentTextarea.sendKeys(comment);
    }

    /**
     * Set vehicle comment
     */
    public void setVehicleComment(String comment) {
        WebElement vehicleCommentTextarea = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("vehicleComment")));
        vehicleCommentTextarea.clear();
        vehicleCommentTextarea.sendKeys(comment);
    }

    /**
     * Click submit rating button
     */
    public void submitRating() {
        try {
            WebElement submitButton = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//app-button[.//button[contains(text(), 'Submit Rating')]]//button")));

            // Scroll the button into view
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitButton);

            // Wait for button to be in viewport and visible
            wait.until(driver -> {
                boolean isDisplayed = submitButton.isDisplayed();
                boolean isInViewport = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var rect = arguments[0].getBoundingClientRect();" +
                    "return (rect.top >= 0 && rect.left >= 0 && rect.bottom <= window.innerHeight && rect.right <= window.innerWidth);",
                    submitButton
                );
                return isDisplayed && isInViewport;
            });

            // Wait for button to be clickable
            wait.until(ExpectedConditions.elementToBeClickable(submitButton));

            // Click the button
            submitButton.click();

            // Wait for navigation away from rating page
            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/rating")));
        } catch (Exception e) {
            System.err.println("Error submitting rating: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Check if submit button is enabled (both ratings are set)
     */
    public boolean isSubmitButtonEnabled() {
        try {
            WebElement submitButton = driver.findElement(
                    By.xpath("//app-button[.//button[contains(text(), 'Submit Rating')]]//button"));

            // Scroll the button into view
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", submitButton);

            // Wait for button to be in viewport and visible
            wait.until(driver -> {
                boolean isDisplayed = submitButton.isDisplayed();
                boolean isInViewport = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var rect = arguments[0].getBoundingClientRect();" +
                    "return (rect.top >= 0 && rect.left >= 0 && rect.bottom <= window.innerHeight && rect.right <= window.innerWidth);",
                    submitButton
                );
                return isDisplayed && isInViewport;
            });

            // Check if the disabled attribute is NOT present or is false
            String disabled = submitButton.getAttribute("disabled");
            return disabled == null || disabled.equals("false");
        } catch (Exception e) {
            return false;
        }
    }
}
