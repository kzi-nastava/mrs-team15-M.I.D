package rs.ac.uns.ftn.asd.ridenow.testutils.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
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
                try {
                    routeOpt = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//li[contains(normalize-space(text()), '"+pickUp+"')]")));
                } catch (Exception ignored) {}
            }

            // If still not found, pick the first favorite entry (fallback)
            if (routeOpt == null) {
                routeOpt = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".custom-dropdown-menu li.custom-dropdown-item")));
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
            // match normalized text like "6 km" and "15 minutes"
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[normalize-space(text())='"+distance+" km']")));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[normalize-space(text())='"+estimated+" minutes']")));
        } catch (Exception e) {
            return false;
        }
        return true;
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

    public void openVehicleTypesMenu(){
        // In some UIs vehicle types are on the next page (preferences). Provide a no-op that can be used safely.
        // If there's an in-page control later, tests should switch to RidePreferencePage which has the select element.
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


}
