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
import org.openqa.selenium.JavascriptExecutor;

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

    /**
     * Click the favorite/star icon for the ride matching the provided route (pickup/destination/stops)
     * and confirm the Add modal.
     */
    public void toggleFavorite(String pickup, String destination, List<String> stops) {
        try {
            List<WebElement> rows = wait
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("tbody tr")));

            int matchIndex = -1;
            for (int i = 0; i < rows.size(); i++) {
                String routeText = getRowColumnText(i, 1); // column 1 is route
                if (routeText == null) continue;
                if (routeText.contains(pickup) && routeText.contains(destination)) {
                    int pickIdx = routeText.indexOf(pickup);
                    int destIdx = routeText.indexOf(destination);
                    // ensure pickup appears before destination in the displayed route (avoid reversed-route match)
                    if (pickIdx != -1 && destIdx != -1 && pickIdx < destIdx) {
                        matchIndex = i;
                        break;
                    }
                }
            }

            if (matchIndex == -1) {
                throw new IllegalStateException("No history row matching provided route was found");
            }

            WebElement favIcon = rows.get(matchIndex).findElement(By.cssSelector("td.fav-cell i.bi"));
            wait.until(ExpectedConditions.elementToBeClickable(favIcon));

            // Use JS click to avoid interception by row click handlers
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", favIcon);

            // If add-favorite modal appears, validate stops then click Add
            WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[@id='addFavoriteModal']//app-button[.//button[contains(text(), 'Add')]]//button")
            ));

            // Validate stop addresses in modal match provided stops
            List<WebElement> stopElems = driver.findElements(By.cssSelector("#addFavoriteModal .route-info ul li"));
            if (stops == null || stops.isEmpty()) {
                if (!stopElems.isEmpty()) {
                    throw new IllegalStateException("Expected no stops in add-favorite modal, but some were present");
                }
            } else {
                if (stopElems.size() != stops.size()) {
                    throw new IllegalStateException("Stop count in add-favorite modal does not match expected stops");
                }
                for (int si = 0; si < stops.size(); si++) {
                    String modalStop = stopElems.get(si).getText().trim();
                    if (!modalStop.equals(stops.get(si))) {
                        throw new IllegalStateException("Stop address mismatch at index " + si + ": expected='" + stops.get(si) + "' actual='" + modalStop + "'");
                    }
                }
            }

            addButton.click();

            // Wait until the icon reflects favorite state (filled star)
            WebElement iconAfter = rows.get(matchIndex).findElement(By.cssSelector("td.fav-cell i.bi"));
            wait.until(ExpectedConditions.attributeContains(iconAfter, "class", "bi-star-fill"));

        } catch (Exception e) {
            System.err.println("Error toggling favorite for route " + pickup + " -> " + destination + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * Remove favorite for the ride matching provided route (click filled star and wait until unfilled)
     */
    public void removeFavorite(String pickup, String destination) {
        try {
            // Click the favorite icon for the matching row (ensure pickup before destination)
            List<WebElement> rows = wait
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("tbody tr")));

            int matchIndex = -1;
            for (int i = 0; i < rows.size(); i++) {
                String routeText = getRowColumnText(i, 1);
                if (routeText == null) continue;
                if (routeText.contains(pickup) && routeText.contains(destination)) {
                    int pickIdx = routeText.indexOf(pickup);
                    int destIdx = routeText.indexOf(destination);
                    if (pickIdx != -1 && destIdx != -1 && pickIdx < destIdx) {
                        matchIndex = i;
                        break;
                    }
                }
            }

            if (matchIndex == -1) {
                throw new IllegalStateException("No history row matching provided route was found for removal");
            }

            WebElement favIcon = rows.get(matchIndex).findElement(By.cssSelector("td.fav-cell i.bi"));
            wait.until(ExpectedConditions.elementToBeClickable(favIcon));
            // Click to open remove modal (frontend opens modal when favorite is true)
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", favIcon);

            // Wait for Remove button in the modal and click it
            WebElement removeButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//div[@id='removeFavoriteModal']//app-button[.//button[contains(text(), 'Remove')]]//button")
            ));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", removeButton);

            // capture index for lambda (must be effectively final)
            final int idx = matchIndex;

            // Wait until the icon no longer has the filled-star class. Re-query the table each poll to avoid stale references.
            wait.until(drv -> {
                try {
                    List<WebElement> currentRows = ((WebDriver) drv).findElements(By.cssSelector("tbody tr"));
                    if (idx >= currentRows.size()) {
                        // row disappeared or table changed; consider favorite removed
                        return true;
                    }
                    WebElement icon = currentRows.get(idx).findElement(By.cssSelector("td.fav-cell i.bi"));
                    String cls = icon.getAttribute("class");
                    return cls == null || !cls.contains("bi-star-fill");
                } catch (StaleElementReferenceException e) {
                    // DOM changed — retry until timeout
                    return false;
                } catch (Exception e) {
                    return false;
                }
            });

        } catch (Exception e) {
            System.err.println("Error removing favorite for route " + pickup + " -> " + destination + ": " + e.getMessage());
            throw e;
        }
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
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("tbody tr")));
            if (rowIndex >= rows.size()) {
                throw new IllegalArgumentException("Row index out of bounds");
            }

            WebElement row = rows.get(rowIndex);
            wait.until(ExpectedConditions.elementToBeClickable(row));
            row.click();

            // Wait for navigation to ride details page
            wait.until(ExpectedConditions.urlContains("/history-ride-details"));
        } catch (Exception e) {
            System.err.println("Error clicking ride: " + e.getMessage());
            throw e;
        }
    }
}
