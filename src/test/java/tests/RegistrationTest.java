package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegistrationTest {

    private WebDriver driver;

    @BeforeEach
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://parabank.parasoft.com/parabank/index.htm");
    }

    // REG-01: Verify successful registration with valid customer details
    @Test
    public void successfulUserRegistration() {

        driver.findElement(By.linkText("Register")).click();

        // Generate a unique username for every test run
        String username = "group35_" +
                UUID.randomUUID().toString().substring(0, 8);

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
                .sendKeys("123456789");

        // Enter account information
        driver.findElement(By.id("customer.username"))
                .sendKeys(username);

        driver.findElement(By.name("customer.password"))
                .sendKeys("Test1234");

        driver.findElement(By.id("repeatedPassword"))
                .sendKeys("Test1234");

        // Submit the registration form
        driver.findElement(
                By.xpath("//input[@value='Register']")
        ).click();

        // Verify the welcome heading
        String actualHeading = driver.findElement(
                By.cssSelector("#rightPanel h1")
        ).getText();

        assertEquals(
                "Welcome " + username,
                actualHeading,
                "The expected registration welcome heading was not displayed"
        );

        // Verify successful account creation message
        String pageText = driver.findElement(
                By.id("rightPanel")
        ).getText();

        assertTrue(
                pageText.contains("Your account was created successfully"),
                "The account creation success message was not displayed"
        );
    }

    // REG-02: Verify validation when required fields are empty
    @Test
    public void registrationWithEmptyRequiredFields() {

        driver.findElement(By.linkText("Register")).click();

        // Submit without entering registration information
        driver.findElement(
                By.xpath("//input[@value='Register']")
        ).click();

        String pageText = driver.findElement(
                By.id("rightPanel")
        ).getText();

        // Verify required-field validation messages
        assertTrue(
                pageText.contains("First name is required."),
                "First name validation message was not displayed"
        );

        assertTrue(
                pageText.contains("Last name is required."),
                "Last name validation message was not displayed"
        );

        assertTrue(
                pageText.contains("Address is required."),
                "Address validation message was not displayed"
        );

        assertTrue(
                pageText.contains("City is required."),
                "City validation message was not displayed"
        );

        assertTrue(
                pageText.contains("State is required."),
                "State validation message was not displayed"
        );

        assertTrue(
                pageText.contains("Zip Code is required."),
                "Zip Code validation message was not displayed"
        );

        assertTrue(
                pageText.contains("Social Security Number is required."),
                "SSN validation message was not displayed"
        );

        assertTrue(
                pageText.contains("Username is required."),
                "Username validation message was not displayed"
        );

        assertTrue(
                pageText.contains("Password is required."),
                "Password validation message was not displayed"
        );

        assertTrue(
                pageText.contains("Password confirmation is required."),
                "Password confirmation validation message was not displayed"
        );
    }

    // REG-03: Verify registration fails when passwords do not match
    @Test
    public void registrationWithMismatchedPasswords() {

        driver.findElement(By.linkText("Register")).click();

        String username = "group35_mismatch_" +
                UUID.randomUUID().toString().substring(0, 8);

        // Enter valid personal information
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
                .sendKeys("987654321");

        driver.findElement(By.id("customer.username"))
                .sendKeys(username);

        // Intentionally enter different passwords
        driver.findElement(By.name("customer.password"))
                .sendKeys("Test1234");

        driver.findElement(By.id("repeatedPassword"))
                .sendKeys("Test5678");

        // Submit the registration form
        driver.findElement(
                By.xpath("//input[@value='Register']")
        ).click();

        // Read validation result
        String pageText = driver.findElement(
                By.id("rightPanel")
        ).getText();

        // Verify the actual ParaBank password mismatch message
        assertTrue(
                pageText.contains("Passwords did not match."),
                "Password mismatch validation message was not displayed"
        );

        // Verify that registration did not proceed to a welcome page
        assertTrue(
                !pageText.contains("Welcome " + username),
                "User should not be registered when passwords do not match"
        );
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}