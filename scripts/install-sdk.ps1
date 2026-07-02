$ErrorActionPreference = "Stop"
$sdkDir = "$env:LOCALAPPDATA\Android\Sdk"
$toolsDir = "$sdkDir\cmdline-tools\latest"

if (!(Test-Path $toolsDir)) {
    Write-Host "Creating Directories..."
    New-Item -ItemType Directory -Force -Path "$sdkDir\cmdline-tools" | Out-Null
}

Write-Host "Downloading Android SDK Command-line Tools..."
$zipPath = "$env:TEMP\commandlinetools-win.zip"
Invoke-WebRequest -Uri "https://dl.google.com/android/repository/commandlinetools-win-10406996_latest.zip" -OutFile $zipPath

Write-Host "Extracting ZIP..."
Expand-Archive -Path $zipPath -DestinationPath "$sdkDir\cmdline-tools\temp" -Force

Write-Host "Moving files..."
if (Test-Path $toolsDir) {
    Remove-Item $toolsDir -Recurse -Force
}
Move-Item -Path "$sdkDir\cmdline-tools\temp\cmdline-tools" -Destination $toolsDir -Force
Remove-Item "$sdkDir\cmdline-tools\temp" -Recurse -Force
Remove-Item $zipPath -Force

Write-Host "Setting ANDROID_HOME Environment Variable..."
[Environment]::SetEnvironmentVariable("ANDROID_HOME", $sdkDir, "User")
$env:ANDROID_HOME = $sdkDir

Write-Host "Installing SDK Packages (platform-34, build-tools 34.0.0, platform-tools)..."
$sdkManager = "$toolsDir\bin\sdkmanager.bat"

echo "y" | & $sdkManager "platforms;android-34" "build-tools;34.0.0" "platform-tools"
echo "y" | & $sdkManager --licenses

Write-Host "Android SDK Installation Complete!"
