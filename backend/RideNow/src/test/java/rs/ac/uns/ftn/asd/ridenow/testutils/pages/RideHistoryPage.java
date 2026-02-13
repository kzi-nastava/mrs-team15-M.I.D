package rs.ac.uns.ftn.asd.ridenow.testutils.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RideHistoryPage {
    private WebDriver driver;
    private WebDriverWait wait;

    public RideHistoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    @FindBy(how = How.CSS, using = "table.table")
    WebElement table;

    @FindBy(how = How.CSS, using = "app-page-header input[type='date']")
    WebElement dateFilterInput;

    @FindBy(how = How.CSS, using = ".btn-primary")
    WebElement filterButton;

    @FindBy(how = How.CSS, using = ".btn-secondary")
    WebElement clearButton;

    @FindBy(how = How.CSS, using = "app-button#previous button")
    WebElement previousPageButton;

    @FindBy(how = How.CSS, using = "app-button#next button")
    WebElement nextPageButton;

    @FindBy(how = How.CSS, using = ".text-muted")
    WebElement paginationInfo;

    public List<WebElement> getTableRows() {
        wait.until(ExpectedConditions.visibilityOf(table));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("tbody tr.clickable-row")));
        return driver.findElements(By.cssSelector("tbody tr.clickable-row"));
    }

    public int getRowCount(){
        return getTableRows().size();
    }

    public String getRowColumnText(int rowIndex, int columnIndex) {
        try {
            List<WebElement> rows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("tbody tr.clickable-row")));
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

    public String getRoute(int rowIndex){
        return getRowColumnText(rowIndex, 0);
    }

    public String getStartTime(int rowIndex){
        return getRowColumnText(rowIndex, 1);
    }

    public String getEndTime(int rowIndex){
        return getRowColumnText(rowIndex, 2);
    }

    public boolean isCancelled(int rowIndex){
        String cancelledText = getRowColumnText(rowIndex, 3);
        return  cancelledText != null && !cancelledText.equals("N/A");
    }

    public int getCost(int rowIndex){
        String [] parts  = getRowColumnText(rowIndex, 4).split(" ");
        return Integer.parseInt(parts[0]);
    }

    public boolean hasPanic(int rowIndex) {
        String panicText = getRowColumnText(rowIndex, 5);
        return panicText != null && !panicText.equals("N/A");
    }

    public int getTotalRides() {
        Integer result = wait.until(driver -> {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector(".text-muted"));
                if (elements.isEmpty()) {
                    return 0;
                }
                String text = elements.get(0).getText();
                java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\((\\d+) total rides\\)").matcher(text);
                if (m.find()) {
                    return Integer.valueOf(m.group(1));
                }
                return 0;
            } catch (StaleElementReferenceException e) {
                return null;
            }
        });
        return (result != null) ? result : 0;
    }

    public boolean isNoResultsRowVisible() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-test='no-results-row']")));
        WebElement noResultsRow = driver.findElement(By.cssSelector("[data-test='no-results-row']"));
        return noResultsRow.isDisplayed() && noResultsRow.getText().trim().equals("No rides found");
    }

    public void filterByDate(String date) {
        wait.until(ExpectedConditions.visibilityOf(dateFilterInput));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].value = arguments[1];", dateFilterInput, date);
        js.executeScript("arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", dateFilterInput);
        js.executeScript("arguments[0].dispatchEvent(new Event('change', { bubbles: true }));", dateFilterInput);

        wait.until(ExpectedConditions.elementToBeClickable(filterButton)).click();
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("tbody tr.clickable-row")));
    }

    public boolean filterByDateWithNoResults(String futureDate) {
        wait.until(ExpectedConditions.visibilityOf(dateFilterInput));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].value = arguments[1];", dateFilterInput, futureDate);
        js.executeScript("arguments[0].dispatchEvent(new Event('input', { bubbles: true }));", dateFilterInput);
        js.executeScript("arguments[0].dispatchEvent(new Event('change', { bubbles: true }));", dateFilterInput);

        wait.until(ExpectedConditions.elementToBeClickable(filterButton)).click();

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("text-muted")));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean validateDateFilter(String expectedDate) {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".text-muted")));
        int total = getTotalRides();
        if (total == 0) {
            return isNoResultsRowVisible();
        }
        goToFirstPage();
        List<String> startTimes = getAllStartTimes();
        for (String time : startTimes) {
            if (!time.contains(expectedDate)) {
                return false;
            }
        }
        return true;
    }

    public void sortByColumn(String columnName) {
        WebElement header = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//th[contains(text(), '" + columnName + "')]")));
        String iconBefore = getSortIcon(columnName);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", header);
        wait.until(driver -> !getSortIcon(columnName).equals(iconBefore));
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("tbody tr.clickable-row")));
    }

    public String getSortIcon(String columnName){
        WebElement header = driver.findElement(By.xpath("//th[contains(text(), '" + columnName + "')]"));
        WebElement sortIcon = header.findElement(By.className("sort-icon"));
        return sortIcon.getText().trim();
    }

    public boolean isFirstPage() {
        return previousPageButton.getAttribute("disabled") != null;
    }

    public boolean isLastPage() {
        return nextPageButton.getAttribute("disabled") != null;
    }

    public void goToNextPage() {
        if (!isLastPage()) {
            String currentPagination = paginationInfo.getText();
            wait.until(ExpectedConditions.elementToBeClickable(nextPageButton));
            nextPageButton.click();
            wait.until(driver -> !paginationInfo.getText().equals(currentPagination));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("tbody tr.clickable-row")));
        }
    }

    public void goToPreviousPage() {
        if (!isFirstPage()) {
            String currentPagination = paginationInfo.getText();
            wait.until(ExpectedConditions.elementToBeClickable(previousPageButton));
            previousPageButton.click();
            wait.until(driver -> !paginationInfo.getText().equals(currentPagination));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("tbody tr.clickable-row")));
        }
    }

    public void goToFirstPage() {
        while (!isFirstPage()) {
            goToPreviousPage();
        }
    }

    public List<String> getAllStartTimes() {
        List<String> allStartTimes = new ArrayList<>();
        goToFirstPage();
        int totalPages = 0;
        do {
            String firstItemThisPage = getRowCount() > 0 ? getStartTime(0) : "";
            int rowCount = getRowCount();
            for (int i = 0; i < rowCount; i++) {
                allStartTimes.add(getStartTime(i));
            }
            if (!isLastPage() && totalPages < 20) {
                goToNextPage();
                final String previousFirstItem = firstItemThisPage;
                wait.until(driver -> {
                    String currentFirstItem = getStartTime(0);
                    return !currentFirstItem.equals(previousFirstItem);
                });
                totalPages++;
            } else {
                break;
            }
        } while (true);
        return allStartTimes;
    }

    public List<String> getAllEndTimes() {
        List<String> allEndTimes = new ArrayList<>();
        goToFirstPage();
        int totalPages = 0;
        do {
            String firstItemThisPage = getRowCount() > 0 ? getEndTime(0) : "";
            int rowCount = getRowCount();
            for (int i = 0; i < rowCount; i++) {
                allEndTimes.add(getEndTime(i));
            }
            if (!isLastPage() && totalPages < 20) {
                goToNextPage();
                final String previousFirstItem = firstItemThisPage;
                wait.until(driver -> {
                    String currentFirstItem = getEndTime(0);
                    return !currentFirstItem.equals(previousFirstItem);
                });
                totalPages++;
            } else {
                break;
            }
        } while (true);
        return allEndTimes;
    }

    public List<Integer> getAllCosts() {
        List<Integer> allCosts = new ArrayList<>();
        goToFirstPage();
        int totalPages = 0;
        do {
            Integer firstItemThisPage = getRowCount() > 0 ? getCost(0) : null;
            int rowCount = getRowCount();
            for (int i = 0; i < rowCount; i++) {
                allCosts.add(getCost(i));
            }
            if (!isLastPage() && totalPages < 20) {
                goToNextPage();
                final Integer previousFirstItem = firstItemThisPage;
                wait.until(driver -> {
                    if (getRowCount() == 0) {
                        return false;
                    };
                    Integer currentFirstItem = getCost(0);
                    return !currentFirstItem.equals(previousFirstItem);
                });
                totalPages++;
            } else {
                break;
            }
        } while (true);
        return allCosts;
    }

    public List<String> getAllRoutes() {
        List<String> allRoutes = new ArrayList<>();
        goToFirstPage();
        int totalPages = 0;
        do {
            String firstItemThisPage = getRowCount() > 0 ? getRoute(0) : "";
            int rowCount = getRowCount();
            for (int i = 0; i < rowCount; i++) {
                allRoutes.add(getRoute(i));
            }
            if (!isLastPage() && totalPages < 20) {
                goToNextPage();
                final String previousFirstItem = firstItemThisPage;
                wait.until(driver -> {
                    if (getRowCount() == 0){
                        return false;
                    }
                    String currentFirstItem = getRoute(0);
                    return !currentFirstItem.equals(previousFirstItem);
                });
                totalPages++;
            } else {
                break;
            }
        } while (true);
        return allRoutes;
    }

    public List<Boolean> getAllCancelledStatuses() {
        List<Boolean> statuses = new ArrayList<>();
        goToFirstPage();

        while (true) {
            String currentPagination = paginationInfo.getText();
            int rowCount = getRowCount();
            for (int i = 0; i < rowCount; i++) {
                statuses.add(isCancelled(i));
            }
            if (isLastPage()) {
                break;
            }
            nextPageButton.click();
            wait.until(driver -> !paginationInfo.getText().equals(currentPagination));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("tbody tr.clickable-row")));
        }
        return statuses;
    }

    public List<Boolean> getAllPanicStatuses() {
        List<Boolean> statuses = new ArrayList<>();
        goToFirstPage();
        int totalPages = 0;
        do {
            String firstItemThisPage = getRowCount() > 0 ? getStartTime(0) : "";
            int rowCount = getRowCount();
            for (int i = 0; i < rowCount; i++) {
                statuses.add(hasPanic(i));
            }
            if (!isLastPage() && totalPages < 20) {
                goToNextPage();
                final String previousFirstItem = firstItemThisPage;
                wait.until(driver -> !getStartTime(0).equals(previousFirstItem));
                totalPages++;
            } else break;
        } while (true);
        return statuses;
    }

    public boolean isBooleanSortedAscending(List<Boolean> values) {
        for (int i = 0; i < values.size() - 1; i++) {
            if (values.get(i) && !values.get(i + 1)) {
                return false;
            }
        }
        return true;
    }

    public boolean isBooleanSortedDescending(List<Boolean> values) {
        for (int i = 0; i < values.size() - 1; i++) {
            if (!values.get(i) && values.get(i + 1)) {
                return false;
            }
        }
        return true;
    }

    public boolean isDateTimeSortedAscending(List<String> dateTimes) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm");
        for (int i = 0; i < dateTimes.size() - 1; i++) {
            LocalDateTime current = LocalDateTime.parse(dateTimes.get(i), formatter);
            LocalDateTime next = LocalDateTime.parse(dateTimes.get(i + 1), formatter);
            if (current.isAfter(next)) {
                return false;
            }
        }
        return true;
    }

    public boolean isDateTimeSortedDescending(List<String> dateTimes) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm");
        for (int i = 0; i < dateTimes.size() - 1; i++) {
            LocalDateTime current = LocalDateTime.parse(dateTimes.get(i), formatter);
            LocalDateTime next = LocalDateTime.parse(dateTimes.get(i + 1), formatter);
            if (current.isBefore(next)) {
                return false;
            }
        }
        return true;
    }

    public boolean isIntegerSortedAscending(List<Integer> values) {
        for (int i = 0; i < values.size() - 1; i++) {
            if (values.get(i) > values.get(i + 1)) {
                return false;
            }
        }
        return true;
    }

    public boolean isIntegerSortedDescending(List<Integer> values) {
        for (int i = 0; i < values.size() - 1; i++) {
            if (values.get(i) < values.get(i + 1)) {
                return false;
            }
        }
        return true;
    }

    public boolean isStringSortedAscending(List<String> values) {
        for (int i = 0; i < values.size() - 1; i++) {
            if (values.get(i).compareToIgnoreCase(values.get(i + 1)) > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isStringSortedDescending(List<String> values) {
        for (int i = 0; i < values.size() - 1; i++) {
            if (values.get(i).compareToIgnoreCase(values.get(i + 1)) < 0) {
                return false;
            }
        }
        return true;
    }
}