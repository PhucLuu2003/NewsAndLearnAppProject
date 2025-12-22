# Deploy Firestore Rules to Firebase
# Run this script to update security rules

Write-Host "🔥 Deploying Firestore Security Rules..." -ForegroundColor Cyan

# Check if Firebase CLI is installed
$firebaseInstalled = Get-Command firebase -ErrorAction SilentlyContinue

if (-not $firebaseInstalled) {
    Write-Host "❌ Firebase CLI not found!" -ForegroundColor Red
    Write-Host "Please install it first:" -ForegroundColor Yellow
    Write-Host "npm install -g firebase-tools" -ForegroundColor Yellow
    exit 1
}

# Login check
Write-Host "Checking Firebase login status..." -ForegroundColor Yellow
firebase login:list

# Deploy rules
Write-Host "`nDeploying rules..." -ForegroundColor Yellow
firebase deploy --only firestore:rules

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ Firestore rules deployed successfully!" -ForegroundColor Green
    Write-Host "The PERMISSION_DENIED error should be fixed now." -ForegroundColor Green
} else {
    Write-Host "`n❌ Deployment failed!" -ForegroundColor Red
    Write-Host "Please check the error message above." -ForegroundColor Yellow
}
