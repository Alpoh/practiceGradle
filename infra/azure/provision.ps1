param(
  [Parameter(Mandatory=$true)][string]$SubscriptionId,
  [Parameter(Mandatory=$true)][string]$ResourceGroup,
  [Parameter(Mandatory=$true)][string]$Location,
  [Parameter(Mandatory=$true)][string]$AppName,
  [string]$SkuName = 'B1',
  [string]$SkuTier = 'Basic'
)

# Login if needed
try { az account show | Out-Null } catch { az login }

az account set --subscription $SubscriptionId

# Create RG if not exists
$rgExists = az group exists --name $ResourceGroup | ConvertFrom-Json
if (-not $rgExists) {
  az group create --name $ResourceGroup --location $Location | Out-Null
}

# Deploy Bicep
$deploymentName = "deploy-$(Get-Date -Format yyyyMMddHHmmss)"
az deployment group create `
  --name $deploymentName `
  --resource-group $ResourceGroup `
  --template-file "$PSScriptRoot\main.bicep" `
  --parameters appName=$AppName skuName=$SkuName skuTier=$SkuTier | Tee-Object -Variable deployOut | Out-Null

# Output
$webAppName = ($deployOut.properties.outputs.webAppName.value)
$webAppUrl  = ($deployOut.properties.outputs.webAppUrl.value)

Write-Host "Web App Name: $webAppName"
Write-Host "Web App URL:  $webAppUrl"