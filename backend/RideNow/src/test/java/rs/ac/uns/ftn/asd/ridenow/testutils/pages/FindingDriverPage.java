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
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                    "//*[contains(normalize-space(.),'Looking for available vehicles')]")
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
            WebElement found = shortWait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class,'driver-card')]")));
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public boolean isDriverFound(){
        try{
            List<WebElement> candidates = driver.findElements(By.xpath("//div[contains(@class,'driver-card')]") );
            return !candidates.isEmpty();
        } catch (Exception e){
            return false;
        }
    }

    public String getDriverName(){
        try{
            By[] locators = new By[]{
                    By.xpath("//div[contains(@class,'driver-card')]//*[contains(@class,'name')]")
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

    public boolean waitForFindingShown(long timeoutSeconds){
        long end = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < end){
            if (isFindingDriverShown()) return true;
        }
        return false;
    }

    public boolean isNoDriversAvailableShown(){
        try{
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                "//*[contains(normalize-space(text()),'No drivers available')]"
            )));
            return true;
        } catch (Exception e){
            return false;
        }
    }


    public boolean waitForNoDriversAvailable(long timeoutSeconds){
        try{
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            shortWait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(normalize-space(text()),'No drivers available')]"))
            ));
            return true;
        } catch (Exception e){
            return false;
        }
    }
}
