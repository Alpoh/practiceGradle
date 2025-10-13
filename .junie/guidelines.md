Project Guidelines for Advanced Contributors

Overview
This repository is a Spring Boot 3.5 (Java 21) service with JWT-based auth, a simple User domain, and a REST API. It uses:
- Gradle Kotlin DSL with toolchain set to Java 21
- Testing stack: JUnit 5, Spring Boot test slices (WebMvcTest, SpringBootTest), Mockito, AssertJ
- MapStruct for DTO mapping, Lombok for boilerplate, Vavr Either for error handling
- H2 in-memory DB by default for dev/tests
- Code quality: Jacoco coverage, Sonar config, Qodana config

Build and Configuration
- JDK/Toolchain: Gradle enforces Java 21 (java.toolchain). Install JDK 21 locally if not using the toolchain.
- Build: Use the Gradle wrapper.
  - Windows PowerShell: .\gradlew.bat build
  - Unix/macOS: ./gradlew build
- Run app locally: .\gradlew.bat bootRun
  - Server runs on port 8081 (see src/main/resources/application.properties)
  - H2 console: /h2-console (enabled)
  - Base context path for WebMvcTest examples is set via TestPropertySource where needed
- Dependency Management: Spring Boot + io.spring.dependency-management plugin. Versions are centralized at top of build.gradle.kts via extra properties.
- Annotation Processors: Lombok, MapStruct, Spring configuration processor are configured. No extra IDE config should be needed if annotation processing is enabled.
- JWT: jwt.secret and jwt.expiration-ms in application.properties. The default secret is a base64-encoded 256-bit key suitable for HS256 in dev.
- Profiles: Only default properties file is committed; introduce application-<profile>.properties as needed.
- Publishing (optional): The maven-publish block is configured for GitHub Packages. Set env vars in CI/local to publish:
  - GITHUB_REPOSITORY, GITHUB_ACTOR, GITHUB_TOKEN
- Versioning: PROJECT_VERSION environment variable overrides the Gradle project version; defaults to 0.0.1-SNAPSHOT.

Testing
- Frameworks/Libraries:
  - JUnit 5 (useJUnitPlatform)
  - Spring Boot test starters (context tests and web-slice tests)
  - Mockito (+ ByteBuddy). Note: you may see a warning about dynamic agent loading on newer JDKs; it is harmless for now. To silence in CI later, consider adding Mockito as a Java agent per Mockito docs or enabling -XX:+EnableDynamicAgentLoading when appropriate.
  - AssertJ is used in some unit tests for fluent assertions.
- Running tests:
  - All tests: .\gradlew.bat test
  - Single class: .\gradlew.bat test --tests "co.medina.starter.practice.user.api.UserControllerTest"
  - Single method: .\gradlew.bat test --tests "co.medina.starter.practice.user.api.UserControllerTest.create_shouldReturn201"
  - Generate coverage report (auto runs after tests due to finalizedBy): .\gradlew.bat jacocoTestReport
    - Reports: build/reports/jacoco/test/html/index.html (HTML), XML at build/reports/jacoco/test/jacocoTestReport.xml
- Test slices and patterns in this codebase:
  - Web layer tests: @WebMvcTest with @AutoConfigureMockMvc(addFilters = false). Security filters are disabled in tests unless a security scenario is under test. Use @MockitoBean to provide mocks for collaborators (UserService, JwtUtil, UserDetailsService, mappers). See UserControllerTest for patterns:
    - Stubbing with BDDMockito.given(...).willReturn(...)
    - Validating HTTP status and JSON payloads via MockMvc
    - Using @TestPropertySource(properties = "server.servlet.context-path=/v1") to set base context at test runtime
  - Service unit tests: Use plain JUnit + Mockito with @Mock and @InjectMocks (no spring context). See UserServiceImplMoreTest for examples of:
    - Stubbing repositories
    - Verifying interactions (never/save)
    - Asserting Vavr Either results (isRight/isLeft) and exception types/messages
  - Context smoke test: @SpringBootTest in PracticeApplicationTests validates that the Spring context starts and the application bean wires correctly.
- Data layer:
  - Default datasource: in-memory H2 configured in application.properties. For repository tests, you can use @DataJpaTest and override properties if needed. Current suite focuses on service/web slices with mocked collaborators.
