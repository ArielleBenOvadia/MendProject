# Google Cloud Storage CLI Testing Framework

## Overview
This project provides an automated testing framework for Google Cloud Storage CLI commands. It enables comprehensive validation of multiple CLI commands, including thorough testing of the sign-url command to ensure generated URLs don't trigger phishing warnings.
The framework is designed to be modular, extensible, and capable of running both locally (using Docker) and in Google Cloud environments.

## Features
- Automated tests for four gcloud storage commands:
      -  ls (list buckets and objects)
      -  cp (copy files to/from cloud storage)
      -  rm (remove files from cloud storage)
      -  sign-url (generate signed URLs for temporary access)
      -  Browser-based validation of signed URLs using Playwright
      -  Docker containerization for consistent testing environments
      -  Google Cloud Run Jobs integration for cloud-based execution
      -  Modular architecture for easy expansion to other commands

##Prerequisites

### - For local execution:
       - Java JDK 11 or higher
       - Maven 3.6+
       - Docker
       - Google Cloud SDK installed and configured


### - For cloud execution:
      - Google Cloud account with permissions to:
      - Create and run Cloud Run Jobs
      - Access Google Cloud Storage

## Running the Tests

### Option 1: Simple Script (Recommended)
The easiest way to run the tests is using the provided PowerShell script:
.\run-tests.ps1

This script will:
1. Build the Docker image
2. Run the tests in the container
3. Display formatted test results in the console
4. Save detailed logs to a file

### Option 2: Manual Execution with Docker

Build the Docker image:
docker build -t gcloud-storage-cli-testing .

Run the tests:
docker run --rm -v ~/.config/gcloud:/root/.config/gcloud gcloud-storage-cli-testing

For Windows PowerShell, use:
powershellCopydocker run --rm -v ${env:USERPROFILE}\.config\gcloud:/root/.config/gcloud gcloud-storage-cli-testing

### Option 3: Execution in Google Cloud
The tests have been successfully deployed and run in Google Cloud Run Jobs. To verify this:
       1. Check the Google Cloud Run Jobs page for this project
       2. View the successful executions and their logs
       3. See the /results folder for screenshots and evidence of cloud execution

## Test Details

### List Command (ls)
Tests validation of the gcloud storage ls command:
       - Listing buckets
       - Listing objects in a bucket
       - Using different format options
       - Pattern matching for object names

### Copy Command (cp)
Tests validation of the gcloud storage cp command:
       - Copying files from local to cloud
       - Copying files between cloud locations
       - Copying files from cloud to local
       - Recursive copying of directories

### Remove Command (rm)
Tests validation of the gcloud storage rm command:
       - Removing individual files
       - Using wildcards to remove multiple files
       - Recursive removal of directories

### Sign-URL Command (sign-url)
Tests validation of the gcloud storage sign-url command:  
       - Generating properly signed URLs
       - Setting different expiration durations
       - Browser-based validation to ensure URLs don't trigger phishing warnings
       - Error handling for invalid parameters

### Extending the Framework
To add tests for additional commands:
       - Create a new test class in src/test/java/com/googlecloud/testing/commands/
       - Extend the BaseTest class to inherit common functionality
       - Add test methods using TestNG annotations
       - Update the testng.xml file to include your new test class

### Cloud Deployment
This testing framework has been successfully deployed and run in Google Cloud Run Jobs, demonstrating that it works in both local and cloud environments.
The deployment process:
       - Build and push the Docker image to Google Container Registry
       - Create a Cloud Run Job using the image
       - Configure memory and permissions for the job
       - Execute the job to run tests in the cloud

### Contributing
To contribute to this project:
       - Fork the repository
       - Create a feature branch
       - Submit a pull request with your changes

### License
This project is licensed under the MIT License - see the LICENSE file for details.
Acknowledgments
       - Google Cloud Documentation
       - TestNG Project
       - Playwright Documentation

       
