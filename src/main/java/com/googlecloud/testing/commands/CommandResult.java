package com.googlecloud.testing.commands;

import java.util.List;
import com.microsoft.playwright.*;

/**
 * Represents the result of executing a command.
 */
public class CommandResult {
    private final List<String> stdout;
    private final List<String> stderr;
    private final int exitCode;

    /**
     * Constructor for CommandResult.
     *
     * @param stdout The standard output lines
     * @param stderr The standard error lines
     * @param exitCode The exit code of the command
     */
    public CommandResult(List<String> stdout, List<String> stderr, int exitCode) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
    }

    /**
     * Get the standard output lines.
     *
     * @return List of stdout lines
     */
    public List<String> getStdout() {
        return stdout;
    }

    /**
     * Get the standard error lines.
     *
     * @return List of stderr lines
     */
    public List<String> getStderr() {
        return stderr;
    }

    /**
     * Get the exit code.
     *
     * @return The exit code (0 typically means success)
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Check if the command was successful.
     *
     * @return true if the exit code is 0, false otherwise
     */
    public boolean isSuccess() {
        return exitCode == 0;
    }

    /**
     * Get the standard output as a single string.
     *
     * @return Combined stdout as a string
     */
    public String getStdoutAsString() {
        return String.join("\n", stdout);
    }

    /**
     * Get the standard error as a single string.
     *
     * @return Combined stderr as a string
     */
    public String getStderrAsString() {
        return String.join("\n", stderr);
    }

    @Override
    public String toString() {
        return "CommandResult{" +
                "exitCode=" + exitCode +
                ", stdout=" + stdout +
                ", stderr=" + stderr +
                '}';
    }
}