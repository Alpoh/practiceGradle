package co.medina.starter.practice.user.api;

import co.medina.starter.practice.user.api.dto.UserRequest;
import co.medina.starter.practice.user.api.dto.UserResponse;
import co.medina.starter.practice.user.service.UserService;
import io.vavr.control.Either;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/v1/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Either<ApiError, UserResponse> create(@Valid @RequestBody UserRequest request) {
        return userService.create(request)
            .map(userMapper::toResponse)
            .mapLeft(this::mapToApiError);
    }

    @GetMapping("/{id}")
    public Either<ApiError, UserResponse> get(@PathVariable Long id) {
        return userService.getById(id)
            .map(userMapper::toResponse)
            .mapLeft(this::mapToApiError);
    }

    @GetMapping
    public Either<ApiError, Page<UserResponse>> list(Pageable pageable) {
        return userService.getAll(pageable)
            .map(page -> page.map(userMapper::toResponse))
            .mapLeft(this::mapToApiError);
    }

    @PutMapping("/{id}")
    public Either<ApiError, UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return userService.update(id, request)
            .map(userMapper::toResponse)
            .mapLeft(this::mapToApiError);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Either<ApiError, Void> delete(@PathVariable Long id) {
        return userService.delete(id)
            .mapLeft(this::mapToApiError);
    }

    private ApiError mapToApiError(Throwable throwable) {
        if (throwable instanceof DataIntegrityViolationException) {
            return new ApiError(HttpStatus.CONFLICT, throwable.getMessage());
        }
        if (throwable instanceof NoSuchElementException) {
            return new ApiError(HttpStatus.NOT_FOUND, throwable.getMessage());
        }
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
}
