# Practice (Spring Boot 3.5 / Java 21)

A small Spring Boot service demonstrating a pragmatic stack for building secure REST APIs with JWT, Vavr Either-based service boundaries, MapStruct DTO mapping, and an in-memory H2 database for local development and tests.

This README documents the actual tech stack, how to build/run/test, environment variables, and useful project structure. It avoids guessing unknowns and calls them out explicitly.


## Overview
- Language/runtime: Java 21
- Frameworks/libraries: Spring Boot 3.5 (Web, Security, Data JPA, Validation), Spring Integration, Springdoc OpenAPI UI, MapStruct, Lombok, Vavr Either
- Auth: JWT (HS256)
- Database (dev/test): H2 in-memory
- Build tool: Gradle (wrapper provided)
- Tests: JUnit 5, Spring Test (WebMvcTest, SpringBootTest), Mockito, AssertJ
- Code quality: JaCoCo coverage, Sonar, Qodana
- Port: 8081 (configurable)

Entry point: `co.medina.starter.practice.PracticeApplication`.


## Requirements
- Java is managed via Gradle toolchain (no need to switch local JDK). The Gradle wrapper will provision Java 21 for builds/tests.
- Windows PowerShell commands below are verified for this repository. On macOS/Linux, replace `gradlew.bat` with `./gradlew`.
- Docker (optional) if you want to use the provided `compose.yaml` to run via a containerized Gradle image.


## Getting started

Build (compiles, runs tests, and produces coverage):
- Windows: `.\u0067radlew.bat build`

Run the app locally (port 8081 by default):
- Windows: `.\u0067radlew.bat bootRun`
- Browse H2 console: http://localhost:8081/h2-console
- OpenAPI UI (if enabled/available at runtime): http://localhost:8081/swagger-ui/index.html (see TODO below)

Run with Docker Compose (optional):
- Requires Docker/Compose. The service uses the Gradle 8 JDK 21 image to execute `gradle bootRun`.
- Command: `docker compose up` (in the project root; uses `compose.yaml`)

Notes:
- Security is enabled. For controller tests we disable filters; in runtime you will need to obtain a JWT to call protected endpoints.
- H2 is in-memory; data resets on restart.


## Configuration and environment variables
Application properties (defaults in `src/main/resources/application.properties`):
- Server
  - `server.port=8081`
- H2 (dev/test)
  - `spring.datasource.url=jdbc:h2:mem:practice;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
  - `spring.h2.console.enabled=true`
  - `spring.h2.console.path=/h2-console`
- JWT
  - `jwt.secret` (base64-encoded 256-bit key for HS256; for dev only)
  - `jwt.expiration-ms` (token lifetime in ms)
- Email (optional; when unset, emails are no-op/logged)
  - `app.base-url` (used for email links, defaults to `http://localhost:${server.port}`)
  - `spring.mail.*` (see commented examples in application.properties)

Build/publish related:
- `PROJECT_VERSION` (overrides Gradle project `version`)
- `GITHUB_REPOSITORY`, `GITHUB_ACTOR`, `GITHUB_TOKEN` (for `maven-publish` to GitHub Packages; optional)
- Optional test runtime noise suppression:
  - `JAVA_TOOL_OPTIONS=-XX:+EnableDynamicAgentLoading` (silences ByteBuddy/Mockito agent warning)

Profiles:
- `SPRING_PROFILES_ACTIVE` can be set as usual (Compose example sets `default`).


## API quick start (auth flow)
Basic flow to obtain a JWT and call secured endpoints:
- Register: `POST /auth/register` with a JSON body containing at least email and password plus required user fields.
- Login: `POST /auth/login` with `{"email":"...","password":"..."}` to receive a JWT (`AuthResponse`).
- Use the JWT in `Authorization: Bearer <token>` to call protected resources (e.g., `/api/users`).

See `src/test/java/co/medina/starter/practice/auth/AuthControllerTest.java` and `HELP.md` for sample requests.


