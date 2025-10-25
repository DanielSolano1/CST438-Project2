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

# Collect GitHub-related environment variables (if set) and pass them into the container.
$envArgs = @()
$vars = @('GITHUB_CLIENT_ID','GITHUB_CLIENT_SECRET','GITHUB_AUTHORIZE_URI','GITHUB_TOKEN_URI','GITHUB_REDIRECT_URI','GITHUB_SCOPE')
foreach ($v in $vars) {
    $val = Get-Item -Path Env:$v -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Value -ErrorAction SilentlyContinue
    if (![string]::IsNullOrEmpty($val)) {
        $envArgs += "-e"
        # Use Key=Value form so the container receives the same variable name
        $envArgs += "${v}=$val"
    }
}

if ($envArgs.Count -eq 0) {
    Write-Host "No GITHUB_* environment variables detected. Attempting to read test properties from src/test/resources/application-test.properties and pass them to the container." -ForegroundColor Yellow
    $testPropsPath = Join-Path (Get-Location) "src\test\resources\application-test.properties"
    if (Test-Path $testPropsPath) {
        $lines = Get-Content $testPropsPath | ForEach-Object { $_.Trim() } | Where-Object { $_ -and -not ($_.StartsWith('#')) }
        foreach ($line in $lines) {
            if ($line -match '^github\.(?<key>[a-zA-Z0-9\-]+)=(?<val>.*)$') {
                $k = $matches['key'] -replace '\-','_' -replace '\.','_' 
                $v = $matches['val']
                $envName = ("GITHUB_{0}" -f $k).ToUpper()
                $envArgs += "-e"
                $envArgs += "${envName}=$v"
            }
        }
        # Also activate test profile inside the container so non-test resources pick up correctly
        $envArgs += "-e"
        $envArgs += "SPRING_PROFILES_ACTIVE=test"
    } else {
        Write-Host "Test properties file not found; running container with 'test' profile only." -ForegroundColor Yellow
        $envArgs += "-e"
        $envArgs += "SPRING_PROFILES_ACTIVE=test"
    }
}

# Build the docker run command arguments
$dockerArgs = @("run","-d","--name","project2-container","-p","8080:8080") + $envArgs + @("cst438-project2")
Write-Host "docker" $dockerArgs -ForegroundColor Cyan
$proc = Start-Process -FilePath "docker" -ArgumentList $dockerArgs -NoNewWindow -PassThru -Wait
if ($proc.ExitCode -ne 0) {
    Write-Host "docker run failed with exit code $($proc.ExitCode)" -ForegroundColor Red
    exit $proc.ExitCode
}

# Give the container a few seconds to start
Write-Host "`nWaiting for Spring Boot app to start..."
Start-Sleep -Seconds 5

# Step 6: Open browser automatically
Write-Host "`nStep 5: Opening app in Chrome..."
Start-Process "chrome.exe" "http://localhost:8080"


# Step 6: Show logs
Write-Host "`nStep 6: Showing logs (Ctrl+C to exit)...`n"
docker logs -f project2-container