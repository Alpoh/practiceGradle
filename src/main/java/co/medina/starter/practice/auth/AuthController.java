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
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final co.medina.starter.practice.security.JwtUtil jwtUtil;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Either<ApiError, Void> register(@Valid @RequestBody RegisterRequest request) {
        return Try.run(() -> {
                    if (userRepository.existsByEmail(request.email())) {
                        throw new DataIntegrityViolationException("Email already exists");
                    }
                    User user = User.builder()
                            .email(request.email())
                            .name(request.name())
                            .mobileNumber(request.mobileNumber())
                            .address(request.address())
                            .password(passwordEncoder.encode(request.password()))
                            .build();
                    userRepository.save(user);
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
                    UserDetails userDetails = (UserDetails) auth.getPrincipal();
                    String token = jwtUtil.generateToken(userDetails);
                    return new AuthResponse(token, "Bearer");
                })
                .toEither()
                .mapLeft(this::mapToApiError);
    }

    private ApiError mapToApiError(Throwable throwable) {
        if (throwable instanceof DataIntegrityViolationException) {
            return new ApiError(HttpStatus.CONFLICT, throwable.getMessage());
        }
        if (throwable instanceof AuthenticationException) {
            return new ApiError(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
}

