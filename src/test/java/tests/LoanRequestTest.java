package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoanRequestTest {

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

        // Create a fresh customer for every test.
        createTestCustomer();

        // Navigate to Request Loan.
        wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.linkText("Request Loan")
                )
        ).click();

        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("rightPanel"),
                        "Apply for a Loan"
                )
        );
    }


    // LOAN-01:
    // Verify that a valid loan request is processed.
    @Test
    public void successfulLoanRequest() {

        driver.findElement(By.id("amount"))
                .sendKeys("100");

        driver.findElement(By.id("downPayment"))
                .sendKeys("10");

        driver.findElement(
                By.xpath("//input[@value='Apply Now']")
        ).click();

        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("rightPanel"),
                        "Loan Request Processed"
                )
        );

        String result =
                driver.findElement(By.id("rightPanel")).getText();

        assertTrue(
                result.contains("Loan Request Processed"),
                "Loan request was not processed"
        );

        assertTrue(
                result.contains("Approved"),
                "Expected the valid loan request to be approved"
        );
    }


    // LOAN-02:
    // Verify that a loan is denied when the down payment
    // exceeds the customer's available funds.
    @Test
    public void loanDeniedForInsufficientFunds() {

        driver.findElement(By.id("amount"))
                .sendKeys("10000");

        driver.findElement(By.id("downPayment"))
                .sendKeys("5000");

        driver.findElement(
                By.xpath("//input[@value='Apply Now']")
        ).click();

        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("rightPanel"),
                        "Loan Request Processed"
                )
        );

        String result =
                driver.findElement(By.id("rightPanel")).getText();

        assertTrue(
                result.contains("Denied"),
                "Expected loan request to be denied"
        );

        assertTrue(
                result.contains(
                        "You do not have sufficient funds for the given down payment."
                ),
                "Expected insufficient funds message was not displayed"
        );
    }


    // LOAN-03:
    // Verify application behaviour when loan amount
    // and down payment are left empty.
    @Test
    public void loanRequestWithEmptyValues() {

        // Submit without entering loan amount or down payment.
        driver.findElement(
                By.xpath("//input[@value='Apply Now']")
        ).click();

        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("rightPanel"),
                        "An internal error has occurred and has been logged."
                )
        );

        String result =
                driver.findElement(By.id("rightPanel")).getText();

        assertTrue(
                result.contains("Error!"),
                "Error heading was not displayed"
        );

        assertTrue(
                result.contains(
                        "An internal error has occurred and has been logged."
                ),
                "Expected error message was not displayed"
        );
    }


    // Create a new ParaBank customer so each test is independent.
    private void createTestCustomer() {

        driver.findElement(By.linkText("Register")).click();

        String username =
                "loan35_" +
                        UUID.randomUUID()
                                .toString()
                                .substring(0, 8);

        String password = "LoanTest35";

        driver.findElement(By.id("customer.firstName"))
                .sendKeys("Group");

        driver.findElement(By.name("customer.lastName"))
                .sendKeys("ThirtyFive");

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

        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("rightPanel"),
                        "Your account was created successfully"
                )
        );

        String registrationResult =
                driver.findElement(By.id("rightPanel")).getText();

        assertTrue(
                registrationResult.contains(
                        "Your account was created successfully"
                ),
                "Test customer could not be created"
        );
    }


    @AfterEach
    public void tearDown() {

        if (driver != null) {
            driver.quit();
        }
    }
}