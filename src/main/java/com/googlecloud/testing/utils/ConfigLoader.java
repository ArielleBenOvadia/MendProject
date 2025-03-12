package com.googlecloud.testing.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Loads configuration for tests.
 */
public class ConfigLoader {
    private static final Logger LOGGER = Logger.getLogger(ConfigLoader.class.getName());
    private static final String DEFAULT_BUCKET_NAME = "gcloud-cli-test-bucket-1234"; // Fixed default bucket name

    private static Properties properties;

    /**
     * Gets the properties from config file.
     *
     * @return Properties object with configuration
     */
    private static Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (input != null) {
                    properties.load(input);
                    LOGGER.info("Configuration loaded successfully");
                } else {
                    LOGGER.warning("config.properties file not found, using default values");
                }
            } catch (IOException e) {
                LOGGER.warning("Error loading properties: " + e.getMessage());
            }
        }
        return properties;
    }


    /**
     * Gets the test bucket name from properties file or default.
     *
     * @return The test bucket name
     */
    public static String getTestBucketName() {
        String bucketName = getProperties().getProperty("test.bucket");
        if (bucketName == null || bucketName.isEmpty()) {
            // IMPORTANT: Use the fixed default bucket name instead of generating one
            LOGGER.info("Using fixed default bucket name: " + DEFAULT_BUCKET_NAME);
            return DEFAULT_BUCKET_NAME;
        }
        LOGGER.info("Using bucket name from config: " + bucketName);
        return "gcloud-cli-test-bucket-1234";
    }

    /**
     * Gets the project ID from properties file or environment.
     *
     * @return The project ID
     */
    public static String getProjectId() {
        String projectId = getProperties().getProperty("test.projectId");
        if (projectId == null || projectId.isEmpty()) {
            // Try environment variable
            projectId = System.getenv("PROJECT_ID");
        }
        if (projectId == null || projectId.isEmpty()) {
            projectId = "mend-project-arielle-123"; // Default value
        }
        return projectId;
    }


}