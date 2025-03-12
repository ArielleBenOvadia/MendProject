# Docker Setup Instructions

This document explains how to run the Google Cloud Storage CLI tests using Docker.

## Prerequisites

1. Docker installed on your machine
2. Access to Google Cloud Storage (for non-mock tests)
3. Google Cloud SDK installed locally (for authentication)

## Running with Docker

### 1. Build the Docker Image

Navigate to the project root directory and build the Docker image:

```bash
docker build -t gcloud-storage-cli-testing .
```

### 2. Authentication Options

There are multiple ways to authenticate with Google Cloud when running tests in Docker:

#### Option A: Mount your local Google Cloud credentials

If you've already authenticated with `gcloud auth login` on your local machine, you can mount those credentials into the container:

```bash
docker run --rm -v ~/.config/gcloud:/root/.config/gcloud gcloud-storage-cli-testing
```

For Windows PowerShell:

```powershell
docker run --rm -v ${env:USERPROFILE}\.config\gcloud:/root/.config/gcloud gcloud-storage-cli-testing
```

#### Option B: Use a Service Account Key File

1. Create a service account with the necessary permissions
2. Download the service account key file as JSON
3. Place it in the project directory (do not commit this file to version control)
4. Run Docker with the key file:

```bash
docker run --rm -v $(pwd)/service-account-key.json:/gcloud-key/key.json -e GOOGLE_APPLICATION_CREDENTIALS=/gcloud-key/key.json gcloud-storage-cli-testing
```

### 3. Using Docker Compose

You can also use Docker Compose for a more streamlined experience:

```bash
docker-compose up
```

Or to run in mock mode (no real GCP access needed):

```bash
docker-compose run test-mock
```

## Configuring the Docker Environment

### Environment Variables

You can pass additional environment variables to configure the tests:

```bash
docker run --rm -v ~/.config/gcloud:/root/.config/gcloud \
  -e TEST_BUCKET_NAME=my-special-bucket \
  -e PROJECT_ID=my-gcp-project \
  gcloud-storage-cli-testing
```

### Java System Properties

To pass Java system properties to Maven:

```bash
docker run --rm -v ~/.config/gcloud:/root/.config/gcloud \
  gcloud-storage-cli-testing \
  mvn clean test -Dtest.bucket=my-bucket -Dcommand.timeout=120
```

## Running in Mock Mode

To run tests without needing actual GCP access:

```bash
docker run --rm gcloud-storage-cli-testing mvn clean test -Duse.mock=true
```

This mode uses the `MockStorageCommandExecutor` to simulate command responses.

## Troubleshooting Docker Issues

### 1. Authentication Issues

If you encounter authentication issues, try:

```bash
# Inside your local machine (not Docker)
gcloud auth application-default login
# Then run Docker with credentials mounted
docker run --rm -v ~/.config/gcloud:/root/.config/gcloud gcloud-storage-cli-testing
```

### 2. Debugging Container Issues

To debug issues within the container:

```bash
# Start an interactive shell
docker run --rm -it gcloud-storage-cli-testing /bin/bash

# Inside the container
gcloud auth list
gcloud config list
gcloud storage ls
```

### 3. Browser/Playwright Issues

If you encounter issues with the browser-based validation:

```bash
# Run with detailed Playwright logging
docker run --rm -v ~/.config/gcloud:/root/.config/gcloud \
  -e DEBUG=pw:api \
  gcloud-storage-cli-testing
```

## Advanced Docker Configuration

### Custom Entry Point

The Docker container uses an `entrypoint.sh` script that performs some initial debugging and setup. If needed, you can override it:

```bash
docker run --rm -v ~/.config/gcloud:/root/.config/gcloud \
  --entrypoint /bin/bash \
  gcloud-storage-cli-testing \
  -c "gcloud auth list && mvn clean test"
```

### Resource Limits

For better performance on resource-constrained systems:

```bash
docker run --rm -v ~/.config/gcloud:/root/.config/gcloud \
  --memory=2g --cpus=2 \
  gcloud-storage-cli-testing
```