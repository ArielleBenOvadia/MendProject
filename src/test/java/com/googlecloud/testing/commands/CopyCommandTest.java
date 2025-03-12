package com.googlecloud.testing.commands;

import com.googlecloud.testing.BaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.microsoft.playwright.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tests for the gcloud storage cp command.
 */
public class CopyCommandTest extends BaseTest {
    private static final String TEST_FILE_PREFIX = "test-cp-";
    private static final String TEST_FILE_CONTENT = "This is a test file for copy command testing.";
    private String sourceFileName;
    private String destFileName;
    private Path localSourceFile;

    /**
     * Setup for the copy command tests.
     */
    @BeforeMethod
    public void setUp() throws Exception {
        // Create unique file names for each test run
        sourceFileName = TEST_FILE_PREFIX + "source-" + System.currentTimeMillis() + ".txt";
        destFileName = TEST_FILE_PREFIX + "dest-" + System.currentTimeMillis() + ".txt";

        // Create a local source file
        localSourceFile = Paths.get(sourceFileName);
        Files.write(localSourceFile, TEST_FILE_CONTENT.getBytes());
        LOGGER.info("Created local source file: " + localSourceFile.toAbsolutePath());
    }

    /**
     * Test copying a file from local to cloud storage.
     */
    @Test
    public void testCopyLocalToCloud() {
        // Copy from local to cloud
        CommandResult result = commandExecutor.executeCommand("cp " + sourceFileName + " gs://" + TEST_BUCKET_NAME + "/");

        // Validate command execution
        Assert.assertTrue(result.isSuccess(), "Copy local to cloud command failed: " + result.getStderrAsString());

        // Verify the file exists in the bucket
        CommandResult lsResult = commandExecutor.executeCommand("ls gs://" + TEST_BUCKET_NAME + "/" + sourceFileName);
        Assert.assertTrue(lsResult.isSuccess(), "Could not find uploaded file in bucket");
        Assert.assertTrue(lsResult.getStdout().size() > 0, "No output when listing uploaded file");

        // Clean up the local file
        try {
            Files.delete(localSourceFile);
        } catch (Exception e) {
            LOGGER.warning("Failed to delete local source file: " + e.getMessage());
        }
    }

    /**
     * Test copying a file within cloud storage.
     */
    @Test
    public void testCopyCloudToCloud() {
        // First, upload the file to cloud storage
        CommandResult uploadResult = commandExecutor.executeCommand("cp " + sourceFileName + " gs://" + TEST_BUCKET_NAME + "/");
        Assert.assertTrue(uploadResult.isSuccess(), "Initial upload failed: " + uploadResult.getStderrAsString());

        // Copy from one cloud location to another
        CommandResult copyResult = commandExecutor.executeCommand(
                "cp gs://" + TEST_BUCKET_NAME + "/" + sourceFileName + " gs://" + TEST_BUCKET_NAME + "/" + destFileName
        );

        // Validate command execution
        Assert.assertTrue(copyResult.isSuccess(), "Copy cloud to cloud command failed: " + copyResult.getStderrAsString());

        // Verify both files exist in the bucket
        CommandResult lsSourceResult = commandExecutor.executeCommand("ls gs://" + TEST_BUCKET_NAME + "/" + sourceFileName);
        Assert.assertTrue(lsSourceResult.isSuccess(), "Could not find source file in bucket after copy");

        CommandResult lsDestResult = commandExecutor.executeCommand("ls gs://" + TEST_BUCKET_NAME + "/" + destFileName);
        Assert.assertTrue(lsDestResult.isSuccess(), "Could not find destination file in bucket after copy");

        // Clean up the local file
        try {
            Files.delete(localSourceFile);
        } catch (Exception e) {
            LOGGER.warning("Failed to delete local source file: " + e.getMessage());
        }
    }

    /**
     * Test copying a file from cloud storage to local.
     */
    @Test
    public void testCopyCloudToLocal() {
        // First, upload the file to cloud storage
        CommandResult uploadResult = commandExecutor.executeCommand("cp " + sourceFileName + " gs://" + TEST_BUCKET_NAME + "/");
        Assert.assertTrue(uploadResult.isSuccess(), "Initial upload failed: " + uploadResult.getStderrAsString());

        // Delete the local file to prepare for download
        try {
            Files.delete(localSourceFile);
        } catch (Exception e) {
            LOGGER.warning("Failed to delete local source file: " + e.getMessage());
        }

        // Copy from cloud to local
        CommandResult copyResult = commandExecutor.executeCommand(
                "cp gs://" + TEST_BUCKET_NAME + "/" + sourceFileName + " " + destFileName
        );

        // Validate command execution
        Assert.assertTrue(copyResult.isSuccess(), "Copy cloud to local command failed: " + copyResult.getStderrAsString());

        // Verify the file exists locally
        Path downloadedFile = Paths.get(destFileName);
        Assert.assertTrue(Files.exists(downloadedFile), "Downloaded file does not exist locally");

        // Verify the content is correct
        try {
            String downloadedContent = new String(Files.readAllBytes(downloadedFile));
            Assert.assertEquals(downloadedContent, TEST_FILE_CONTENT, "Downloaded file content does not match original");

            // Clean up local files
            Files.delete(downloadedFile);
        } catch (Exception e) {
            LOGGER.severe("Error verifying downloaded file: " + e.getMessage());
            Assert.fail("Error verifying downloaded file: " + e.getMessage());
        }
    }

    /**
     * Test copying a file with the -r (recursive) flag.
     */
    @Test
    public void testCopyWithRecursiveFlag() throws Exception {
        // Create a subdirectory structure locally
        Path subDir = Paths.get("subdir-" + System.currentTimeMillis());
        Files.createDirectory(subDir);

        Path subFile1 = subDir.resolve("subfile1.txt");
        Path subFile2 = subDir.resolve("subfile2.txt");

        Files.write(subFile1, "Subfile 1 content".getBytes());
        Files.write(subFile2, "Subfile 2 content".getBytes());

        // Copy recursively to cloud
        CommandResult copyResult = commandExecutor.executeCommand(
                "cp -r " + subDir + " gs://" + TEST_BUCKET_NAME + "/"
        );

        // Validate command execution
        Assert.assertTrue(copyResult.isSuccess(), "Copy with recursive flag failed: " + copyResult.getStderrAsString());

        // Verify the files exist in the bucket
        CommandResult lsResult = commandExecutor.executeCommand("ls -r gs://" + TEST_BUCKET_NAME + "/" + subDir.getFileName());
        Assert.assertTrue(lsResult.isSuccess(), "Could not list files after recursive copy");

        // Verify both files are listed
        String lsOutput = lsResult.getStdoutAsString();
        Assert.assertTrue(lsOutput.contains("subfile1.txt"), "subfile1.txt not found after recursive copy");
        Assert.assertTrue(lsOutput.contains("subfile2.txt"), "subfile2.txt not found after recursive copy");

        // Clean up local files
        try {
            Files.delete(subFile1);
            Files.delete(subFile2);
            Files.delete(subDir);
            Files.delete(localSourceFile);
        } catch (Exception e) {
            LOGGER.warning("Failed to clean up local test files: " + e.getMessage());
        }
    }
}