## Scripts and common Gradle tasks
- Build all: `.\u0067radlew.bat build`
- Run app: `.\u0067radlew.bat bootRun`
- Run all tests: `.gradlew.bat test` (Note: replace `` with nothing if copied; correct command is `gradlew.bat test`)
- One test class: `.\u0067radlew.bat test --tests "co.medina.starter.practice.user.api.UserControllerTest"`
- One test method: `.\u0067radlew.bat test --tests "co.medina.starter.practice.user.api.UserControllerTest.POST /api/users - 201 Created happy path"`
- Coverage report (JaCoCo): `.\u0067radlew.bat jacocoTestReport` (see HTML at `build/reports/jacoco/test/html/index.html`)

OpenAPI generation:
- The plugin `org.springdoc.openapi-gradle-plugin` is applied. Specific tasks/routes depend on configuration.
- TODO: Document the exact generation/run command and endpoint if applicable.


## Testing
- Full suite: `.\u0067radlew.bat test`
- Examples:
  - Web layer tests: `src/test/java/.../user/api/UserControllerTest.java` (uses `@WebMvcTest` and `EitherResponseHandler` mapping)
  - Service tests: `src/test/java/.../user/service/UserServiceImplMoreTest.java` (Either left/right branches)
  - Context smoke: `src/test/java/.../PracticeApplicationTests.java`

Guidelines:
- Use `@WebMvcTest(controllers = {YourController.class, EitherResponseHandler.class})` for controller tests.
- Disable filters unless testing security: `@AutoConfigureMockMvc(addFilters = false)`.
- Mock collaborators with `@MockitoBean`.
- Service tests use Mockito without Spring context; methods return `Either<Throwable, T>`.


## Project structure (selected)
```
.
├─ build.gradle.kts
├─ settings.gradle.kts
├─ compose.yaml
├─ src
│  ├─ main
│  │  ├─ java/co/medina/starter/practice
│  │  │  ├─ PracticeApplication.java
│  │  │  ├─ auth/ (controllers + DTOs for auth)
│  │  │  ├─ config/ (SecurityConfig)
│  │  │  ├─ security/ (JwtUtil, JwtAuthenticationFilter, CustomUserDetailsService)
│  │  │  └─ user/ (api, domain, repo, service, mappers)
│  │  └─ resources/application.properties
│  └─ test/java/co/medina/starter/practice
│     ├─ PracticeApplicationTests.java
│     ├─ auth/AuthControllerTest.java
│     └─ user/... (controller + service tests)
├─ HELP.md (additional tips and sample curl)
├─ sonar-project.properties (Sonar config)
├─ qodana.yaml (Qodana config)
└─ .github/workflows/*.yml (CI)
```


## CI/CD
- GitHub Actions workflows:
  - `.github/workflows/snapshot.yml` (build/test and publish snapshots)
  - `.github/workflows/release.yml` (publish on tags/releases)
  - `.github/workflows/protect-branches.yml`

Artifacts:
- JaCoCo HTML: `build/reports/jacoco/test/html/index.html`


## Development notes
- Prefer constructor injection for services.
- Keep controllers thin; let services return `Either<Throwable, T>` and delegate error mapping to `EitherResponseHandler`:
  - `NoSuchElementException` -> 404
  - `DataIntegrityViolationException` -> 409
  - Others -> 500 with generic message
- MapStruct mappers are `@Mapper(componentModel = "spring")`.


## License
- TODO: No LICENSE file present. Decide on a license (e.g., Apache-2.0, MIT) and add `LICENSE` to the repository. Update this section accordingly.


## Links
- Spring Boot docs: https://docs.spring.io/spring-boot/
- Gradle docs: https://docs.gradle.org
- Spring Security: https://docs.spring.io/spring-security/
- Springdoc OpenAPI: https://springdoc.org/


## Troubleshooting
- If Mockito/ByteBuddy warns about dynamic agent loading on recent JDKs, set `JAVA_TOOL_OPTIONS=-XX:+EnableDynamicAgentLoading` while running tests.
- If Compose complains about ports in use, stop local instance or change `server.port`.
- H2 console login: JDBC URL `jdbc:h2:mem:practice`, user `sa`, empty password.
