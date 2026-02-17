package rs.ac.uns.ftn.asd.ridenow.testutils.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class FindingDriverPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public FindingDriverPage(WebDriver driver){
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        PageFactory.initElements(driver, this);
    }

    public boolean isFindingDriverShown(){
        try{
            // look for common headings / indicators on the page
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                    "//*[normalize-space(text())='Finding driver' or contains(normalize-space(.),'Finding driver') or contains(normalize-space(.),'Searching for drivers') or contains(normalize-space(.),'Searching for a driver') or contains(normalize-space(.),'Looking for available vehicles') or contains(normalize-space(.),'Looking for')]")
                ));
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public boolean waitForDriverFound(long timeoutSeconds){
        try{
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            // Try multiple heuristics indicating driver info appeared
            boolean found = shortWait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class,'driver-card')]") ),
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(normalize-space(.),'Driver found')]") ),
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(normalize-space(.),'Driver:')]") ),
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class,'found-driver')]") )
            ));
            return found;
        } catch (Exception e){
            return false;
        }
    }

    public boolean isDriverFound(){
        try{
            // quick check without waiting
            List<WebElement> candidates = driver.findElements(By.xpath("//div[contains(@class,'driver-card') or contains(@class,'found-driver') or contains(normalize-space(.),'Driver:') or contains(normalize-space(.),'Driver found')]") );
            return !candidates.isEmpty();
        } catch (Exception e){
            return false;
        }
    }

    public String getDriverName(){
        try{
            // Try several possible locations for driver name
            By[] locators = new By[]{
                    By.xpath("//div[contains(@class,'driver-card')]//h3"),
                    By.xpath("//div[contains(@class,'driver-card')]//h4"),
                    By.xpath("//div[contains(@class,'driver-card')]//*[contains(@class,'name')]" ) ,
                    By.xpath("//*[contains(normalize-space(.),'Driver:')]")
            };
            for (By loc : locators){
                List<WebElement> el = driver.findElements(loc);
                if (!el.isEmpty()){
                    String text = el.get(0).getText().trim();
                    if (!text.isEmpty()) return text;
                }
            }
        } catch (Exception ignored){}
        return null;
    }

    public Double getDriverRating(){
        try{
            // try to find numeric rating in common places
            By[] locators = new By[]{
                    By.xpath("//div[contains(@class,'driver-card')]//*[contains(@class,'rating')]") ,
                    By.xpath("//*[contains(normalize-space(.),'Rating') or contains(normalize-space(.),'rating')]") ,
                    By.xpath("//span[contains(@class,'rating')]")
            };
            for (By loc : locators){
                List<WebElement> el = driver.findElements(loc);
                if (!el.isEmpty()){
                    String text = el.get(0).getText().trim();
                    // extract first number-like token
                    String num = text.replaceAll("[^0-9.,]"," ").trim().split("\\s+")[0];
                    num = num.replace(',','.') ;
                    try{
                        return Double.parseDouble(num);
                    } catch (Exception ignored){}
                }
            }
            // As a fallback, try to infer rating from star elements (★ or svg filled)
            List<WebElement> starEls = driver.findElements(By.xpath("//*[contains(.,'★')]") );
            if (!starEls.isEmpty()){
                String starText = starEls.get(0).getText();
                int filled = 0;
                for (char c : starText.toCharArray()) if (c=='★') filled++;
                if (filled>0) return (double) filled;
            }
        } catch (Exception ignored){}
        return null;
    }

    public boolean isDriverRatingAtLeast(double threshold){
        Double r = getDriverRating();
        if (r==null) return false;
        return r >= threshold;
    }

    public boolean isGoodDriver(double minRating){
        // Good driver = driver found AND rating present and >= minRating
        if (!isDriverFound()) return false;
        Double r = getDriverRating();
        return r != null && r >= minRating;
    }

    public boolean waitForGoodDriverFound(long timeoutSeconds, double minRating){
        long end = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < end){
            if (isGoodDriver(minRating)) return true;
        }
        return false;
    }

    public boolean waitForFindingShown(long timeoutSeconds){
        long end = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < end){
            if (isFindingDriverShown()) return true;
        }
        return false;
    }

    /**
     * Check if "No drivers available" message is displayed.
     */
    public boolean isNoDriversAvailableShown(){
        try{
            // Look for the "No drivers available" heading
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                "//*[contains(normalize-space(text()),'No drivers available') or contains(normalize-space(text()),'no drivers available')]"
            )));
            return true;
        } catch (Exception e){
            return false;
        }
    }

    /**
     * Wait for "No drivers available" message to appear.
     * @param timeoutSeconds Maximum time to wait in seconds
     * @return true if message appears within timeout, false otherwise
     */
    public boolean waitForNoDriversAvailable(long timeoutSeconds){
        try{
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            shortWait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(normalize-space(text()),'No drivers available')]")),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(normalize-space(text()),'no drivers available')]")),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(normalize-space(text()),'currently no drivers')]"))
            ));
            return true;
        } catch (Exception e){
            return false;
        }
    }

    /**
     * Get the "no drivers" message text.
     */
    public String getNoDriversMessage(){
        try{
            WebElement messageElement = driver.findElement(By.xpath(
                "//*[contains(normalize-space(text()),'No drivers') or contains(normalize-space(text()),'no drivers')]"
            ));
            return messageElement.getText().trim();
        } catch (Exception e){
            return null;
        }
    }
}
