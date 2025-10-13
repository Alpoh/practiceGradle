Despliegue en Azure con Azure CLI (Backend Spring Boot Java 21)

Requisitos previos
- Azure CLI instalado y autenticado (az login)
- Suscripción de Azure con permisos de Colaborador
- PowerShell 7+ en Windows
- Java 21 y Gradle Wrapper en el repo (incluido)

Estructura
- infra/azure/main.bicep: Infraestructura (App Service Plan Linux + Web App Java)
- infra/azure/provision.ps1: Provisiona RG/Plan/WebApp
- infra/azure/deploy-app.ps1: Compila y despliega el JAR al Web App

1) Provisionar infraestructura
Ejemplo (ajusta datos):

  $sub = "<SUBSCRIPTION_ID>"
  $rg  = "rg-practice-dev"
  $loc = "eastus"
  $app = "practice-api"
  pwsh .\infra\azure\provision.ps1 -SubscriptionId $sub -ResourceGroup $rg -Location $loc -AppName $app -SkuName B1 -SkuTier Basic

La salida mostrará Web App Name y URL.

2) Desplegar la aplicación (local)
Con el nombre del Web App devuelto en el paso anterior:

  $sub = "<SUBSCRIPTION_ID>"
  $rg  = "rg-practice-dev"
  $web = "<WEB_APP_NAME_SALIDA>"
  pwsh .\infra\azure\deploy-app.ps1 -SubscriptionId $sub -ResourceGroup $rg -WebAppName $web -Clean

Esto compila con Gradle y sube el JAR mediante az webapp deploy.

3) Variables/Configuración de la app
Puedes añadir App Settings editando main.bicep (param appSettings) o luego con:

  az webapp config appsettings set -g <RG> -n <WEB_APP_NAME> --settings KEY=VALUE

4) CI/CD con GitHub Actions
Workflow: .github/workflows/azure-webapp.yml
Requiere:
- Secret: AZURE_CREDENTIALS (JSON de un Service Principal con permisos sobre la suscripción)
  Ejemplo de creación:

    az ad sp create-for-rbac --name "gha-practice" --role contributor \
      --scopes /subscriptions/<SUBSCRIPTION_ID> --sdk-auth

  Copia el JSON de salida a GitHub > Settings > Secrets and variables > Actions > New secret.
- Variable: AZURE_WEBAPP_NAME (nombre del Web App provisionado)

El workflow compila y despliega el JAR en pushes a main o mediante workflow_dispatch.

5) Frontend
Si tienes un front aparte, opciones comunes:
- Azure Static Web Apps (ideal para SPA). Provisiona con az staticwebapp create y conecta a tu repo.
- Azure Storage Static Website + CDN.
- Azure App Service (Node) si es SSR.

Podemos añadir un bicep/flujo similar para el front cuando compartas detalles del proyecto de front (framework, build output, rutas).

Solución de problemas
- 403/401 al desplegar: verifica AZURE_CREDENTIALS y permisos del SP.
- Fallo al ejecutar: revisa Application Logs (az webapp log tail -n <WEB_APP_NAME> -g <RG>) y variables (WEBSITES_PORT=8080 ya incluido).
- Nombre en uso: cambia appName en provision.ps1; el nombre final es unique.


6) Verificar que la implementación y configuración son correctas

A. Verificación local (antes de subir a Azure)
- Ejecuta pruebas unitarias e integración:

  Windows PowerShell:
    .\gradlew.bat clean test

- Levanta la app localmente (puerto 8081 definido en application.properties):

    .\gradlew.bat bootRun

- Comprueba endpoints básicos en otra consola:

    curl -i http://localhost:8081/actuator/health
    curl -i http://localhost:8081/v3/api-docs

  Espera un 200 OK y, para health, un JSON con {"status":"UP"}.

- Opcional: Usa los archivos Postman de la carpeta postman/ con el environment Local.postman_environment.json.

B. Verificación en Azure (después del despliegue)
- Obtén el nombre del Web App y recurso:

    az webapp show -g <RG> -n <WEB_APP_NAME> -o table

- Usa el script de verificación automatizado (recomendado):

    $sub = "<SUBSCRIPTION_ID>"
    $rg  = "rg-practice-dev"
    $web = "<WEB_APP_NAME>"
    pwsh .\infra\azure\verify.ps1 -SubscriptionId $sub -ResourceGroup $rg -WebAppName $web

  El script:
  - Verifica que el Web App exista y obtiene la URL pública.
  - Espera a que la app esté lista (hasta 5 min) y valida:
    - GET / (200)
    - GET /actuator/health (status=UP)
    - GET /v3/api-docs (200)
    - GET /api/users (200 si no requiere auth; si requiere, mostrará warning)

- Verificación manual (alternativa):

    $url = "https://<WEB_APP_NAME>.azurewebsites.net"
    curl -i "$url"
    curl -i "$url/actuator/health"
    curl -i "$url/v3/api-docs"

  Si no ves 200 en health y api-docs, revisa logs:

    az webapp log tail -g <RG> -n <WEB_APP_NAME>

C. Verificación de CI/CD (GitHub Actions)
- Revisa el workflow .github/workflows/azure-webapp.yml
- Configura:
  - Secret AZURE_CREDENTIALS (JSON de az ad sp create-for-rbac --sdk-auth)
  - Variable AZURE_WEBAPP_NAME (nombre del Web App)
- Lanza el workflow manualmente (workflow_dispatch) o haz push a main. Debe compilar y desplegar sin errores.
- Agrega protección de ramas o workflows existentes (protect-branches.yml) según tu política.

D. Configuración de variables de app (App Settings)
- Si tu app requiere config adicional (por ejemplo JWT, mail, etc.), establece valores en el Web App:

    az webapp config appsettings set -g <RG> -n <WEB_APP_NAME> --settings KEY=VALUE

- Vuelve a ejecutar el script verify.ps1 para comprobar que sigue todo OK.

Notas
- El App Service ya está configurado para Java 21 y puerto 8080. Spring Boot escucha en 8081 localmente; Azure reverse proxy enruta al contenedor, por lo que no necesitas cambiar nada si expones HTTP estándar.
- Si modificas el path base o seguridad, ajusta los checks del verify.ps1 según tus rutas.
