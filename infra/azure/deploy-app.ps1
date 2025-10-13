param(
  [Parameter(Mandatory=$true)][string]$SubscriptionId,
  [Parameter(Mandatory=$true)][string]$ResourceGroup,
  [Parameter(Mandatory=$true)][string]$WebAppName,
  [switch]$Clean
)

# Ensure Gradle wrapper exists
if (-not (Test-Path "$PSScriptRoot\..\..\gradlew.bat")) {
  Write-Error "gradlew.bat not found at repo root"
  exit 1
}

# Login if needed
try { az account show | Out-Null } catch { az login }

az account set --subscription $SubscriptionId

Push-Location "$PSScriptRoot\..\.."
try {
  if ($Clean) { ./gradlew.bat clean }
  ./gradlew.bat build -x test
  $jar = Get-ChildItem -Path ".\build\libs" -Filter "*.jar" | Where-Object { $_.Name -notmatch "plain" } | Select-Object -First 1
  if (-not $jar) { Write-Error "No JAR found in build/libs"; exit 1 }
  Write-Host "Deploying $($jar.FullName) to $WebAppName..."

  az webapp deploy `
    --resource-group $ResourceGroup `
    --name $WebAppName `
    --type jar `
    --src-path $jar.FullName `
    --async false
}
finally {
  Pop-Location
}