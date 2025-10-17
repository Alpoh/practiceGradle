Project-specific development guidelines (advanced)

Scope
This document captures the details you actually need to build, test, and extend this Spring Boot 3.5 / Java 21 service. It consolidates conventions and verified commands from this repo. It intentionally omits generic Spring/Gradle basics.

Key stack
- Java 21 via Gradle toolchain (no local JDK switching needed if you use Gradle wrapper)
- Spring Boot 3.5, Security with JWT (HS256)
- MapStruct for DTO mapping, Lombok for boilerplate
- Vavr Either<Throwable, T> on service boundary
- H2 in-memory DB for dev/tests by default
- Tests: JUnit 5, Spring test slices (WebMvcTest, SpringBootTest), Mockito, AssertJ
- Code quality: Jacoco coverage, Sonar, Qodana

Build and run (verified)
- Use the Gradle wrapper (ensures Java 21 toolchain and plugins):
  - Windows PowerShell: .\gradlew.bat build
  - Run locally: .\gradlew.bat bootRun
- Runtime details
  - Port: 8081 (src/main/resources/application.properties)
  - H2 console: /h2-console (enabled by default)
  - JWT props (application.properties):
    - jwt.secret: base64-encoded 256-bit value suitable for HS256 (dev only)
    - jwt.expiration-ms: default token lifetime
- Dependency management: Boot + io.spring.dependency-management. Versions centralized at top of build.gradle.kts via extra properties.
- Publishing (optional): maven-publish to GitHub Packages. Requires env vars: GITHUB_REPOSITORY, GITHUB_ACTOR, GITHUB_TOKEN.
- Versioning: PROJECT_VERSION env var overrides Gradle project version; falls back to 0.0.1-SNAPSHOT.

Testing
Verified commands and patterns below come from this repository and were executed successfully during authoring.

How to run tests
- All tests (Gradle will resolve toolchain and run JUnit 5):
  - Windows: .\gradlew.bat test
- Single class by FQN (verified):
  - co.medina.starter.practice.user.api.UserControllerTest (10/10 passed on verification)
- Single method:
  - .\gradlew.bat test --tests "co.medina.starter.practice.user.api.UserControllerTest.POST /api/users - 201 Created happy path"
- Coverage (Jacoco):
  - .\gradlew.bat jacocoTestReport
  - HTML report: build/reports/jacoco/test/html/index.html

Adding and running a new test (demonstrated)
- Place new tests under src/test/java and mirror main package structure when relevant.
- Example created and executed during verification (then removed to keep repo clean):
  - File: src/test/java/co/medina/starter/practice/demo/DemoSampleTest.java
  - Content:
    package co.medina.starter.practice.demo;
    import org.junit.jupiter.api.Test;
    import static org.junit.jupiter.api.Assertions.assertEquals;
    class DemoSampleTest { @Test void demo_shouldAddNumbers() { assertEquals(5, 2 + 3); } }
  - Executed via FQN: co.medina.starter.practice.demo.DemoSampleTest
  - Result: 1/1 passed

Test slice guidance (existing patterns)
- Web layer
  - Use @WebMvcTest(controllers = {YourController.class, EitherResponseHandler.class}) so that exception mapping is active.
  - Disable security filters unless testing security: @AutoConfigureMockMvc(addFilters = false).
  - Mock collaborators with @MockitoBean (e.g., UserService, JwtUtil, UserDetailsService, MapStruct mappers).
  - Adjust base context only when necessary using @TestPropertySource(properties = "server.servlet.context-path=/v1").
  - See src/test/java/co/medina/starter/practice/user/api/UserControllerTest.java for JSON assertions, stubbing with BDDMockito, and pagination with PageImpl.
- Service layer
  - Prefer plain JUnit + Mockito with @Mock and @InjectMocks (do not load Spring context).
  - Methods return Either<Throwable, T>: test both left/right branches; check exception messages/types so EitherResponseHandler maps correctly.
  - Refer to src/test/java/co/medina/starter/practice/user/service/UserServiceImplMoreTest.java for verification of repository interactions and Either assertions.
- Context smoke
  - PracticeApplicationTests uses @SpringBootTest to ensure the context wires.

Mockito agent note
- On recent JDKs you may see a ByteBuddy/Mockito dynamic agent warning during tests. It is currently harmless. To silence locally if desired:
  - Set env var JAVA_TOOL_OPTIONS="-XX:+EnableDynamicAgentLoading" OR
  - Configure Mockito as a Java agent as per Mockito docs.

Either error handling contract (web boundary)
- Services expose Either<Throwable, T>. The controller layer delegates error-to-HTTP mapping to EitherResponseHandler:
  - NoSuchElementException -> 404 Not Found (message propagated)
  - DataIntegrityViolationException -> 409 Conflict
  - Any other Throwable -> 500 Internal Server Error with generic message
- When implementing service methods, choose descriptive exception types/messages for Left to ensure correct HTTP mapping.

Mapping and annotation processing
- MapStruct mappers are declared with @Mapper(componentModel = "spring"). Example: UserMapper.
- For partial DTOs, prefer @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE) to avoid compile-time failures when target fields aren’t all mapped.
- Lombok and MapStruct processors are configured in build.gradle.kts; ensure annotation processing is enabled in your IDE if running tests there.

Security notes for tests
- Spring Security is enabled in the app. In controller tests, add @AutoConfigureMockMvc(addFilters = false) unless the test explicitly targets security.
- For security-focused tests, pull in spring-security-test and use its helpers; don’t disable filters in those tests.

Data layer and defaults
- Default datasource is H2 in-memory via application.properties. For repository tests, you can use @DataJpaTest and override properties as needed (current suite focuses on web/service slices with mocks).

Code style and static analysis
- Style: java-google-style.xml at repo root. Configure your IDE formatter accordingly.
- Sonar: sonar-project.properties configured for sources/tests, binaries, JUnit reports, and Jacoco XML. Test files (*Test.java) are excluded from source scanning to avoid duplication.
- Qodana: qodana.yaml is set for JVM Community and JDK 21. Integrate with CI if you need gating.

CI/CD
- GitHub Actions workflows:
  - .github/workflows/snapshot.yml: build/test and publish snapshots.
  - .github/workflows/release.yml: build/test and publish on tags/releases.
  - .github/workflows/protect-branches.yml: branch protections.
- Jacoco artifacts can be uploaded in CI; HTML is at build/reports/jacoco/test/html/index.html.

Runtime endpoints and context
- App base port is 8081. Test slices may set a temporary context-path per-test using @TestPropertySource; the application itself does not set a custom context-path by default beyond the port.

Practical tips
- Prefer constructor injection in services for testability.
- Keep controllers thin; push branching into services returning Either.
- When adding DTOs, also add/adjust MapStruct mappers and mock them in your @WebMvcTest.
- For pagination in controller tests, use new PageImpl<>(...) to stub repository/service responses and assert JSON structure accordingly.

Reference commands (Windows PowerShell)
- Build: .\gradlew.bat build
- Run: .\gradlew.bat bootRun
- All tests: .\gradlew.bat test
- One class: .\gradlew.bat test --tests "co.medina.starter.practice.user.api.UserControllerTest"
- One method: .\gradlew.bat test --tests "co.medina.starter.practice.user.api.UserControllerTest.POST /api/users - 201 Created happy path"
- Coverage report: .\gradlew.bat jacocoTestReport