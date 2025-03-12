package com.googlecloud.testing.commands;

import com.googlecloud.testing.BaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tests for the gcloud storage rm command.
 */
public class RemoveCommandTest extends BaseTest {
    private static final String TEST_FILE_PREFIX = "test-rm-";
    private String testFileName;

    /**
     * Setup for the remove command tests.
     */
    @BeforeMethod
    public void setUp() {
        skipIfBucketNotAccessible();

        // Create unique file name for this test
        testFileName = TEST_FILE_PREFIX + System.currentTimeMillis() + ".txt";

        try {
            // Create a local test file
            Path localFile = Paths.get(testFileName);
            Files.write(localFile, "This is a test file for rm command testing.".getBytes());

            // Upload to bucket
            CommandResult uploadResult = commandExecutor.executeCommand("cp " + testFileName + " gs://" + TEST_BUCKET_NAME + "/");
            Assert.assertTrue(uploadResult.isSuccess(), "Failed to upload test file: " + uploadResult.getStderrAsString());

            // Delete local file
            Files.delete(localFile);

        } catch (Exception e) {
            LOGGER.severe("Error setting up rm test: " + e.getMessage());
        }
    }

    /**
     * Test removing a single file.
     */
    @Test
    public void testRemoveFile() {
        skipIfBucketNotAccessible();

        // First verify the file exists
        CommandResult lsResult = commandExecutor.executeCommand("ls gs://" + TEST_BUCKET_NAME + "/" + testFileName);
        Assert.assertTrue(lsResult.isSuccess(), "Test file not found in bucket before removal");

        // Remove the file
        CommandResult rmResult = commandExecutor.executeCommand("rm gs://" + TEST_BUCKET_NAME + "/" + testFileName);
        Assert.assertTrue(rmResult.isSuccess(), "Remove command failed: " + rmResult.getStderrAsString());

        // Verify the file is gone
        CommandResult verifyResult = commandExecutor.executeCommand("ls gs://" + TEST_BUCKET_NAME + "/" + testFileName);
        Assert.assertFalse(verifyResult.isSuccess(), "File still exists after removal");
    }

    /**
     * Test removing multiple files with a wildcard pattern.
     */
    @Test
    public void testRemoveWithWildcard() {
        skipIfBucketNotAccessible();

        // Create additional test files
        String[] additionalFiles = new String[3];
        for (int i = 0; i < 3; i++) {
            additionalFiles[i] = TEST_FILE_PREFIX + "multi-" + i + "-" + System.currentTimeMillis() + ".txt";
            try {
                // Create a local test file
                Path localFile = Paths.get(additionalFiles[i]);
                Files.write(localFile, ("This is test file " + i + " for rm wildcard testing.").getBytes());

                // Upload to bucket
                CommandResult uploadResult = commandExecutor.executeCommand("cp " + additionalFiles[i] + " gs://" + TEST_BUCKET_NAME + "/");
                Assert.assertTrue(uploadResult.isSuccess(), "Failed to upload additional test file: " + uploadResult.getStderrAsString());

                // Delete local file
                Files.delete(localFile);

            } catch (Exception e) {
                LOGGER.severe("Error creating additional test file: " + e.getMessage());
            }
        }

        // Remove files with wildcard
        String wildcardPattern = TEST_FILE_PREFIX + "multi-*";
        CommandResult rmResult = commandExecutor.executeCommand("rm gs://" + TEST_BUCKET_NAME + "/" + wildcardPattern);
        Assert.assertTrue(rmResult.isSuccess(), "Remove with wildcard failed: " + rmResult.getStderrAsString());

        // Verify all files are gone
        CommandResult lsResult = commandExecutor.executeCommand("ls gs://" + TEST_BUCKET_NAME + "/" + wildcardPattern);
        Assert.assertFalse(lsResult.isSuccess() && !lsResult.getStdoutAsString().contains(TEST_FILE_PREFIX + "multi-"),
                "Some files still exist after wildcard removal");
    }

    /**
     * Test removing files with the -r (recursive) flag.
     */
    @Test
    public void testRemoveRecursive() {
        skipIfBucketNotAccessible();

        // Create a folder structure
        String folderPrefix = TEST_FILE_PREFIX + "folder-" + System.currentTimeMillis();
        String[] folderFiles = new String[2];

        for (int i = 0; i < 2; i++) {
            folderFiles[i] = folderPrefix + "/file" + i + ".txt";

            // Create a local test file
            try {
                Path localFile = Paths.get("temp-file" + i + ".txt");
                Files.write(localFile, ("This is test file " + i + " for rm recursive testing.").getBytes());

                // Upload to bucket in folder structure
                CommandResult uploadResult = commandExecutor.executeCommand("cp " + localFile + " gs://" + TEST_BUCKET_NAME + "/" + folderFiles[i]);
                Assert.assertTrue(uploadResult.isSuccess(), "Failed to upload folder test file: " + uploadResult.getStderrAsString());

                // Delete local file
                Files.delete(localFile);

            } catch (Exception e) {
                LOGGER.severe("Error creating folder test file: " + e.getMessage());
            }
        }

        // Verify files exist in the folder
        CommandResult lsResult = commandExecutor.executeCommand("ls -r gs://" + TEST_BUCKET_NAME + "/" + folderPrefix + "/");
        Assert.assertTrue(lsResult.isSuccess() && lsResult.getStdout().size() > 0, "Folder files not found before removal");

        // Remove the folder recursively
        CommandResult rmResult = commandExecutor.executeCommand("rm -r gs://" + TEST_BUCKET_NAME + "/" + folderPrefix + "/");
        Assert.assertTrue(rmResult.isSuccess(), "Remove recursive failed: " + rmResult.getStderrAsString());

        // Verify the folder is gone
        CommandResult verifyResult = commandExecutor.executeCommand("ls -r gs://" + TEST_BUCKET_NAME + "/" + folderPrefix + "/");
        Assert.assertFalse(verifyResult.isSuccess() && verifyResult.getStdout().size() > 0, "Folder still exists after recursive removal");
    }
}