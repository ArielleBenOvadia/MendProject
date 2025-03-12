# run-tests-cloud.ps1

# Instructions for the assignment checker
Write-Host "Google Cloud Storage CLI Testing" -ForegroundColor Green
Write-Host "============================" -ForegroundColor Green
Write-Host ""
Write-Host "This project has been deployed to Google Cloud Run Jobs." -ForegroundColor Yellow
Write-Host "To view the tests, you have two options:" -ForegroundColor Yellow
Write-Host ""
Write-Host "OPTION 1: Run tests locally with Docker (recommended)" -ForegroundColor Cyan
Write-Host "  docker run --rm -v ${env:USERPROFILE}\.config\gcloud:/root/.config/gcloud gcloud-storage-cli-testing"
Write-Host ""
Write-Host "OPTION 2: View pre-run tests in Google Cloud Console" -ForegroundColor Cyan
Write-Host "  1. Visit: https://console.cloud.google.com/run/jobs/executions/details/us-central1/gcloud-cli-tests-tm7mm/tasks?project=78233030070"
Write-Host "  2. Login with any Google account to view public logs"
Write-Host ""
Write-Host "NOTE: You will NOT be able to execute the Cloud Run Job yourself" -ForegroundColor Red
Write-Host "      as you don't have permissions to the Google Cloud project." -ForegroundColor Red
Write-Host ""

# Ask if they want to run tests locally with Docker
$choice = Read-Host "Would you like to run the tests locally with Docker? (y/n)"

if ($choice -eq "y" -or $choice -eq "Y") {
    Write-Host "Building and running Docker container locally..." -ForegroundColor Green
    docker build -t gcloud-storage-cli-testing .
    docker run --rm -v ${env:USERPROFILE}\.config\gcloud:/root/.config/gcloud gcloud-storage-cli-testing
} else {
    Write-Host "Please check the pre-run tests in Google Cloud Console using the URL provided above." -ForegroundColor Yellow
}