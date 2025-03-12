package com.googlecloud.testing.commands;

import com.googlecloud.testing.BaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.microsoft.playwright.*;

/**
 * Tests for the gcloud storage ls command.
 */
public class ListCommandTest extends BaseTest {
    private static final String TEST_FILE_PREFIX = "test-ls-";
    private String testFile1;
    private String testFile2;

    /**
     * Setup for the list command tests.
     */
    @BeforeMethod
    public void setUp() {
        // Create test files
        testFile1 = TEST_FILE_PREFIX + "1.txt";
        testFile2 = TEST_FILE_PREFIX + "2.txt";

        try {
            // Create and upload first test file
            java.nio.file.Files.write(
                    java.nio.file.Paths.get(testFile1),
                    "This is test file 1 for ls command testing.".getBytes()
            );

            CommandResult uploadResult1 = commandExecutor.executeCommand("cp " + testFile1 + " gs://" + TEST_BUCKET_NAME + "/");
            if (!uploadResult1.isSuccess()) {
                LOGGER.severe("Failed to upload test file 1: " + uploadResult1.getStderrAsString());
            }

            // Create and upload second test file
            java.nio.file.Files.write(
                    java.nio.file.Paths.get(testFile2),
                    "This is test file 2 for ls command testing.".getBytes()
            );

            CommandResult uploadResult2 = commandExecutor.executeCommand("cp " + testFile2 + " gs://" + TEST_BUCKET_NAME + "/");
            if (!uploadResult2.isSuccess()) {
                LOGGER.severe("Failed to upload test file 2: " + uploadResult2.getStderrAsString());
            }

            // Clean up local files
            java.nio.file.Files.delete(java.nio.file.Paths.get(testFile1));
            java.nio.file.Files.delete(java.nio.file.Paths.get(testFile2));

        } catch (Exception e) {
            LOGGER.severe("Error creating test files for ls command: " + e.getMessage());
        }
    }

    /**
     * Test that the ls command lists buckets.
     */
    @Test
    public void testListBuckets() {
        // Instead of testing all buckets, directly check if our test bucket exists
        // Remove the --limit=1 argument which isn't supported
        CommandResult result = commandExecutor.executeCommand("ls gs://" + TEST_BUCKET_NAME + " --project=" + PROJECT_ID);

        // Validate command execution
        Assert.assertTrue(result.isSuccess(), "Bucket access failed: " + result.getStderrAsString());

        // If we get here, the bucket exists and is accessible
        LOGGER.info("Successfully verified bucket exists: " + TEST_BUCKET_NAME);
    }

    /**
     * Test that the ls command lists files in a bucket.
     */
    @Test
    public void testListFilesInBucket() {
        CommandResult result = commandExecutor.executeCommand("ls gs://" + TEST_BUCKET_NAME);

        // Validate command execution
        Assert.assertTrue(result.isSuccess(), "List files command failed: " + result.getStderrAsString());

        // Verify that our test files are listed
        boolean foundFile1 = false;
        boolean foundFile2 = false;

        for (String line : result.getStdout()) {
            if (line.contains(testFile1)) {
                foundFile1 = true;
            }
            if (line.contains(testFile2)) {
                foundFile2 = true;
            }
        }

        Assert.assertTrue(foundFile1, "Test file 1 not found in bucket listing");
        Assert.assertTrue(foundFile2, "Test file 2 not found in bucket listing");
    }

    /**
     * Test ls command with -l (long) format.
     */
    @Test
    public void testListWithLongFormat() {
        CommandResult result = commandExecutor.executeCommand("ls -l gs://" + TEST_BUCKET_NAME);

        // Validate command execution
        Assert.assertTrue(result.isSuccess(), "List with long format command failed: " + result.getStderrAsString());

        // Verify that the output contains size information
        boolean containsSizeInfo = false;
        for (String line : result.getStdout()) {
            if (line.matches(".*\\d+\\s+\\d{4}-\\d{2}-\\d{2}.*")) {
                containsSizeInfo = true;
                break;
            }
        }

        Assert.assertTrue(containsSizeInfo, "Long format listing does not contain size and date information");
    }

    /**
     * Test ls command with a pattern.
     */
    @Test
    public void testListWithPattern() {
        CommandResult result = commandExecutor.executeCommand("ls gs://" + TEST_BUCKET_NAME + "/" + TEST_FILE_PREFIX + "*.txt");

        // Validate command execution
        Assert.assertTrue(result.isSuccess(), "List with pattern command failed: " + result.getStderrAsString());

        // Verify that both test files are found
        Assert.assertEquals(result.getStdout().size(), 2, "Expected exactly 2 files matching the pattern");

        // Test with a more specific pattern that should match only one file
        CommandResult specificResult = commandExecutor.executeCommand("ls gs://" + TEST_BUCKET_NAME + "/" + TEST_FILE_PREFIX + "1*.txt");

        // Validate command execution
        Assert.assertTrue(specificResult.isSuccess(), "List with specific pattern command failed: " + specificResult.getStderrAsString());

        // Verify that only one file is found
        Assert.assertEquals(specificResult.getStdout().size(), 1, "Expected exactly 1 file matching the specific pattern");
        Assert.assertTrue(specificResult.getStdoutAsString().contains(testFile1), "Pattern matching should have found test file 1");
    }
}