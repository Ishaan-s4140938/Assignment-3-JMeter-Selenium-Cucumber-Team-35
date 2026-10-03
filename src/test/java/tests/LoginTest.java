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

public class LoginTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {

        driver = new ChromeDriver();
        driver.manage().window().maximize();

        // Explicit wait used for elements that change after page submission
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get(
                "https://parabank.parasoft.com/parabank/index.htm"
        );
    }

    // LOGIN-01:
    // Verify login using a valid username and password
    @Test
    public void successfulLoginWithValidCredentials() {

        // Create a new customer so this test can run independently
        driver.findElement(By.linkText("Register")).click();

        String username =
                "login35_" +
                        UUID.randomUUID().toString().substring(0, 8);

        String password = "LoginTest35";

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

        // Wait until successful account creation is displayed
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
                "Test account could not be created before login"
        );

        // Log out after creating the account
        wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.linkText("Log Out")
                )
        ).click();

        // Enter the valid credentials
        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.name("username")
                )
        ).sendKeys(username);

        driver.findElement(By.name("password"))
                .sendKeys(password);

        driver.findElement(
                By.xpath("//input[@value='Log In']")
        ).click();

        // Wait for Accounts Overview after successful login
        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("rightPanel"),
                        "Accounts Overview"
                )
        );

        String accountPage =
                driver.findElement(By.id("rightPanel")).getText();

        assertTrue(
                accountPage.contains("Accounts Overview"),
                "Accounts Overview was not displayed after valid login"
        );

        // Verify that logged-in user has a Log Out option
        assertTrue(
                wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.linkText("Log Out")
                        )
                ).isDisplayed(),
                "Log Out link was not displayed after valid login"
        );
    }

    // LOGIN-02:
    // Verify login is rejected for invalid credentials
    @Test
    public void loginWithInvalidCredentials() {

        driver.findElement(By.name("username"))
                .sendKeys("invalid_group35_user");

        driver.findElement(By.name("password"))
                .sendKeys("WrongPass35");

        driver.findElement(
                By.xpath("//input[@value='Log In']")
        ).click();

        // Wait for the actual ParaBank error response
        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("rightPanel"),
                        "The username and password could not be verified."
                )
        );

        String pageText =
                driver.findElement(By.id("rightPanel")).getText();

        assertTrue(
                pageText.contains(
                        "The username and password could not be verified."
                ),
                "Expected invalid credentials message was not displayed"
        );

        assertTrue(
                pageText.contains("Error!"),
                "Error heading was not displayed for invalid login"
        );
    }

    // LOGIN-03:
    // Verify validation when username and password are empty
    @Test
    public void loginWithEmptyCredentials() {

        // Submit the login form without entering credentials
        driver.findElement(
                By.xpath("//input[@value='Log In']")
        ).click();

        // Wait until ParaBank finishes updating the result panel
        wait.until(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("rightPanel"),
                        "Please enter a username and password."
                )
        );

        // Locate rightPanel AFTER the page update
        String pageText =
                driver.findElement(By.id("rightPanel")).getText();

        assertTrue(
                pageText.contains(
                        "Please enter a username and password."
                ),
                "Expected empty credentials message was not displayed"
        );

        assertTrue(
                pageText.contains("Error!"),
                "Error heading was not displayed for empty login"
        );
    }

    @AfterEach
    public void tearDown() {

        if (driver != null) {
            driver.quit();
        }
    }
}