package co.medina.starter.practice.auth;

import co.medina.starter.practice.user.api.ApiError;
import co.medina.starter.practice.user.domain.User;
import co.medina.starter.practice.user.repo.UserRepository;
import io.vavr.control.Either;
import io.vavr.control.Try;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final co.medina.starter.practice.security.JwtUtil jwtUtil;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private EmailService emailService;

    @org.springframework.beans.factory.annotation.Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @PostMapping("/check-email")
    public Either<ApiError, java.util.Map<String, Object>> checkEmail(@Valid @RequestBody LoginRequest request) {
        // Reuse LoginRequest to carry email for simplicity
        boolean exists = userRepository.existsByEmail(request.email());
        return Either.right(java.util.Map.of("exists", exists));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Either<ApiError, Void> register(@Valid @RequestBody RegisterRequest request) {
        return Try.run(() -> {
                    if (userRepository.existsByEmail(request.email())) {
                        throw new DataIntegrityViolationException("Email already exists");
                    }
                    String token = java.util.UUID.randomUUID().toString().replaceAll("-", "");
                    String expiresAt = java.time.Instant.now().plus(java.time.Duration.ofHours(24)).toString();
                    User user = User.builder()
                            .email(request.email())
                            .name(request.name())
                            .mobileNumber(request.mobileNumber())
                            .address(request.address())
                            .password(passwordEncoder.encode(request.password()))
                            .emailVerified(false)
                            .verificationToken(token)
                            .verificationExpiresAt(expiresAt)
                            .build();
                    userRepository.save(user);

                    String link = String.format("%s/v1/auth/confirm?token=%s", baseUrl, token);
                    if (emailService != null) {
                        emailService.sendVerificationEmail(request.email(), link);
                    }
                })
                .toEither()
                .mapLeft(this::mapToApiError)
                .map(__ -> null);
    }

    @PostMapping("/login")
    public Either<ApiError, AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Try.of(() -> {
                    Authentication auth = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(request.email(), request.password())
                    );
                    // Check email verified if available
                    java.util.Optional<co.medina.starter.practice.user.domain.User> opt = userRepository.findByEmail(request.email());
                    if (opt.isPresent() && !opt.get().isEmailVerified()) {
                        throw new org.springframework.security.authentication.BadCredentialsException("Email not verified");
                    }
                    UserDetails userDetails = (UserDetails) auth.getPrincipal();
                    String token = jwtUtil.generateToken(userDetails);
                    return new AuthResponse(token, "Bearer");
                })
                .toEither()
                .mapLeft(this::mapToApiError);
    }

    @org.springframework.web.bind.annotation.GetMapping("/confirm")
    public Either<ApiError, java.util.Map<String, Object>> confirm(@org.springframework.web.bind.annotation.RequestParam("token") String token) {
        return userRepository.findByVerificationToken(token)
                .<Either<ApiError, java.util.Map<String, Object>>>map(user -> {
                    try {
                        java.time.Instant now = java.time.Instant.now();
                        java.time.Instant exp = java.time.Instant.parse(user.getVerificationExpiresAt());
                        if (now.isAfter(exp)) {
                            return Either.left(new ApiError(HttpStatus.BAD_REQUEST, "Token expired"));
                        }
                        user.setEmailVerified(true);
                        user.setVerificationToken(null);
                        user.setVerificationExpiresAt(null);
                        userRepository.save(user);
                        return Either.right(java.util.Map.of("status", "confirmed"));
                    } catch (Exception e) {
                        return Either.left(new ApiError(HttpStatus.BAD_REQUEST, "Invalid token"));
                    }
                })
                .orElseGet(() -> Either.left(new ApiError(HttpStatus.BAD_REQUEST, "Invalid token")));
    }

    private ApiError mapToApiError(Throwable throwable) {
        if (throwable instanceof DataIntegrityViolationException) {
            return new ApiError(HttpStatus.CONFLICT, throwable.getMessage());
        }
        if (throwable instanceof AuthenticationException) {
            return new ApiError(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        if (throwable instanceof IllegalArgumentException) {
            return new ApiError(HttpStatus.BAD_REQUEST, throwable.getMessage());
        }
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
}

