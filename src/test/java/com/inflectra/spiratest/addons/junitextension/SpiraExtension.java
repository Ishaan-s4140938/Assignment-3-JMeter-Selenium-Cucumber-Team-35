package com.inflectra.spiratest.addons.junitextension;

import org.junit.jupiter.api.extension.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Main class of the extension
 */
public class SpiraExtension implements Extension, BeforeAllCallback, AfterEachCallback {

    private static final int EXECUTION_SUCCESS = 2;
    private static final int EXECUTION_FAILED = 1;

    /**
     * Used to send information to Spira
     */
    private static SpiraTestExecute spiraTestExecute;

    /**
     * All of the test runs we need to post to the server.
     * They are posted when the last unit test finishes.
     */
    private static List<TestRun> testRuns;

    /**
     * The total number of tests that will be run
     */
    private static int numTests;

    /**
     * Called before any of the test cases are run.
     * This method sets up the instance.
     */
    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {

        // Get all methods in the class
        Class clazz = extensionContext.getTestClass().get();
        Method[] methods = clazz.getMethods();

        numTests = 0;

        // Increment numTests for every method with the Spira annotation
        for (Method m : methods) {
            if (m.isAnnotationPresent(SpiraTestCase.class)) {
                numTests++;
            }
        }

        // Initialize the array list
        testRuns = new ArrayList<>(numTests);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {

        Set<String> tags = extensionContext.getTags();

        // Spira test run we are creating
        TestRun testRun = new TestRun();

        // Get the method being tested
        Method method = extensionContext.getTestMethod().get();

        // Get the class being tested
        Class clazz = extensionContext.getTestClass().get();

        // Only process methods with the Spira annotation
        if (method.isAnnotationPresent(SpiraTestCase.class)) {

            // Retrieve the test case ID
            SpiraTestCase testCase =
                    method.getAnnotation(SpiraTestCase.class);

            int testCaseId = testCase.testCaseId();

            testRun.testCaseId = testCaseId;

            // Retrieve Spira configuration
            if (clazz.isAnnotationPresent(SpiraTestConfiguration.class)) {

                Annotation spiraAnnotation =
                        clazz.getAnnotation(SpiraTestConfiguration.class);

                SpiraTestConfiguration testConfiguration =
                        (SpiraTestConfiguration) spiraAnnotation;

                // Populate the configuration
                testRun.url = testConfiguration.url();

                testRun.userName = testConfiguration.login();

                // Read password from annotation
                testRun.password = testConfiguration.password();

                /*
                 * If the password is empty in the annotation,
                 * retrieve it from the SPIRA_PASSWORD
                 * environment variable.
                 *
                 * This prevents storing passwords in GitHub.
                 */
                if (testRun.password == null ||
                        testRun.password.isEmpty()) {

                    testRun.password =
                            System.getenv("SPIRA_PASSWORD");
                }

                testRun.projectId = testConfiguration.projectId();

                testRun.releaseId = testConfiguration.releaseId();

                testRun.testSetId = testConfiguration.testSetId();

                testRun.testName = extensionContext.getDisplayName();

                // Check whether the test passed or failed
                Optional<Throwable> throwable =
                        extensionContext.getExecutionException();

                if (throwable.isPresent()) {

                    // Convert stack trace to readable text
                    StringWriter sw = new StringWriter();

                    PrintWriter pw = new PrintWriter(sw);

                    throwable.get().printStackTrace(pw);

                    testRun.stackTrace = sw.toString();

                    testRun.executionStatusId = EXECUTION_FAILED;

                    testRun.message = "Test Failed";

                } else {

                    testRun.stackTrace = "Test Succeeded";

                    testRun.executionStatusId = EXECUTION_SUCCESS;
                }
            }

            // Check required fields
            List<String> missing = missingRequiredFields(testRun);

            if (missing.size() == 0) {

                // Initialize Spira connection only once
                if (spiraTestExecute == null) {

                    spiraTestExecute = new SpiraTestExecute();

                    spiraTestExecute.password = testRun.password;

                    spiraTestExecute.userName = testRun.userName;

                    spiraTestExecute.url = testRun.url;

                    spiraTestExecute.projectId = testRun.projectId;
                }

                // Add test run to the list
                testRuns.add(testRun);

                // Upload results when all tests finish
                if (testRuns.size() == numTests) {

                    for (TestRun r : testRuns) {

                        spiraTestExecute.recordTestRun(r);
                    }
                }

            } else {

                System.out.println(
                        "You need to specify the following " +
                                "to connect to Spira: " +
                                missing.toString()
                );
            }
        }
    }

    /**
     * Returns a list of missing required fields.
     */
    private static List<String> missingRequiredFields(TestRun testRun) {

        List<String> missing = new ArrayList<>(4);

        // Check URL
        if (testRun.url == null || testRun.url.isEmpty()) {
            missing.add("URL");
        }

        // Check username
        if (testRun.userName == null || testRun.userName.isEmpty()) {
            missing.add("Username");
        }

        // Check password
        if (testRun.password == null || testRun.password.isEmpty()) {
            missing.add("Password");
        }

        // Check project ID
        if (testRun.projectId == -1) {
            missing.add("Project ID");
        }

        return missing;
    }
}