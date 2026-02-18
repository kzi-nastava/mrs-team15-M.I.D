package rs.ac.uns.ftn.asd.ridenow.testutils.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {
    private WebDriver driver;
    private WebDriverWait wait;
    private final String LOGIN_URL = "http://localhost:4200/login";
    private final String ADMIN_HOME = "http://localhost:4200/admin-history-overview";

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(LOGIN_URL);
        PageFactory.initElements(driver, this);
    }

    public void setEmail(String email) {
        WebElement emailComponent = wait
                .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-test='email-input']")));
        WebElement emailInput = emailComponent.findElement(By.tagName("input"));

        wait.until(ExpectedConditions.visibilityOf(emailInput));
        emailInput.clear();
        emailInput.sendKeys(email);
    }

    public void setPassword(String password) {
        WebElement passwordComponent = wait
                .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-test='password-input']")));
        WebElement passwordInput = passwordComponent.findElement(By.tagName("input"));

        wait.until(ExpectedConditions.visibilityOf(passwordInput));
        passwordInput.clear();
        passwordInput.sendKeys(password);
    }

    public void login() {
        WebElement loginButtonComponent = wait
                .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-test='login-button']")));
        WebElement loginButton = loginButtonComponent.findElement(By.tagName("button"));

        wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        loginButton.click();

        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(LOGIN_URL)));
    }

    public boolean isLogged() {
        return wait.until(ExpectedConditions.urlToBe(ADMIN_HOME));
    }

    public boolean isOnLoginPage() {
        return driver.getCurrentUrl().equals(LOGIN_URL);
    }
}