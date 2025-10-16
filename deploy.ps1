# Heroku app name
$HEROKU_APP = "cst438-project2-group11"

# Step 1: Header
Write-Host "`n==============================="
Write-Host "Deploying Docker Image to Heroku"
Write-Host "===============================`n"

# Step 2: Clean and rebuild the Spring Boot JAR
Write-Host "🧱 Step 1: Building Gradle project (skip tests)..."
Start-Process -FilePath "./gradlew.bat" -ArgumentList "clean", "build", "-x", "test" -Wait -NoNewWindow

# Step 3: Verify JAR output exists
if (-Not (Test-Path "build/libs")) {
    Write-Host "ERROR: No JAR found in build/libs. Gradle build may have failed." -ForegroundColor Red
    exit 1
}

# Step 4: Login to Heroku registry
Write-Host "`nStep 2: Logging into Heroku container registry..."
heroku container:login
if ($LASTEXITCODE -ne 0) {
    Write-Host "Heroku login failed." -ForegroundColor Red
    exit 1
}

# Step 5: Build single-arch Docker image
Write-Host "`nStep 3: Building Linux/amd64 image for Heroku..."
docker buildx build --platform linux/amd64 --output=type=docker -t registry.heroku.com/$HEROKU_APP/web .

if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker build failed." -ForegroundColor Red
    exit 1
}

# Step 6: Push image to Heroku
Write-Host "`nStep 4: Pushing image to Heroku registry..."
docker push registry.heroku.com/$HEROKU_APP/web
if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker push failed." -ForegroundColor Red
    exit 1
}

# Step 7: Release image on Heroku
Write-Host "`nStep 5: Releasing image to Heroku app..."
heroku container:release web -a $HEROKU_APP
if ($LASTEXITCODE -ne 0) {
    Write-Host "Heroku release failed." -ForegroundColor Red
    exit 1
}

# Step 8: Restart dynos to ensure latest image is running
Write-Host "`nStep 6: Restarting Heroku dynos..."
heroku ps:restart -a $HEROKU_APP

# Step 9: Health check
Write-Host "`nStep 7: Checking dyno status..."
heroku ps -a $HEROKU_APP

# Step 10: Open app + tail logs for verification
Write-Host "`nStep 8: Opening app and tailing logs (Ctrl+C to exit)...`n"
heroku open -a $HEROKU_APP
heroku logs --tail -a $HEROKU_APP