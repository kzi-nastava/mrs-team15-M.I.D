package rs.ac.uns.ftn.asd.ridenow.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.file.Paths;

public class TestBase {
    protected WebDriver driver;

    @BeforeEach
    void setUp(){
        // Get absolute path to chromedriver in the project directory
        //String chromedriverPath = Paths.get(System.getProperty("user.dir"), "chromedriver").toString();
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");

        this.driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @AfterEach
    void cleanUp(){
        if (driver != null) {
            driver.quit();
        }
    }
}