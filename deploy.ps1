# ============================================
# Heroku Docker Deployment Script
# For: cst438-project2-group11
# ============================================

$HEROKU_APP = "cst438-project2-group11"

Write-Host ""
Write-Host "==============================="
Write-Host " Deploying Docker Image to Heroku" 
Write-Host "==============================="
Write-Host ""

# Step 1: Clean and build Spring Boot JAR
Write-Host "Step 1: Building Gradle project (skip tests)..." 
Start-Process -FilePath "./gradlew.bat" -ArgumentList "clean", "build", "-x", "test" -Wait -NoNewWindow

if (-Not (Test-Path "build/libs")) {
    Write-Host "ERROR: Gradle build failed — JAR not found." 
    exit 1
}

# Step 2: Login to Heroku container registry
Write-Host "`nStep 2: Logging into Heroku container registry..."
heroku container:login
if ($LASTEXITCODE -ne 0) {
    Write-Host "Heroku login failed."
    exit 1
}

# Step 3: Build single-arch Docker image (BuildKit disabled)
Write-Host "`nStep 3: Building single-arch image for Heroku (BuildKit disabled)..." 
$Env:DOCKER_BUILDKIT = "0"
$Env:DOCKER_DEFAULT_PLATFORM = "linux/amd64"

docker build -t $HEROKU_APP .
if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker build failed." -ForegroundColor Red
    exit 1
}

docker tag $HEROKU_APP registry.heroku.com/$HEROKU_APP/web

# Re-enable BuildKit for later commands
$Env:DOCKER_BUILDKIT = "1"

# Step 4: Push image to Heroku
Write-Host "`nStep 4: Pushing image to Heroku registry..."
docker push registry.heroku.com/$HEROKU_APP/web
if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker push failed." -ForegroundColor Red
    exit 1
}

# Step 5: Release image to Heroku app
Write-Host "`nStep 5: Releasing image to Heroku app..."
heroku container:release web -a $HEROKU_APP
if ($LASTEXITCODE -ne 0) {
    Write-Host "Heroku release failed." -ForegroundColor Red
    exit 1
}

# 🔁 Step 6: Restart dynos
Write-Host "`nStep 6: Restarting Heroku dynos..."
heroku ps:restart -a $HEROKU_APP

# Step 7: Check dyno status
Write-Host "`nStep 7: Checking Heroku dyno status..."
heroku ps -a $HEROKU_APP

# Step 8: Show final success summary
Write-Host ""
Write-Host "Deployment complete!" 
Write-Host "Your app should now be live at:" 
Write-Host "https://$HEROKU_APP.herokuapp.com/" 
Write-Host ""
Write-Host "To view logs in real-time, run:" 
Write-Host "   heroku logs --tail -a $HEROKU_APP" 
Write-Host ""
