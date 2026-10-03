package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenNewAccountTest {

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

        // Create a new customer for each test
        // This allows every test to run independently
        driver.findElement(By.linkText("Register")).click();

        String username =
                "account35_" +
                        UUID.randomUUID().toString().substring(0, 8);

        String password = "AccountTest35";

        // Enter personal information
        driver.findElement(By.id("customer.firstName"))
                .sendKeys("Srinath");

        driver.findElement(By.name("customer.lastName"))
                .sendKeys("Krothapalli");

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

        // Enter account information
        driver.findElement(By.id("customer.username"))
                .sendKeys(username);

        driver.findElement(By.name("customer.password"))
                .sendKeys(password);

        driver.findElement(By.id("repeatedPassword"))
                .sendKeys(password);

        // Submit registration
        driver.findElement(
                By.xpath("//input[@value='Register']")
        ).click();

        // Wait until registration is successful
        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("rightPanel"),
                        "Your account was created successfully"
                )
        );
    }

    // ACC-01:
    // Verify that a new CHECKING account can be opened
    @Test
    public void openCheckingAccount() {

        driver.findElement(
                By.linkText("Open New Account")
        ).click();

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("type")
                )
        );

        // Select CHECKING account
        Select accountType =
                new Select(driver.findElement(By.id("type")));

        accountType.selectByVisibleText("CHECKING");

        // Open the new account
        driver.findElement(
                By.xpath("//input[@value='Open New Account']")
        ).click();

        // Wait for successful account creation
        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("openAccountResult"),
                        "Account Opened!"
                )
        );

        String result =
                driver.findElement(
                        By.id("openAccountResult")
                ).getText();

        assertTrue(
                result.contains("Account Opened!"),
                "Checking account was not opened successfully"
        );

        assertTrue(
                result.contains(
                        "Congratulations, your account is now open."
                ),
                "Account confirmation message was not displayed"
        );
    }

    // ACC-02:
    // Verify that a new SAVINGS account can be opened
    @Test
    public void openSavingsAccount() {

        driver.findElement(
                By.linkText("Open New Account")
        ).click();

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("type")
                )
        );

        // Select SAVINGS account
        Select accountType =
                new Select(driver.findElement(By.id("type")));

        accountType.selectByVisibleText("SAVINGS");

        // Open the new account
        driver.findElement(
                By.xpath("//input[@value='Open New Account']")
        ).click();

        // Wait for successful account creation
        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("openAccountResult"),
                        "Account Opened!"
                )
        );

        String result =
                driver.findElement(
                        By.id("openAccountResult")
                ).getText();

        assertTrue(
                result.contains("Account Opened!"),
                "Savings account was not opened successfully"
        );

        assertTrue(
                result.contains(
                        "Congratulations, your account is now open."
                ),
                "Account confirmation message was not displayed"
        );
    }

    // ACC-03:
    // Verify that a new account number is displayed
    @Test
    public void verifyNewAccountNumberIsDisplayed() {

        driver.findElement(
                By.linkText("Open New Account")
        ).click();

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("type")
                )
        );

        Select accountType =
                new Select(driver.findElement(By.id("type")));

        accountType.selectByVisibleText("CHECKING");

        driver.findElement(
                By.xpath("//input[@value='Open New Account']")
        ).click();

        // Wait until the new account result is displayed
        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("openAccountResult"),
                        "Your new account number:"
                )
        );

        String result =
                driver.findElement(
                        By.id("openAccountResult")
                ).getText();

        assertTrue(
                result.contains("Your new account number:"),
                "New account number was not displayed"
        );
    }

    @AfterEach
    public void tearDown() {

        if (driver != null) {
            driver.quit();
        }
    }
}