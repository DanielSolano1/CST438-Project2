param(
    [switch]$tests  # Optional flag: run tests if provided
)

# Step 1: Print header
Write-Host "`n==============================="
Write-Host "Rebuilding and Running Locally"
Write-Host "===============================`n"

# Step 2: Clean and rebuild the Spring Boot JAR
if ($tests) {
    Write-Host "Step 1: Building Gradle project (with tests)..."
    Start-Process -FilePath "./gradlew.bat" -ArgumentList "clean", "build" -Wait -NoNewWindow
} else {
    Write-Host "Step 1: Building Gradle project (skipping tests)..."
    Start-Process -FilePath "./gradlew.bat" -ArgumentList "clean", "build", "-x", "test" -Wait -NoNewWindow
}

# Step 3: Rebuild the Docker image
Write-Host "`nStep 2: Building Docker image..."
docker build -t cst438-project2 .

# Step 4: Stop & remove any old container (optional but clean)
Write-Host "`nStep 3: Cleaning up old container..."
docker stop project2-container 2>$null | Out-Null
docker rm project2-container 2>$null | Out-Null

# Step 5: Run a new container
Write-Host "`nStep 4: Starting new container..."
docker run -d --name project2-container -p 8080:8080 cst438-project2

# Step 6: Show logs
Write-Host "`nStep 5: Showing logs (Ctrl+C to exit)...`n"
docker logs -f project2-container