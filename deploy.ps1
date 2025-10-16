# Heroku app name
$HEROKU_APP = "cst438-project2-group11"

# Step 1: Print header
Write-Host "`n==============================="
Write-Host "Deploying Docker Image to Heroku"
Write-Host "===============================`n"

# Step 2: Clean and rebuild the Spring Boot JAR
Write-Host "Step 1: Building Gradle project..."
Start-Process -FilePath "./gradlew.bat" -ArgumentList "clean", "build", "-x", "test" -Wait -NoNewWindow

# Step 3: Confirm JAR exists
if (-Not (Test-Path "build/libs")) {
    Write-Host "ERROR: No JAR found in build/libs. Did Gradle fail?" -ForegroundColor Red
    exit 1
}

# Step 4: Rebuild the Docker image
Write-Host "`nStep 2: Building Docker image..."
docker build -t $HEROKU_APP .

if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker build failed." -ForegroundColor Red
    exit 1
}

# Step 5: Tag image for Heroku registry
Write-Host "`nStep 3: Tagging image for Heroku..."
docker tag $HEROKU_APP registry.heroku.com/$HEROKU_APP/web

# Step 6: Login to Heroku container registry
Write-Host "`nStep 4: Logging into Heroku container registry..."
heroku container:login

# Step 7: Push the image to Heroku
Write-Host "`nStep 5: Building and pushing image to Heroku (linux/amd64)..."
docker buildx build --platform linux/amd64 -t registry.heroku.com/$HEROKU_APP/web --push .

if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker buildx push failed." -ForegroundColor Red
    exit 1
}

# Step 8: Release the image
Write-Host "`nStep 6: Releasing image on Heroku..."
heroku container:release web -a $HEROKU_APP

# Step 9: Restart dyno to ensure latest image runs
Write-Host "`nStep 7: Restarting Heroku dynos..."
heroku ps:restart -a $HEROKU_APP

# Step 10: Show logs
Write-Host "`nStep 8: Viewing logs (press Ctrl+C to stop)...`n"
heroku logs --tail -a $HEROKU_APP