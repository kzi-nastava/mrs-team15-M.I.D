package rs.ac.uns.ftn.asd.ridenow.testutils.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class RidePreferencePage {
    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(css = "select.form-select")
    private WebElement vehicleTypeSelect;

    @FindBy(id = "rpPet")
    private WebElement petSelect;

    @FindBy(id = "rpBabySeat")
    private WebElement babySelect;

    @FindBy(xpath = "//button[contains(@aria-label,'Add guest') or contains(normalize-space(.),'Add guest')]")
    private WebElement addGuestButton;

    @FindBy(css = "input[type='datetime-local']")
    private WebElement dateInput;

    @FindBy(xpath = "//app-button[.//button[normalize-space(text())='Order ride']]//button")
    private WebElement orderButton;


    public RidePreferencePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        PageFactory.initElements(driver, this);
        // wait until the vehicle preference label is present to consider the page ready
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//label[text()='Vehicle preference']")));
    }

    public boolean isPageOpened(){
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//label[text()='Vehicle preference']")));
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    public void openVehicleTypesMenu(){
        wait.until(ExpectedConditions.elementToBeClickable(vehicleTypeSelect)).click();
    }

    public void chooseVehicleType(String type){
        String typeFind = type.toLowerCase();
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//option[contains(translate(@value,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'"+typeFind+"')]")));
        option.click();
        // wait a moment for price update if present
        try { Thread.sleep(400); } catch (InterruptedException ignored) {}
    }

    public boolean checkPrice(String price){
        try{
            WebElement priceElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("selected-price-value")));
            return priceElement.getText().trim().equals(price);
        } catch (Exception e){
            return false;
        }
    }

    public void setPetFriendly(boolean petFriendly){
        try{
            if (petFriendly){
                wait.until(ExpectedConditions.elementToBeClickable(petSelect)).click();
            }
        } catch (Exception ignored){}
    }

    public void setBabyFriendly(boolean babyFriendly) {
        try{
            if (babyFriendly){
                wait.until(ExpectedConditions.elementToBeClickable(babySelect)).click();
            }
        } catch (Exception ignored){}
    }

    public void addGuest(String guestEmail) {
        wait.until(ExpectedConditions.elementToBeClickable(addGuestButton)).click();
        enterGuest(guestEmail);
    }

    public void enterGuest(String guestEmail) {
        WebElement inputEmail = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[contains(@placeholder,'Enter guest email') or contains(@placeholder,'Guest email')]")));
        inputEmail.clear();
        inputEmail.sendKeys(guestEmail);
        // after entering guest email some UIs may require pressing Enter or blur
        inputEmail.sendKeys("\n");
    }

    public void enterScheduledDateTime(String scheduledDateTime) {
        try {
            wait.until(ExpectedConditions.visibilityOf(dateInput));
            // use JS to set value to avoid browser/format issues
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1]; arguments[0].dispatchEvent(new Event('input')); arguments[0].dispatchEvent(new Event('change'));",
                    dateInput, scheduledDateTime
            );
        } catch (Exception e) {
            // fallback to sendKeys
            wait.until(ExpectedConditions.visibilityOf(dateInput)).clear();
            dateInput.sendKeys(scheduledDateTime);
        }
    }

    public void orderRide() {
        try {
            wait.until(ExpectedConditions.visibilityOf(orderButton));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", orderButton);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", orderButton);
        } catch (Exception e) {
            // fallback: click inner button of app-button component
            WebElement btn = driver.findElement(By.xpath("//app-button[.//button[contains(normalize-space(.),'Order ride')]]//button"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});arguments[0].click();", btn);
        }
    }

}
