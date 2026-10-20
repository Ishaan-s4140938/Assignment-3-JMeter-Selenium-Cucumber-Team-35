package group35;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class BankSteps {
    private WebDriver driver;
    private WebDriverWait wait;
    private Scenario scenario;
    private String first, second, amount, payee;
    private BigDecimal firstBefore, secondBefore;
    private Set<String> accountsBefore;
    private final String base = System.getProperty("parabank.url", "https://parabank.parasoft.com/parabank/").replaceAll("/index\\.htm$", "").replaceAll("/+$", "") + "/";

    @Before
    public void startBrowser(Scenario scenario) {
        this.scenario = scenario;
        ChromeOptions options = new ChromeOptions();
        if (System.getProperty("chrome.binary") != null) options.setBinary(System.getProperty("chrome.binary"));
        options.addArguments("--window-size=1440,1000");
        if (Boolean.getBoolean("headless")) options.addArguments("--headless=new");
        if (Boolean.getBoolean("test.restrictedEnvironment")) options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(45));
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    @After
    public void finish(Scenario scenario) {
        try {
            if (driver != null && scenario.isFailed()) scenario.log(driver.findElement(By.tagName("body")).getText());
            if (driver != null) scenario.attach(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES),
                    "image/png", scenario.isFailed() ? "Failure evidence" : "Verified outcome");
        } catch (WebDriverException e) {
            scenario.log("Browser evidence unavailable: " + e.getClass().getSimpleName());
        } finally {
            if (driver != null) driver.quit();
        }
    }

    private void evidence(String name) {
        scenario.attach(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES), "image/png", name);
    }

    private WebElement visible(By locator) { return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)); }
    private String text(String id) { return visible(By.id(id)).getText().trim(); }
    private void fill(By locator, String value) {
        WebElement field = visible(locator);
        field.clear();
        if (!value.isEmpty()) field.sendKeys(value);
    }
    private void click(By locator) { wait.until(ExpectedConditions.elementToBeClickable(locator)).click(); }
    private boolean shown(By locator) { return driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed); }
    private void page(String path) { driver.get(base + path); }
    private void choose(By locator, String value) {
        wait.until(d -> d.findElements(locator).size() == 1 &&
                new Select(d.findElement(locator)).getOptions().stream().anyMatch(o -> value.equals(o.getAttribute("value"))));
        new Select(driver.findElement(locator)).selectByValue(value);
    }
    private BigDecimal money(String value) {
        return new BigDecimal(value.replace("$", "").replace(",", "").trim());
    }
    private void equalMoney(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual), "Expected balance " + expected + " but was " + actual);
    }
    private BigDecimal balance(String account) {
        page("activity.htm?id=" + account);
        wait.until(ExpectedConditions.textToBe(By.id("accountId"), account));
        wait.until(d -> !d.findElement(By.id("balance")).getText().isBlank());
        return money(text("balance"));
    }
    private Set<String> accounts() {
        page("overview.htm");
        wait.until(d -> !d.findElements(By.cssSelector("#accountTable tbody a")).isEmpty()
                || shown(By.cssSelector("#rightPanel .error")));
        assertFalse(shown(By.cssSelector("#rightPanel .error")),
                "ParaBank account setup is unavailable: " + driver.findElement(By.id("rightPanel")).getText());
        Set<String> result = new LinkedHashSet<>();
        driver.findElements(By.cssSelector("#accountTable tbody a")).forEach(a -> result.add(a.getText().trim()));
        return result;
    }

    @Given("I am a newly registered customer with a funded account")
    public void register() {
        page("register.htm");
        String username = "het35_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("customer.firstName", "Het");
        fields.put("customer.lastName", "Test");
        fields.put("customer.address.street", "35 Test Street");
        fields.put("customer.address.city", "Sydney");
        fields.put("customer.address.state", "NSW");
        fields.put("customer.address.zipCode", "2000");
        fields.put("customer.phoneNumber", "0400000035");
        fields.put("customer.ssn", "123453535");
        fields.put("customer.username", username);
        fields.put("customer.password", "Group35Test27");
        fields.put("repeatedPassword", "Group35Test27");
        fields.forEach((key, value) -> fill(By.id(key), value));
        click(By.cssSelector("input[value='Register']"));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("#rightPanel h1"), username));
        assertTrue(driver.findElement(By.id("rightPanel")).getText().contains("Your account was created successfully"), "Customer setup failed");
        first = accounts().iterator().next();
        firstBefore = balance(first);
        assertTrue(firstBefore.compareTo(new BigDecimal("100.00")) >= 0, "Fresh account requires at least $100 for these scenarios");
        accountsBefore = accounts();
    }

    @Given("I am a newly registered customer with two funded accounts")
    public void registerTwo() {
        register();
        click(By.linkText("Open New Account"));
        choose(By.id("fromAccountId"), first);
        new Select(visible(By.id("type"))).selectByVisibleText("SAVINGS");
        click(By.cssSelector("input[value='Open New Account']"));
        second = text("newAccountId");
        assertNotEquals(first, second, "Transfer requires distinct accounts");
        assertTrue(second.matches("\\d+"));
        firstBefore = balance(first);
        secondBefore = balance(second);
        assertTrue(firstBefore.compareTo(new BigDecimal("10.00")) >= 0, "Transfer source is not funded");
        assertTrue(secondBefore.signum() > 0, "Transfer destination is not funded");
    }

    @When("I transfer {string} from my first account to my second account")
    public void transfer(String value) {
        amount = value;
        click(By.linkText("Transfer Funds"));
        choose(By.id("fromAccountId"), first);
        choose(By.id("toAccountId"), second);
        fill(By.id("amount"), amount);
        click(By.cssSelector("input[value='Transfer']"));
    }

    @Then("the transfer confirmation identifies the amount and both accounts")
    public void transferConfirmed() {
        assertEquals("Transfer Complete!", visible(By.cssSelector("#showResult h1")).getText());
        equalMoney(new BigDecimal(amount), money(text("amountResult")));
        assertEquals(first, text("fromAccountIdResult"));
        assertEquals(second, text("toAccountIdResult"));
        evidence("Transfer confirmation");
    }

    @Then("my first account is debited and my second account is credited by {string}")
    public void transferBalances(String value) {
        BigDecimal change = new BigDecimal(value);
        equalMoney(firstBefore.subtract(change), balance(first));
        equalMoney(secondBefore.add(change), balance(second));
    }

    @Then("the transfer is rejected without a success confirmation")
    public void transferRejected() {
        wait.until(d -> shown(By.cssSelector("#showError .error")) || shown(By.cssSelector("#showForm .error")) || shown(By.id("showResult")));
        assertFalse(shown(By.id("showResult")), "Invalid transfer must not succeed");
        List<WebElement> errors = driver.findElements(By.cssSelector("#transferApp .error"));
        assertTrue(errors.stream().anyMatch(e -> e.isDisplayed() && !e.getText().isBlank()), "A rejection message is required");
        evidence("Rejected transfer");
    }

    @Then("both account balances remain unchanged")
    public void unchangedBoth() {
        equalMoney(firstBefore, balance(first));
        equalMoney(secondBefore, balance(second));
    }

    @When("I pay {string} to {string} with payee account {string} confirmed as {string}")
    public void payBill(String value, String name, String account, String confirmation) {
        amount = value;
        payee = name;
        click(By.linkText("Bill Pay"));
        Map<String, String> values = new LinkedHashMap<>();
        values.put("payee.name", payee);
        values.put("payee.address.street", "10 Utility Street");
        values.put("payee.address.city", "Sydney");
        values.put("payee.address.state", "NSW");
        values.put("payee.address.zipCode", "2000");
        values.put("payee.phoneNumber", "0290000035");
        values.put("payee.accountNumber", account);
        values.put("verifyAccount", confirmation);
        values.put("amount", amount);
        values.forEach((key, entry) -> fill(By.name(key), entry));
        choose(By.name("fromAccountId"), first);
        click(By.cssSelector("input[value='Send Payment']"));
    }

    @Then("the payment confirmation identifies the payee, amount and source account")
    public void paymentConfirmed() {
        assertEquals("Bill Payment Complete", visible(By.cssSelector("#billpayResult h1")).getText());
        assertEquals(payee, text("payeeName"));
        equalMoney(new BigDecimal(amount), money(text("amount")));
        assertEquals(first, text("fromAccountId"));
        evidence("Payment confirmation");
    }

    @Then("payment is blocked with {string}")
    public void paymentRejected(String message) {
        wait.until(d -> d.findElements(By.cssSelector("#billpayForm .error")).stream().anyMatch(e -> e.isDisplayed() && e.getText().trim().equals(message)));
        assertFalse(shown(By.id("billpayResult")), "Invalid payment must not show success");
        assertTrue(shown(By.id("billpayForm")), "Customer must be able to correct payment details");
        evidence("Payment validation");
    }

    @Then("my source account is debited by {string}")
    public void sourceDebited(String value) { equalMoney(firstBefore.subtract(new BigDecimal(value)), balance(first)); }

    @Then("my source account balance remains unchanged")
    public void sourceUnchanged() { equalMoney(firstBefore, balance(first)); }

    @When("I request a loan of {string} with a down payment of {string}")
    public void requestLoan(String loanAmount, String downPayment) {
        click(By.linkText("Request Loan"));
        fill(By.id("amount"), loanAmount);
        fill(By.id("downPayment"), downPayment);
        choose(By.id("fromAccountId"), first);
        click(By.cssSelector("input[value='Apply Now']"));
    }

    @When("I request a loan of {string} with a down payment greater than my available funds")
    public void unaffordableLoan(String loanAmount) {
        requestLoan(loanAmount, firstBefore.add(new BigDecimal("100.00")).toPlainString());
    }

    @Then("the loan decision is {string}")
    public void loanDecision(String expected) {
        wait.until(ExpectedConditions.textToBe(By.id("loanStatus"), expected));
        assertEquals(expected, text("loanStatus"));
        assertFalse(text("loanProviderName").isBlank(), "Loan provider must be identified");
        assertTrue(text("responseDate").matches("\\d{1,2}-\\d{1,2}-\\d{4}"), "Decision date must be shown");
        evidence("Loan decision");
    }

    @Then("a new loan account contains {string}")
    public void loanAccount(String value) {
        assertTrue(shown(By.id("loanRequestApproved")));
        String loanAccount = text("newAccountId");
        assertTrue(loanAccount.matches("\\d+"), "Approved loan must have an account number");
        assertFalse(accountsBefore.contains(loanAccount), "Loan account must be newly created");
        Set<String> after = accounts();
        Set<String> expected = new LinkedHashSet<>(accountsBefore);
        expected.add(loanAccount);
        assertEquals(expected, after, "Only the loan account should be added");
        equalMoney(new BigDecimal(value), balance(loanAccount));
    }

    @Then("a reason is provided without creating a loan account")
    public void deniedLoan() {
        assertFalse(visible(By.cssSelector("#loanRequestDenied .error")).getText().isBlank(), "Denial needs an explanation");
        assertFalse(shown(By.id("loanRequestApproved")));
        assertFalse(shown(By.id("newAccountId")), "Denied loan must not offer a new account");
        assertEquals(accountsBefore, accounts(), "Denied loan must not create accounts");
    }
}