- Adding a new test (guidelines):
  - Place tests under src/test/java mirrored to main package structure.
  - For controller tests:
    - Use @WebMvcTest(controllers = {YourController.class, EitherResponseHandler.class}) to include exception handling advice.
    - Mock all external dependencies with @MockitoBean.
    - If your controller depends on mapping, mock your MapStruct mapper.
    - If security is not the subject of the test, add @AutoConfigureMockMvc(addFilters = false).
    - Use TestPropertySource to adjust context-path for endpoint paths if your controller assumes one.
  - For service tests:
    - Prefer constructor-injected @InjectMocks and @Mock; avoid loading Spring.
    - When methods return Either<Throwable, T>, assert both left and right branches as applicable, and check messages for ApiError mapping.
  - Naming: Adopt clear method names that state scenario and expectation, e.g., method_underTest_shouldReturnExpected_whenCondition.
  - Assert style: Prefer AssertJ or JUnit assertions consistently; use MockMvc JSON path assertions for REST responses.

Demonstrated test addition
- A simple JUnit test (DemoSampleTest) was created and executed locally to demonstrate adding and running a new test. It has been removed to keep the repo clean; you can create a similar test if you need a template:
  package co.medina.starter.practice.demo;
  import org.junit.jupiter.api.Test;
  import static org.junit.jupiter.api.Assertions.assertEquals;
  class DemoSampleTest { @Test void demo_shouldAddNumbers() { assertEquals(5, 2 + 3); } }

Error handling and Either mapping
- The API uses Vavr Either<Throwable, T> in the service boundary. The web layer converts left(Throwable) to ApiError via EitherResponseHandler:
  - NoSuchElementException -> 404 Not Found with error message
  - DataIntegrityViolationException -> 409 Conflict
  - Others -> 500 Internal Server Error with a generic message
- When adding service methods, return descriptive exception types/messages in the Left branch so the handler maps them correctly.

Mapping
- MapStruct is configured via annotationProcessor; mappers are declared with @Mapper(componentModel = "spring"). See UserMapper. For new DTOs:
  - Place interfaces in a relevant package, annotate with @Mapper(componentModel = "spring").
  - Define mapping methods; consider @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE) to avoid compile-time errors when not all fields are covered.
  - When using in controllers/services, inject and use directly; mock them in tests.

Security
- Spring Security is enabled in the app, with a JwtAuthenticationFilter and a CustomUserDetailsService. In controller tests we disable filters unless testing security explicitly. For security-focused tests, use spring-security-test helpers and avoid disabling filters.

Code Style and Static Analysis
- Style: java-google-style.xml is included. Configure your IDE to use it for consistent formatting.
- Qodana: qodana.yaml is set for JVM Community linter and JDK 21. Integrate with CI to gate on issues if desired.
- Sonar: sonar-project.properties is configured for sources/tests, binaries, JUnit reports, and Jacoco XML coverage. Excludes *Test.java from "sources" scanning to avoid duplication.

CI/CD
- GitHub Actions workflows exist for branch protection and release flows (see .github/workflows/*):
  - snapshot.yml: build/test and publish snapshots when appropriate.
  - release.yml: build/test and publish on tags/releases.
  - protect-branches.yml: enforces branch policies.
- Jacoco report generation is tied to tests and will be available in build/reports/jacoco/test/html/index.html in CI artifacts if uploaded.

Useful Gradle tasks
- test: Runs unit/integration tests
- jacocoTestReport: Generates code coverage (XML+HTML)
- bootRun: Runs the app
- clean: Cleans build artifacts

Local development tips
- If you see Mockito dynamic agent warnings on newer JDKs, they are currently harmless. If you need to silence them locally: set JAVA_TOOL_OPTIONS="-XX:+EnableDynamicAgentLoading" or migrate to using the Mockito Java agent in your build startup (see Mockito docs).
- For JSON serialization tests, ObjectMapper is autowired by Spring in @WebMvcTest; prefer objectMapper.writeValueAsString(...) for request bodies.
- For pagination in controllers, tests stub Page<T> with PageImpl to simulate repository results.
- When adding endpoints, ensure you register any @ControllerAdvice or handler used broadly in your @WebMvcTest to keep error mapping consistent.
