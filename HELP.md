# Read Me First

The following was discovered as part of building this project:

* No Docker Compose services found. As of now, the application won't start! Please add at least one service to the
  `compose.yaml` file.

# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.6/gradle-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.5.6/gradle-plugin/packaging-oci-image.html)
* [Spring Integration JPA Module Reference Guide](https://docs.spring.io/spring-integration/reference/jpa.html)
* [Spring Integration Test Module Reference Guide](https://docs.spring.io/spring-integration/reference/testing.html)
* [Spring Integration Security Module Reference Guide](https://docs.spring.io/spring-integration/reference/security.html)
* [Spring Integration HTTP Module Reference Guide](https://docs.spring.io/spring-integration/reference/http.html)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/3.5.6/reference/actuator/index.html)
* [Spring Configuration Processor](https://docs.spring.io/spring-boot/3.5.6/specification/configuration-metadata/annotation-processor.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/3.5.6/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Rest Repositories](https://docs.spring.io/spring-boot/3.5.6/how-to/data-access.html#howto.data-access.exposing-spring-data-repositories-as-rest)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/3.5.6/reference/using/devtools.html)
* [Docker Compose Support](https://docs.spring.io/spring-boot/3.5.6/reference/features/dev-services.html#features.dev-services.docker-compose)
* [Spring Integration](https://docs.spring.io/spring-boot/3.5.6/reference/messaging/spring-integration.html)
* [Spring Security](https://docs.spring.io/spring-boot/3.5.6/reference/web/spring-security.html)
* [Thymeleaf](https://docs.spring.io/spring-boot/3.5.6/reference/web/servlet.html#web.servlet.spring-mvc.template-engines)
* [Spring Web](https://docs.spring.io/spring-boot/3.5.6/reference/web/servlet.html)

### Guides

The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Accessing JPA Data with REST](https://spring.io/guides/gs/accessing-data-rest/)
* [Accessing Neo4j Data with REST](https://spring.io/guides/gs/accessing-neo4j-data-rest/)
* [Accessing MongoDB Data with REST](https://spring.io/guides/gs/accessing-mongodb-data-rest/)
* [Integrating Data](https://spring.io/guides/gs/integration/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
* [Handling Form Submission](https://spring.io/guides/gs/handling-form-submission/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

### Additional Links

These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

## Project Security

This project uses JWT for authentication. To access protected endpoints, you first need to register a user and then log in to obtain a JWT.

### User Registration

To create a new user, send a `POST` request to the `/auth/register` endpoint.

**Example using `curl`:**
```bash
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "name": "Test User", "mobileNumber": "1234567890", "address": "123 Test St", "password": "password"}'
```

### User Login (JWT Generation)

Once the user is registered, you can obtain a JWT by sending a `POST` request to the `/auth/login` endpoint with the user's credentials.

**Example using `curl`:**
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password"}'
```

The response will contain the JWT, which you can then use to authenticate subsequent requests.

### Docker Compose support

This project contains a Docker Compose file named `compose.yaml`.

However, no services were found. As of now, the application won't start!

Please make sure to add at least one service in the `compose.yaml` file.

---

## Installing Docker on Windows (Best Practices) and Alternatives

This section helps you install Docker on Windows and run this project using the provided `compose.yaml`. It also covers good practices and alternatives if you prefer not to use Docker Desktop.

### Recommended: Docker Desktop with WSL 2 backend

Prerequisites
- Windows 11 (recommended) or Windows 10 22H2+, 64-bit
- Hardware virtualization enabled in BIOS/UEFI
- Windows Subsystem for Linux 2 (WSL 2)

1) Enable WSL and Virtual Machine Platform
- Open PowerShell as Administrator and run:
  - dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart
  - dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart
- Restart your computer when prompted.

2) Install the WSL 2 Linux kernel update (if needed)
- In Admin PowerShell:
  - wsl --install
  - If you already have WSL, ensure version 2 is default: wsl --set-default-version 2

3) Install Docker Desktop (winget)
- In normal PowerShell:
  - winget install --id Docker.DockerDesktop --source winget
- After installation, start Docker Desktop and complete the first-run setup.

4) Configure Docker Desktop
- Settings > General: Enable “Use the WSL 2 based engine”.
- Settings > Resources: Adjust CPU/Memory as needed (e.g., 2 CPUs, 4–6 GB RAM for light dev work).
- Settings > WSL Integration: Enable integration for your default WSL distro (usually Ubuntu). This gives you docker commands inside WSL as well.

5) Verify installation
- Open a new PowerShell window and run:
  - docker version
  - docker run --rm hello-world
- You should see a success message from the hello-world image.

6) Run this project with Compose
- In PowerShell, navigate to your project root:
  - cd C:\Users\camilo.medinarivera\IdeaProjects\practice
- Start the stack:
  - docker compose up
- The app container exposes port 8080, so browse to http://localhost:8080
- Stop the stack with Ctrl+C, then clean up:
  - docker compose down

Good practices
- Prefer WSL 2 backend over Hyper-V for better performance and compatibility.
- Keep Docker Desktop updated.
- Avoid mapping large Windows folders into containers if possible; prefer a WSL filesystem path for heavy I/O.
- Set sensible resource limits (CPU/RAM) to keep your system responsive.
- Use named volumes (as in compose.yaml) for caches to improve build times.

Troubleshooting
- Docker Desktop not starting: Ensure virtualization is enabled and WSL is version 2. Try restarting the "Docker Desktop Service" from Services.msc.
- Permission denied on volumes: On Windows paths, try running PowerShell as your user (not admin) and avoid mapping protected folders.
- Slow file I/O: Place the project inside your WSL distro (e.g., \\wsl$\Ubuntu\home\<user>\project) for faster performance.
- Port already in use: Change the mapped port in compose.yaml (e.g., "8081:8080").

### Alternatives to Docker Desktop on Windows

1) Podman Desktop (rootless containers)
- Install: winget install --id RedHat.Podman-Desktop
- Enables Docker-compatible CLI via podman and podman-compose, with a Docker socket compatibility layer.
- Pros: Open source, rootless, good performance with WSL; Cons: Some Docker-specific features may not be identical.

2) Rancher Desktop
- Install: winget install --id SUSE.RancherDesktop
- Provides containerd or Moby runtimes and nerdctl; can enable Docker-compatible socket.
- Pros: Kubernetes-friendly; Cons: Different UX vs Docker Desktop.

3) Docker Engine inside WSL only (no Desktop)
- Install Docker Engine directly into your WSL distro (e.g., Ubuntu) following the official Linux instructions.
- Use docker from within WSL and expose services to Windows via localhost.
- Pros: Lightweight; Cons: Manual setup and updates, fewer Windows UI integrations.

Uninstall/Reset
- Uninstall Docker Desktop: winget uninstall Docker.DockerDesktop
- Reset Docker Desktop: Settings > Troubleshoot > Reset to factory defaults

Security notes
- Be mindful when mounting folders into containers; containers can modify files in mounted paths.
- Use regular user accounts where possible; avoid running containers with elevated privileges unless needed.

With Docker installed, you can use the provided compose.yaml to run the app for development quickly. If you want to build a production image later, consider adding a Dockerfile and using Spring Boot’s Boot Build Image or a multi-stage Dockerfile for JDK 21.

---

## Postman collections and environment

To quickly explore and test the API endpoints, Postman assets are included in this repository.

Files:
- postman\Practice_API.postman_collection.json
- postman\Local.postman_environment.json

How to import and use in Postman:
1) Open Postman and click Import.
2) Select both JSON files from the postman folder and import them.
3) In the top-right environment selector, choose Local.
4) Verify the base_url variable in the Local environment (for example: http://localhost:8081).
5) In the collection, start with Auth > Register to create a user.
6) Then run Auth > Login to obtain a JWT token. The collection scripts will store the token in a variable for subsequent requests.
7) Use the User endpoints with the stored token; the Authorization header is configured at the collection level.

Notes:
- If your application runs on a different port, adjust the base_url in the environment accordingly.
- You can duplicate the Local environment to create staging/production variants with different base URLs and credentials.


---

## Code style (IntelliJ): Google Java Style

An IntelliJ IDEA code style scheme is provided at the project root:
- java-google-style.xml

How to import in IntelliJ IDEA:
- Windows/Linux: File > Settings > Editor > Code Style
- macOS: IntelliJ IDEA > Settings > Editor > Code Style
- Click the gear icon next to the Scheme dropdown > Import Scheme > IntelliJ IDEA code style XML
- Select the java-google-style.xml file in the project root
- Apply

Notes:
- This scheme follows the official Google Java Style. Use it to reformat files (Code > Reformat Code) to keep a consistent style across the codebase.
\n## Release v0.0.10 - 2025-10-08
- feature/improve-github-workflow (#36)

## Release v0.0.11 - 2025-10-08

- Feature/improve GitHub workflow (#40)
- feature/improve-github-workflow (#38)
\n## Snapshot v0.0.12-SNAPSHOT - 2025-10-08
- docs(HELP): update for release v0.0.11 [skip ci]
- docs(HELP): update for release v0.0.10
