package rs.ac.uns.ftn.asd.ridenow.testutils.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class AdminOverviewPage {
    private WebDriver driver;
    private WebDriverWait wait;

    public AdminOverviewPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    @FindBy(how = How.CSS, using = "table.table")
    WebElement table;

    public WebElement findUserRow(String email) {
        wait.until(ExpectedConditions.visibilityOf(table));
        List<WebElement> rows = driver.findElements(By.cssSelector("tbody tr.clickable-row"));
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() >= 3) {
                if (cells.get(2).getText().trim().equals(email)) {
                    return row;
                }
            }
        }
        return null;
    }

    private boolean isNextButtonDisabled() {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("app-button#next button")));
        return btn.getAttribute("disabled") != null;
    }

    public void navigateToUsersRideHistory(String email) {
        int maxPages = 20;
        for (int i = 0; i < maxPages; i++) {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("tbody tr.clickable-row")));
            WebElement targetRow = findUserRow(email);
            if (targetRow != null) {
                targetRow.click();
                wait.until(ExpectedConditions.urlContains("/admin-history/"));
                return;
            }
            if (isNextButtonDisabled()) {
                throw new RuntimeException("User " + email + " not found.");
            }
            WebElement nextBtn = driver.findElement(By.id("next"));
            nextBtn.click();
            wait.until(ExpectedConditions.stalenessOf(table));
            this.table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table.table")));
        }
    }

    public boolean isOnRideHistoryPage() {
        return wait.until(ExpectedConditions.urlContains("/admin-history/"));
    }
}