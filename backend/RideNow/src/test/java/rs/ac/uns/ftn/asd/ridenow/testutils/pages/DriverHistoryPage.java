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

public class DriverHistoryPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public DriverHistoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    @FindBy(how = How.CSS, using = "table.table")
    WebElement table;

    public void waitForPageLoad() {
        wait.until(ExpectedConditions.visibilityOf(table));
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("tbody tr.clickable-row")));
    }

    public List<WebElement> getTableRows() {
        wait.until(ExpectedConditions.visibilityOf(table));
        return driver.findElements(By.cssSelector("tbody tr.clickable-row"));
    }

    public int getRowCount() {
        return getTableRows().size();
    }

    private String getRowColumnText(int rowIndex, int columnIndex) {
        try {
            List<WebElement> rows = wait
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("tbody tr.clickable-row")));
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
        return getRowColumnText(rowIndex, 0);
    }

    /**
     * Find a ride by route string
     * 
     * @param routeString The route to search for
     * @return index of the ride, or -1 if not found
     */
    public int findRideByRoute(String routeString) {
        int rowCount = getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String route = getRoute(i);
            if (route != null && route.equals(routeString)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Click on a specific ride row to view details
     */
    public void clickRide(int rowIndex) {
        try {
            List<WebElement> rows = wait
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("tbody tr.clickable-row")));
            if (rowIndex >= rows.size()) {
                throw new IllegalArgumentException("Row index out of bounds");
            }

            WebElement row = rows.get(rowIndex);
            wait.until(ExpectedConditions.elementToBeClickable(row));
            row.click();

            // Wait for navigation to ride details page
            wait.until(ExpectedConditions.urlContains("/ride-details"));
        } catch (Exception e) {
            System.err.println("Error clicking ride: " + e.getMessage());
            throw e;
        }
    }
}
