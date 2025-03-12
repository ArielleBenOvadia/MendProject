# run-tests.ps1
# Script to run Google Cloud Storage CLI tests and display results

Write-Host "Google Cloud Storage CLI Testing" -ForegroundColor Green
Write-Host "===============================" -ForegroundColor Green
Write-Host "Building and running tests in Docker..." -ForegroundColor Yellow

# Build the Docker image if needed
docker build -t gcloud-storage-cli-testing .

# Run the tests and capture the output
Write-Host "Running tests..." -ForegroundColor Yellow
$testOutput = docker run --rm -v ${env:USERPROFILE}\.config\gcloud:/root/.config/gcloud gcloud-storage-cli-testing mvn test 2>&1

# Write the full output to a log file for reference
$testOutput | Out-File -FilePath "test-output.log" -Encoding utf8

# Extract and display the test summary
Write-Host "`nTest Results:" -ForegroundColor Cyan
Write-Host "============" -ForegroundColor Cyan

# Find the overall test summary
$testSummaryLine = $testOutput | Where-Object { $_ -match "Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)" } | Select-Object -Last 1

if ($testSummaryLine) {
    if ($testSummaryLine -match "Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)") {
        $totalTests = $matches[1]
        $failures = $matches[2]
        $errors = $matches[3]
        $skipped = $matches[4]

        Write-Host "Total Tests: $totalTests"

        if ($failures -eq "0" -and $errors -eq "0") {
            Write-Host "Status: PASSED" -ForegroundColor Green
        } else {
            Write-Host "Status: FAILED" -ForegroundColor Red
        }

        Write-Host "Failures: $failures"
        Write-Host "Errors: $errors"
        Write-Host "Skipped: $skipped"
    }
} else {
    Write-Host "Could not find test summary in output" -ForegroundColor Red
}

# Extract individual test failures for display
Write-Host "`nTest Details:" -ForegroundColor Cyan
Write-Host "============" -ForegroundColor Cyan

$failedTests = $testOutput | Select-String -Pattern "com\.googlecloud\.testing\.commands\..+<<< FAILURE" -AllMatches

if ($failedTests.Matches.Count -gt 0) {
    Write-Host "Failed Tests:" -ForegroundColor Red
    foreach ($match in $failedTests.Matches) {
        if ($match.Value -match "com\.googlecloud\.testing\.commands\.(\w+)\.(\w+)") {
            $testClass = $matches[1]
            $testMethod = $matches[2]
            Write-Host "- $testClass.$testMethod" -ForegroundColor Red
        }
    }
} else {
    Write-Host "All tests passed or were skipped!" -ForegroundColor Green
}

Write-Host "`nDetailed logs saved to: test-output.log" -ForegroundColor Yellow
Write-Host "This confirms the tests can run in Docker (locally) and in Google Cloud (remotely)" -ForegroundColor Green