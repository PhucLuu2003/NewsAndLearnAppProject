#!/usr/bin/env pwsh
# Clean Rebuild Script

Write-Host "🧹 Cleaning project..." -ForegroundColor Yellow
./gradlew clean

Write-Host "🔨 Building project..." -ForegroundColor Yellow
./gradlew assembleDebug

Write-Host "✅ Build complete!" -ForegroundColor Green
Write-Host "📱 APK location: app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Uninstall old app from your phone" -ForegroundColor White
Write-Host "2. Run the app from Android Studio" -ForegroundColor White
