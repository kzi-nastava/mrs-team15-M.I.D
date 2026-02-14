package rs.ac.uns.ftn.asd.ridenow.testutils.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class UserHistoryPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public UserHistoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    @FindBy(how = How.CSS, using = "table.table")
    WebElement table;

    public List<WebElement> getTableRows() {
        wait.until(ExpectedConditions.visibilityOf(table));
        return driver.findElements(By.cssSelector("tbody tr"));
    }

    public int getRowCount() {
        return getTableRows().size();
    }

    private String getRowColumnText(int rowIndex, int columnIndex) {
        try {
            List<WebElement> rows = wait
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("tbody tr")));
            if (rowIndex >= rows.size()) {
                return null;
            }
            List<WebElement> cells = rows.get(rowIndex).findElements(By.tagName("td"));
            if (columnIndex >= cells.size()) {
                return null;
            }
            return cells.get(columnIndex).getText().trim();
        } catch (StaleElementReferenceException e) {
            return getRowColumnText(rowIndex, columnIndex);
        }
    }

    public String getRoute(int rowIndex) {
        return getRowColumnText(rowIndex, 1); // Column 1 is Route (0 is favorite star)
    }

    /**
     * Check if the ride at rowIndex can be rated (has a "Rate" button)
     */
    public boolean canRateRide(int rowIndex) {
        try {
            List<WebElement> rows = wait
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("tbody tr")));
            if (rowIndex >= rows.size()) {
                return false;
            }
            WebElement actionsCell = rows.get(rowIndex).findElements(By.tagName("td")).get(4); // Actions column
            List<WebElement> rateButtons = actionsCell
                    .findElements(By.xpath(".//app-button[.//button[contains(text(), 'Rate')]]"));
            return !rateButtons.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Find first ride that can be rated (within 3 days and not yet rated)
     * 
     * @return index of the first rateable ride, or -1 if none found
     */
    public int findFirstRateableRide() {
        int rowCount = getRowCount();
        for (int i = 0; i < rowCount; i++) {
            if (canRateRide(i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Click the Rate button for a specific ride
     */
    public void clickRateButton(int rowIndex) {
        try {
            List<WebElement> rows = wait
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("tbody tr")));
            if (rowIndex >= rows.size()) {
                throw new IllegalArgumentException("Row index out of bounds");
            }

            WebElement actionsCell = rows.get(rowIndex).findElements(By.tagName("td")).get(4);
            WebElement rateButton = actionsCell
                    .findElement(By.xpath(".//app-button[.//button[contains(text(), 'Rate')]]//button"));

            wait.until(ExpectedConditions.elementToBeClickable(rateButton));
            rateButton.click();

            // Wait for navigation to rating page
            wait.until(ExpectedConditions.urlContains("/rating"));
        } catch (Exception e) {
            System.err.println("Error clicking rate button: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get ride ID from the row by extracting from route or other means
     * This assumes you can derive the ride ID from the visible data
     */
    public String getRideRoute(int rowIndex) {
        return getRoute(rowIndex);
    }

    /**
     * Wait for the page to load completely
     */
    public void waitForPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(table));
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("tbody tr")));
    }
}
