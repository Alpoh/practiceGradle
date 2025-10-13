@description('Azure Region for all resources')
param location string = resourceGroup().location

@description('Base name for the Web App (will be used to generate a globally unique name)')
param appName string

@description('App Service Plan SKU (e.g., B1, P1v3)')
param skuName string = 'B1'

@description('App Service Plan tier (e.g., Basic, PremiumV3)')
param skuTier string = 'Basic'

@description('Java version to run')
param javaVersion string = 'Java 21'

@description('Whether to enable HTTPS only')
param httpsOnly bool = true

@description('App settings to inject (name/value pairs)')
@secure()
param appSettings object = {}

var planName = '${appName}-plan'
var siteName = toLower('${appName}-${uniqueString(resourceGroup().id, appName)}')

resource plan 'Microsoft.Web/serverfarms@2023-01-01' = {
  name: planName
  location: location
  sku: {
    name: skuName
    tier: skuTier
    capacity: 1
  }
  kind: 'linux'
  properties: {
    reserved: true // Linux
  }
}

resource site 'Microsoft.Web/sites@2023-01-01' = {
  name: siteName
  location: location
  kind: 'app,linux'
  properties: {
    serverFarmId: plan.id
    httpsOnly: httpsOnly
    siteConfig: {
      linuxFxVersion: 'JAVA|${javaVersion}'
      alwaysOn: true
      appSettings: [
        for k in union(appSettings, {
          'WEBSITE_STACK': 'java'
          'WEBSITE_RUN_FROM_PACKAGE': '1'
          'WEBSITE_ENABLE_SYNC_UPDATE_SITE': 'true'
          'WEBSITES_PORT': '8080'
          'JAVA_VERSION': javaVersion
        }): {
          name: k
          value: string(appSettings[k])
        }
      ]
    }
  }
}

output webAppName string = site.name
output webAppUrl string = 'https://${site.name}.azurewebsites.net'