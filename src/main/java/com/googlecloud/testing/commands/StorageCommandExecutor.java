package com.googlecloud.testing.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import com.googlecloud.testing.utils.ConfigLoader;

/**
 * Executes Google Cloud Storage CLI commands and returns the results.
 */
public class StorageCommandExecutor {
    private static final Logger LOGGER = Logger.getLogger(StorageCommandExecutor.class.getName());
    private static final int COMMAND_TIMEOUT_SECONDS = 60;

    /**
     * Executes a gcloud storage command and returns the result.
     *
     * @param command The command to execute (without the "gcloud storage" prefix)
     * @return A CommandResult object containing stdout, stderr, and exit code
     */
    public CommandResult executeCommand(String command) {
        return executeCommand(command, COMMAND_TIMEOUT_SECONDS);
    }

    /**
     * Executes a gcloud storage command with a specified timeout and returns the result.
     *
     * @param command The command to execute (without the "gcloud storage" prefix)
     * @param timeoutSeconds Timeout in seconds
     * @return A CommandResult object containing stdout, stderr, and exit code
     */
    public CommandResult executeCommand(String command, int timeoutSeconds) {
        List<String> stdout = new ArrayList<>();
        List<String> stderr = new ArrayList<>();
        int exitCode = -1;

        // Add project ID to commands if not already specified
        if (!command.contains("--project=") && !command.equals("--help")) {
            if (command.startsWith("ls") && !command.contains(" gs://")) {
                // For plain 'ls' command, add project
                command = command + " --project=" + ConfigLoader.getProjectId();
            } else if (!command.startsWith("--")) {
                // Add project to other commands
                command = command + " --project=" + ConfigLoader.getProjectId();
            }
        }

        // Prepare the full command
        String fullCommand = "gcloud storage " + command;
        LOGGER.info("Executing command: " + fullCommand);

        try {
            // Create the process
            ProcessBuilder processBuilder = new ProcessBuilder();

            // Set up the command based on the operating system
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                processBuilder.command("cmd.exe", "/c", fullCommand);
            } else {
                processBuilder.command("bash", "-c", fullCommand);
            }

            // Start the process
            Process process = processBuilder.start();

            // Read the standard output
            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = stdoutReader.readLine()) != null) {
                stdout.add(line);
            }

            // Read the standard error
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = stderrReader.readLine()) != null) {
                stderr.add(line);
            }

            // Wait for the process to complete with timeout
            boolean completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                stderr.add("Command timed out after " + timeoutSeconds + " seconds");
                return new CommandResult(stdout, stderr, -1);
            }

            // Get the exit code
            exitCode = process.exitValue();

            LOGGER.info("Command completed with exit code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            LOGGER.severe("Error executing command: " + e.getMessage());
            stderr.add("Exception: " + e.getMessage());
        }

        return new CommandResult(stdout, stderr, exitCode);
    }
}