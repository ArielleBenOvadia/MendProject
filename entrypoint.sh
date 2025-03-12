#!/bin/bash
echo "Setting up test environment..."

# Debugging: Print relevant environment information
echo "============================================================"
echo "DEBUG: SYSTEM INFORMATION"
echo "============================================================"
echo "Current directory: $(pwd)"
echo "Directory contents: $(ls -la)"
echo "Java version: $(java -version 2>&1)"
echo "Maven version: $(mvn -version)"
echo "GCloud version: $(gcloud --version | head -1)"

# Debugging: Check for dynamic bucket name generation in code
echo "============================================================"
echo "DEBUG: CODE ANALYSIS"
echo "============================================================"
echo "Searching for bucket creation in code..."
grep -r "buckets create" /app/src || echo "No direct bucket creation found"

echo "Searching for System.currentTimeMillis usage..."
grep -r "System.currentTimeMillis" /app/src || echo "No timestamp generation found"

echo "Checking ConfigLoader implementation..."
find /app/src -name "ConfigLoader.java" -exec cat {} \; || echo "ConfigLoader.java not found"

echo "Checking BaseTest implementation..."
find /app/src -name "BaseTest.java" -exec cat {} \; || echo "BaseTest.java not found"

# Debugging: Check config properties
echo "============================================================"
echo "DEBUG: CONFIGURATION"
echo "============================================================"
echo "Config properties file contents:"
find /app/src -name "config.properties" -exec cat {} \; || echo "config.properties not found"

# Authenticate with service account key if provided
if [ -f "/app/service-account-key.json" ]; then
  echo "Authenticating with service account key..."
  gcloud auth activate-service-account --key-file=/app/service-account-key.json

  # Set default project
  gcloud config set project $(grep "test.projectId" /app/src/test/resources/config.properties | cut -d"=" -f2 || echo "mend-project-arielle-123")
else
  echo "No service account key found. Using default authentication."
fi

# Check if bucket exists (DO NOT try to create it)
echo "============================================================"
echo "DEBUG: BUCKET CHECK"
echo "============================================================"
BUCKET_NAME="gcloud-cli-test-bucket-1234"
echo "Checking if bucket exists: $BUCKET_NAME"
gsutil ls gs://$BUCKET_NAME > /dev/null 2>&1
if [ $? -eq 0 ]; then
  echo "Test bucket exists."
else
  echo "Test bucket does not exist. Please create it manually in Google Cloud Console."
  # Continue anyway to see if tests attempt to create a different bucket
  echo "Will continue to run tests to debug bucket name issue."
fi

# Force using the fixed bucket name as an environment variable
echo "============================================================"
echo "DEBUG: SETTING FORCED BUCKET NAME"
echo "============================================================"
echo "Setting TEST_BUCKET_NAME environment variable to $BUCKET_NAME"
export TEST_BUCKET_NAME=$BUCKET_NAME

# Start Xvfb for headless browser tests if needed
Xvfb :99 -screen 0 1024x768x16 &
export DISPLAY=:99

# Run Maven with debug output to see what's happening
echo "============================================================"
echo "DEBUG: RUNNING TESTS WITH DEBUG OUTPUT"
echo "============================================================"
exec "$@" -X