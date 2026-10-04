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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BillPayTest {

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

        // Create a fresh customer for each test.
        // This keeps the Bill Pay tests independent.
        createTestCustomer();

        // Open the Bill Pay page.
        wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.linkText("Bill Pay")
                )
        ).click();

        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("rightPanel"),
                        "Bill Payment Service"
                )
        );
    }


    // BILLPAY-01:
    // Verify that a bill payment can be made using valid details.
    @Test
    public void successfulBillPayment() {

        String payeeName = "Electricity Group35";
        String amount = "25.00";

        enterPayeeDetails(payeeName);

        driver.findElement(By.name("payee.accountNumber"))
                .sendKeys("12345678");

        driver.findElement(By.name("verifyAccount"))
                .sendKeys("12345678");

        driver.findElement(By.name("amount"))
                .sendKeys(amount);

        driver.findElement(
                By.xpath("//input[@value='Send Payment']")
        ).click();

        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("rightPanel"),
                        "Bill Payment Complete"
                )
        );

        String result =
                driver.findElement(By.id("rightPanel")).getText();

        assertTrue(
                result.contains("Bill Payment Complete"),
                "Bill Payment Complete message was not displayed"
        );

        assertTrue(
                result.contains(payeeName),
                "The payee name was not displayed in the payment result"
        );

        assertTrue(
                result.contains(amount),
                "The payment amount was not displayed in the payment result"
        );
    }


    // BILLPAY-02:
    // Verify validation when account numbers are not provided.
    @Test
    public void billPaymentWithMissingAccountNumbers() {

        enterPayeeDetails("Electricity Group35");

        driver.findElement(By.name("amount"))
                .sendKeys("25.00");

        driver.findElement(
                By.xpath("//input[@value='Send Payment']")
        ).click();

        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("rightPanel"),
                        "Account number is required."
                )
        );

        String pageText =
                driver.findElement(By.id("rightPanel")).getText();

        assertTrue(
                pageText.contains("Account number is required."),
                "Required account number validation was not displayed"
        );

        // Both account number fields should produce the validation.
        int errorCount =
                driver.findElements(
                        By.xpath(
                                "//*[contains(text(),'Account number is required.')]"
                        )
                ).size();

        assertEquals(
                2,
                errorCount,
                "Expected validation messages for both account number fields"
        );
    }


    // BILLPAY-03:
    // Verify validation when Account # and Verify Account # do not match.
    @Test
    public void billPaymentWithMismatchedAccountNumbers() {

        enterPayeeDetails("Electricity Group35");

        driver.findElement(By.name("payee.accountNumber"))
                .sendKeys("0001");

        driver.findElement(By.name("verifyAccount"))
                .sendKeys("0002");

        driver.findElement(By.name("amount"))
                .sendKeys("25.00");

        driver.findElement(
                By.xpath("//input[@value='Send Payment']")
        ).click();

        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("rightPanel"),
                        "The account numbers do not match."
                )
        );

        String pageText =
                driver.findElement(By.id("rightPanel")).getText();

        assertTrue(
                pageText.contains(
                        "The account numbers do not match."
                ),
                "Account number mismatch validation was not displayed"
        );
    }


    // Enter common valid payee information.
    private void enterPayeeDetails(String payeeName) {

        driver.findElement(By.name("payee.name"))
                .sendKeys(payeeName);

        driver.findElement(By.name("payee.address.street"))
                .sendKeys("10 Test Street");

        driver.findElement(By.name("payee.address.city"))
                .sendKeys("Melbourne");

        driver.findElement(By.name("payee.address.state"))
                .sendKeys("Victoria");

        driver.findElement(By.name("payee.address.zipCode"))
                .sendKeys("3000");

        driver.findElement(By.name("payee.phoneNumber"))
                .sendKeys("0400000035");
    }


    // Create a new ParaBank customer so each test can run independently.
    private void createTestCustomer() {

        driver.findElement(By.linkText("Register")).click();

        String username =
                "bill35_" +
                        UUID.randomUUID()
                                .toString()
                                .substring(0, 8);

        String password = "BillTest35";

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