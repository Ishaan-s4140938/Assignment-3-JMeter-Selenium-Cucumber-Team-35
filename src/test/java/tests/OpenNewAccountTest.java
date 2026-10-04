package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenNewAccountTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {

        driver = new ChromeDriver();
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        driver.get(
                "https://parabank.parasoft.com/parabank/index.htm"
        );

        // Register a new customer for each test
        wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.linkText("Register")
                )
        ).click();

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

        openAccountPage();

        Select accountType =
                new Select(
                        wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                        By.id("type")
                                )
                        )
                );

        accountType.selectByVisibleText("CHECKING");

        openAccountAndWaitForSuccess();

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

        openAccountPage();

        Select accountType =
                new Select(
                        wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                        By.id("type")
                                )
                        )
                );

        accountType.selectByVisibleText("SAVINGS");

        openAccountAndWaitForSuccess();

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

        openAccountPage();

        Select accountType =
                new Select(
                        wait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                        By.id("type")
                                )
                        )
                );

        accountType.selectByVisibleText("CHECKING");

        openAccountAndWaitForSuccess();

        // Get the dynamically generated account number
        WebElement accountNumber =
                wait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.id("newAccountId")
                        )
                );

        String newAccountNumber =
                accountNumber.getText().trim();

        assertFalse(
                newAccountNumber.isEmpty(),
                "New account number was not displayed"
        );

        assertTrue(
                newAccountNumber.matches("\\d+"),
                "Displayed account number is not valid"
        );
    }


    // Navigate to the Open New Account page
    private void openAccountPage() {

        wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.linkText("Open New Account")
                )
        ).click();

        wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.id("type")
                )
        );
    }


    // Click the Open New Account button and wait for success.
    // ParaBank may occasionally remain on the form after the first click,
    // so one additional click is attempted if required.
    private void openAccountAndWaitForSuccess() {

        By openButton =
                By.xpath("//input[@value='Open New Account']");

        // First attempt
        wait.until(
                ExpectedConditions.elementToBeClickable(
                        openButton
                )
        ).click();

        WebDriverWait shortWait =
                new WebDriverWait(
                        driver,
                        Duration.ofSeconds(5)
                );

        try {

            shortWait.until(
                    ExpectedConditions.textToBePresentInElementLocated(
                            By.id("openAccountResult"),
                            "Account Opened!"
                    )
            );

        } catch (Exception firstAttemptException) {

            // ParaBank remained on the form.
            // Retry the account-opening action once.
            wait.until(
                    ExpectedConditions.elementToBeClickable(
                            openButton
                    )
            ).click();

            wait.until(
                    ExpectedConditions.textToBePresentInElementLocated(
                            By.id("openAccountResult"),
                            "Account Opened!"
                    )
            );
        }
    }


    @AfterEach
    public void tearDown() {

        if (driver != null) {
            driver.quit();
        }
    }
}