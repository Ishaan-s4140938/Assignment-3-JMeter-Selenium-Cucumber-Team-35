package tests;

import com.inflectra.spiratest.addons.junitextension.SpiraExtension;
import com.inflectra.spiratest.addons.junitextension.SpiraTestCase;
import com.inflectra.spiratest.addons.junitextension.SpiraTestConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpiraExtension.class)
@SpiraTestConfiguration(
        url = "https://rmit.spiraservice.net",
        login = "S4151762",
        password = "",
        projectId = 1151
)

public class FindTransactionsTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {

        driver = new ChromeDriver();
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get(
                "https://parabank.parasoft.com/parabank/index.htm"
        );

        // Create a fresh customer so each test is independent
        driver.findElement(By.linkText("Register")).click();

        String username =
                "find35_" +
                        UUID.randomUUID().toString().substring(0, 8);

        String password = "FindTest35";

        driver.findElement(By.id("customer.firstName"))
                .sendKeys("Test");

        driver.findElement(By.name("customer.lastName"))
                .sendKeys("Customer");

        driver.findElement(By.id("customer.address.street"))
                .sendKeys("35 Test Street");

        driver.findElement(By.name("customer.address.city"))
                .sendKeys("Melbourne");

        driver.findElement(By.id("customer.address.state"))
                .sendKeys("Victoria");

        driver.findElement(By.name("customer.address.zipCode"))
                .sendKeys("3000");

        driver.findElement(By.id("customer.phoneNumber"))
                .sendKeys("0400000035");

        driver.findElement(By.name("customer.ssn"))
                .sendKeys("357913579");

        driver.findElement(By.id("customer.username"))
                .sendKeys(username);

        driver.findElement(By.name("customer.password"))
                .sendKeys(password);

        driver.findElement(By.id("repeatedPassword"))
                .sendKeys(password);

        driver.findElement(
                By.xpath("//input[@value='Register']")
        ).click();

        // Wait until registration completes
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.linkText("Find Transactions")
                )
        );
    }


    // FIND-01
    // Verify transaction search by amount
    @SpiraTestCase(testCaseId = 54314)
    @Test
    public void findTransactionByAmount() {

        driver.findElement(By.linkText("Find Transactions"))
                .click();

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("amount")
                )
        ).sendKeys("100");

        driver.findElement(By.id("findByAmount"))
                .click();

        wait.until(
                ExpectedConditions.or(
                        ExpectedConditions.textToBePresentInElementLocated(
                                By.id("rightPanel"),
                                "Transaction Results"
                        ),
                        ExpectedConditions.textToBePresentInElementLocated(
                                By.id("rightPanel"),
                                "No transactions found"
                        )
                )
        );

        String result =
                driver.findElement(By.id("rightPanel")).getText();

        assertTrue(
                result.contains("Transaction Results")
                        || result.contains("No transactions found"),
                "Transaction search by amount did not complete"
        );
    }


    // FIND-02
    // Verify transaction search by date
    @SpiraTestCase(testCaseId = 54315)
    @Test
    public void findTransactionByDate() {

        driver.findElement(By.linkText("Find Transactions"))
                .click();

        String today =
                LocalDate.now().format(
                        DateTimeFormatter.ofPattern("MM-dd-yyyy")
                );

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("transactionDate")
                )
        ).sendKeys(today);

        driver.findElement(By.id("findByDate"))
                .click();

        wait.until(
                ExpectedConditions.or(
                        ExpectedConditions.textToBePresentInElementLocated(
                                By.id("rightPanel"),
                                "Transaction Results"
                        ),
                        ExpectedConditions.textToBePresentInElementLocated(
                                By.id("rightPanel"),
                                "No transactions found"
                        )
                )
        );

        String result =
                driver.findElement(By.id("rightPanel")).getText();

        assertTrue(
                result.contains("Transaction Results")
                        || result.contains("No transactions found"),
                "Transaction search by date did not complete"
        );
    }


    // FIND-03
    // Verify transaction search using a date range
    @SpiraTestCase(testCaseId = 54316)
    @Test
    public void findTransactionByDateRange() {

        driver.findElement(By.linkText("Find Transactions"))
                .click();

        LocalDate today = LocalDate.now();

        String fromDate =
                today.minusDays(1).format(
                        DateTimeFormatter.ofPattern("MM-dd-yyyy")
                );

        String toDate =
                today.plusDays(1).format(
                        DateTimeFormatter.ofPattern("MM-dd-yyyy")
                );

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("fromDate")
                )
        ).sendKeys(fromDate);

        driver.findElement(By.id("toDate"))
                .sendKeys(toDate);

        driver.findElement(By.id("findByDateRange"))
                .click();

        wait.until(
                ExpectedConditions.or(
                        ExpectedConditions.textToBePresentInElementLocated(
                                By.id("rightPanel"),
                                "Transaction Results"
                        ),
                        ExpectedConditions.textToBePresentInElementLocated(
                                By.id("rightPanel"),
                                "No transactions found"
                        )
                )
        );

        String result =
                driver.findElement(By.id("rightPanel")).getText();

        assertTrue(
                result.contains("Transaction Results")
                        || result.contains("No transactions found"),
                "Transaction search by date range did not complete"
        );
    }


    // FIND-04:
// Verify validation when an invalid transaction ID is entered
    @SpiraTestCase(testCaseId = 54318)
    @Test
    public void findTransactionWithInvalidId() {

        driver.findElement(By.linkText("Find Transactions"))
                .click();

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("transactionId")
                )
        ).sendKeys("INVALID");

        driver.findElement(By.id("findById"))
                .click();

        String errorMessage = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("transactionIdError")
                )
        ).getText();

        assertTrue(
                errorMessage.contains("Invalid transaction ID"),
                "Invalid transaction ID validation message was not displayed"
        );
    }


    @AfterEach
    public void tearDown() {

        if (driver != null) {
            driver.quit();
        }
    }
}