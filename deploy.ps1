# Heroku app name
$HEROKU_APP = "cst438-project2-group11"

# Step 1: Print header
Write-Host "`n==============================="
Write-Host "Deploying Docker Image to Heroku"
Write-Host "===============================`n"

# Step 2: Clean and rebuild the Spring Boot JAR
Write-Host "Step 1: Building Gradle project..."
Start-Process -FilePath "./gradlew.bat" -ArgumentList "clean", "build", "-x", "test" -Wait -NoNewWindow

# Step 3: Rebuild the Docker image
Write-Host "`nStep 2: Building Docker image..."
docker build -t $HEROKU_APP .

# Step 4: Tag image for Heroku registry
Write-Host "`nStep 3: Tagging image for Heroku..."
docker tag $HEROKU_APP registry.heroku.com/$HEROKU_APP/web

# Step 5: Login to Heroku container registry
Write-Host "`nStep 4: Logging into Heroku container registry..."
heroku container:login

# Step 6: Push the image to Heroku
Write-Host "`nStep 5: Pushing image to Heroku..."
docker push registry.heroku.com/$HEROKU_APP/web

# Step 7: Release the image
Write-Host "`nStep 6: Releasing image on Heroku..."
heroku container:release web -a $HEROKU_APP

# Step 8: Confirm app is live
Write-Host "`nStep 7: Opening app and viewing logs..."
Start-Process "https://dashboard.heroku.com/apps/$HEROKU_APP"
Write-Host "`nHeroku logs (press Ctrl+C to stop):`n"
heroku logs --tail -a $HEROKU_APP