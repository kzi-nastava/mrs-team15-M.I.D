package rs.ac.uns.ftn.asd.ridenow.testutils.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class RideOrderingPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = ".form-title.mb-4")
    private WebElement title;

    @FindBy(css = ".custom-dropdown-toggle")
    private WebElement favoritesBtn;

    @FindBy(xpath = "//*[normalize-space(text())='Choose route']")
    private WebElement chooseRouteBtn;


    public RideOrderingPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        PageFactory.initElements(driver, this);
        // wait until main title is present to consider the page loaded
        wait.until(ExpectedConditions.visibilityOf(title));
    }

    public boolean isPageOpened(){
        try{
            return title.getText().trim().equals("Order your ride");
        } catch (Exception e){
            return false;
        }
    }

    public void openFavoritesMenu(){
        wait.until(ExpectedConditions.elementToBeClickable(favoritesBtn)).click();
        // wait for menu to appear
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".custom-dropdown-menu")));
    }

    public void chooseFavoriteRoute(String pickUp, String destination, List<String> stops){
        String findBy = createRouteString(pickUp, destination, stops);
        try{
            // Try exact match first (route string or favorite name)
            WebElement routeOpt = null;
            try {
                routeOpt = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//li[normalize-space(text())='"+findBy+"']")));
            } catch (Exception ignored) {
            }

            // If not found, try matching by partial pickup text
            if (routeOpt == null) {
                routeOpt = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//li[contains(normalize-space(text()), '"+pickUp+"')]")));

            }

            // Click the option (use JS for reliability)
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});arguments[0].click();", routeOpt);
        } catch(Exception e){
            throw new NotFoundException("Route not found in favorites: " + findBy);
        }


    }

    public String createRouteString(String pickUp, String destination, List<String> stops){
        StringBuilder route = new StringBuilder(pickUp + " → ");
        for (String stop : stops) {
            route.append(stop).append(" → ");
        }
        route.append(destination);
        return route.toString();
    }

    public boolean isRouteEstimateShown(String distance, String estimated){
        try{
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[normalize-space(text())='"+distance+" km']")));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[normalize-space(text())='"+estimated+" minutes']")));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean waitForEstimateToDisappear(String distance, String estimated) {
        try {
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath("//div[contains(., '" + distance + " km')]")));
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void chooseRoute(){
        try {
            wait.until(ExpectedConditions.visibilityOf(chooseRouteBtn));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", chooseRouteBtn);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", chooseRouteBtn);
        } catch (Exception e) {
            // fallback: click the inner button of the custom component
            WebElement btn = driver.findElement(By.xpath("//app-button[.//button[contains(normalize-space(.),'Choose route')]]//button"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});arguments[0].click();", btn);
        }
    }

    public String getPickupAddress() {
        try {
            WebElement pickupComp = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("app-input-component[placeholder='Enter pickup address']")
            ));
            WebElement input = pickupComp.findElement(By.tagName("input"));
            return input.getAttribute("value").trim();
        } catch (Exception e) {
            return null;
        }
    }

    public String getDestinationAddress() {
        try {
            WebElement destComp = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("app-input-component[placeholder='Enter destination address']")
            ));
            WebElement input = destComp.findElement(By.tagName("input"));
            return input.getAttribute("value").trim();
        } catch (Exception e) {
            return null;
        }
    }

    public java.util.List<String> getStopAddresses() {
        try {
            java.util.List<WebElement> stopInputs = driver.findElements(
                    By.cssSelector("app-input-component[placeholder='Enter stop address'] input")
            );
            java.util.List<String> res = new java.util.ArrayList<>();
            for (WebElement in : stopInputs) {
                res.add(in.getAttribute("value").trim());
            }
            return res;
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Check that the pickup, destination and stops inputs are filled with the expected values.
     * Returns true when all match, false otherwise.
     */
    public boolean checkAutoinput(String pickup, String destination, java.util.List<String> stops) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            return shortWait.until(drv -> {
                String actualPickup = getPickupAddress();
                String actualDestination = getDestinationAddress();
                if (actualPickup == null) actualPickup = "";
                if (actualDestination == null) actualDestination = "";

                if (!actualPickup.equals(pickup)) return false;
                if (!actualDestination.equals(destination)) return false;

                java.util.List<String> actualStops = getStopAddresses();
                java.util.List<String> expectedStops = (stops == null) ? java.util.Collections.emptyList() : new java.util.ArrayList<>(stops);

                if (expectedStops.isEmpty()) {
                    return actualStops.isEmpty();
                }

                if (actualStops.size() != expectedStops.size()) return false;
                for (int i = 0; i < expectedStops.size(); i++) {
                    String a = actualStops.get(i) == null ? "" : actualStops.get(i).trim();
                    String e = expectedStops.get(i) == null ? "" : expectedStops.get(i).trim();
                    if (!a.equals(e)) return false;
                }
                return true;
            });
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Click the placeholder entry in the favorites dropdown labeled '--Choose favorite--'.
     * Throws NotFoundException if placeholder is not present.
     */
    public void choosePlaceholderFavorite() {
        // ensure menu is open
        openFavoritesMenu();
        try {
                WebElement placeholder = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//li[normalize-space(text())='-- Choose favorite --']")
                ));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});arguments[0].click();", placeholder);
        } catch (Exception e) {
            throw new NotFoundException("Placeholder '--Choose favorite--' not found in favorites menu");
        }
    }

    /**
     * Check if a route is drawn on the map by verifying the presence of Leaflet polyline and markers.
     * Returns true if route polyline and at least 2 markers (start/end) are present.
     */
    public boolean isRouteDrawnOnMap() {
        try {
            // Wait for the polyline (route) to be present on the map
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("path.leaflet-interactive")
            ));
            
            // Check for markers (circle markers for start and end points)
            List<WebElement> markers = driver.findElements(By.cssSelector("path.leaflet-interactive"));
            
            // Should have at least 3 elements: 1 polyline for route + 2 for start/end markers
            return markers.size() >= 3;
        } catch (Exception e) {
            return false;
        }
    }

}
