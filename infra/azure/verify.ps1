param(
  [Parameter(Mandatory=$true)][string]$SubscriptionId,
  [Parameter(Mandatory=$true)][string]$ResourceGroup,
  [Parameter(Mandatory=$true)][string]$WebAppName,
  [int]$TimeoutSec = 300,
  [int]$RetrySec = 10
)

# Helper to perform GET and validate JSON path or status
function Invoke-Check {
  param(
    [Parameter(Mandatory=$true)][string]$Url,
    [int]$ExpectedStatus = 200,
    [string]$JsonPath = $null, # e.g. '$.status'
    [string]$ExpectedValue = $null
  )
  Write-Host "[CHECK] GET $Url"
  try {
    $resp = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 30
    if ($resp.StatusCode -ne $ExpectedStatus) {
      Write-Error "Unexpected status code: $($resp.StatusCode) (expected $ExpectedStatus)"
      return $false
    }
    if ($JsonPath) {
      try {
        $json = $resp.Content | ConvertFrom-Json
        $scriptBlock = [scriptblock]::Create($JsonPath.TrimStart('$').Replace('.', '.'))
        # Simple property navigation without JMES; support $.a.b
        $val = $json
        foreach ($part in $JsonPath.TrimStart('$').TrimStart('.').Split('.')) {
          if ($part) { $val = $val.$part }
        }
        if ($null -eq $val) { Write-Error "JSON path $JsonPath not found"; return $false }
        if ($ExpectedValue -and ($val.ToString() -ne $ExpectedValue)) {
          Write-Error "Unexpected JSON value at $JsonPath: '$val' (expected '$ExpectedValue')"
          return $false
        }
      } catch {
        Write-Error "Failed to parse JSON or evaluate path $JsonPath: $($_.Exception.Message)"
        return $false
      }
    }
    Write-Host "[OK] $Url"
    return $true
  }
  catch {
    Write-Error "Request failed: $($_.Exception.Message)"
    return $false
  }
}

# Ensure Azure login
try { az account show | Out-Null } catch { az login }

az account set --subscription $SubscriptionId

# Validate web app exists and get default hostname
$site = az webapp show -g $ResourceGroup -n $WebAppName | ConvertFrom-Json
if (-not $site) { Write-Error "Web App not found: $WebAppName in RG $ResourceGroup"; exit 1 }
$baseUrl = "https://$($site.defaultHostName)"
Write-Host "Target base URL: $baseUrl"

# Warm-up and wait for readiness up to TimeoutSec
$deadline = (Get-Date).AddSeconds($TimeoutSec)
$ready = $false
while ((Get-Date) -lt $deadline) {
  if (Invoke-Check -Url "$baseUrl" -ExpectedStatus 200) { $ready = $true; break }
  Write-Host "Waiting $RetrySec sec for app to become ready..."
  Start-Sleep -Seconds $RetrySec
}
if (-not $ready) { Write-Error "App did not become ready within $TimeoutSec seconds"; exit 2 }

# Health endpoint (Spring Actuator)
$healthOk = $false
foreach ($path in @('/actuator/health', '/actuator')) {
  if (Invoke-Check -Url "$baseUrl$path" -ExpectedStatus 200 -JsonPath '$.status' -ExpectedValue 'UP') { $healthOk = $true; break }
}
if (-not $healthOk) {
  Write-Warning "Actuator health did not return status=UP; continuing but review logs."
}

# OpenAPI docs endpoint
$docsOk = Invoke-Check -Url "$baseUrl/v3/api-docs" -ExpectedStatus 200
if (-not $docsOk) { Write-Warning "OpenAPI docs not accessible." }

# Sample domain call: list users endpoint (adjust if your API path differs)
$userOk = Invoke-Check -Url "$baseUrl/api/users" -ExpectedStatus 200
if (-not $userOk) { Write-Warning "Users endpoint not accessible (may require auth)." }

Write-Host "Verification complete. Review warnings above if any."
exit 0