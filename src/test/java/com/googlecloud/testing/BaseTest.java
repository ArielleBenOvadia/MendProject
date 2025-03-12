package com.googlecloud.testing;

import com.googlecloud.testing.commands.CommandResult;
import com.googlecloud.testing.commands.StorageCommandExecutor;
import com.googlecloud.testing.utils.ConfigLoader;
import org.testng.SkipException;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.util.logging.Logger;

/**
 * Base class for all Google Cloud Storage CLI tests.
 * Provides common setup and teardown functionality for all test classes.
 */
public class BaseTest {
    protected static final Logger LOGGER = Logger.getLogger(BaseTest.class.getName());
    protected static final StorageCommandExecutor commandExecutor = new StorageCommandExecutor();
    protected static final String TEST_BUCKET_NAME = ConfigLoader.getTestBucketName();
    protected static final String PROJECT_ID = ConfigLoader.getProjectId();

    /**
     * Setup executed before all tests to ensure environment is ready.
     */
    @BeforeSuite
    public void suiteSetup() {
        LOGGER.info("Setting up test suite");

        // Verify that gcloud is installed and authenticated
        CommandResult result = commandExecutor.executeCommand("--help");
        if (!result.isSuccess()) {
            LOGGER.severe("Google Cloud SDK not installed or not in PATH");
            throw new RuntimeException("Google Cloud SDK not available");
        }

        LOGGER.info("Using test bucket: " + TEST_BUCKET_NAME);
        LOGGER.info("Using project ID: " + PROJECT_ID);

        // Check if the test bucket exists
        CommandResult bucketCheckResult = commandExecutor.executeCommand("ls gs://" + TEST_BUCKET_NAME);
        if (!bucketCheckResult.isSuccess()) {
            LOGGER.warning("Test bucket does not exist or is not accessible: " + TEST_BUCKET_NAME);
            LOGGER.warning("Some tests may be skipped");
        } else {
            // Create a test file in the bucket
            createTestFile();
        }
    }

    /**
     * Cleanup executed after all tests.
     */
    @AfterSuite
    public void suiteTeardown() {
        LOGGER.info("Cleaning up test suite");

        // Clean up test files but don't delete the bucket
        CommandResult removeResult = commandExecutor.executeCommand("rm gs://" + TEST_BUCKET_NAME + "/test-* --recursive");
        if (!removeResult.isSuccess()) {
            LOGGER.warning("Failed to clean up test files: " + removeResult.getStderrAsString());
        }
    }

    /**
     * Creates a test file in the test bucket.
     */
    protected void createTestFile() {
        // Create a local test file
        String localFile = "test-file-" + System.currentTimeMillis() + ".txt";
        try {
            java.nio.file.Files.write(
                    java.nio.file.Paths.get(localFile),
                    "This is a test file for gcloud storage CLI testing.".getBytes()
            );

            // Upload the file to the test bucket
            CommandResult uploadResult = commandExecutor.executeCommand("cp " + localFile + " gs://" + TEST_BUCKET_NAME + "/");
            if (!uploadResult.isSuccess()) {
                LOGGER.severe("Failed to upload test file: " + uploadResult.getStderrAsString());
            } else {
                LOGGER.info("Test file uploaded successfully");
            }

            // Delete the local file
            java.nio.file.Files.delete(java.nio.file.Paths.get(localFile));

        } catch (Exception e) {
            LOGGER.severe("Error creating test file: " + e.getMessage());
        }
    }


    /**
     * Helper method to skip tests if bucket is not accessible.
     * This prevents tests from failing when they require bucket access.
     */
    protected void skipIfBucketNotAccessible() {
        CommandResult bucketCheckResult = commandExecutor.executeCommand("ls gs://" + TEST_BUCKET_NAME + " --project=" + PROJECT_ID);
        if (!bucketCheckResult.isSuccess()) {
            throw new SkipException("Test bucket not accessible: " + TEST_BUCKET_NAME +
                    ". Error: " + bucketCheckResult.getStderrAsString());
        }
    }
}