package co.medina.starter.practice.user.api;

import co.medina.starter.practice.user.api.dto.UserRequest;
import co.medina.starter.practice.user.api.dto.UserResponse;
import co.medina.starter.practice.user.service.UserService;
import io.vavr.control.Either;
import io.vavr.control.Option;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public Either<String, UserResponse> create(@Valid @RequestBody UserRequest request) {
        var created = userService.create(request);
        return Either.right(userMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    public Either<String, UserResponse> get(@PathVariable Long id) {
        return Option.ofOptional(userService.getById(id))
                .map(u -> Either.<String, UserResponse>right(userMapper.toResponse(u)))
                .getOrElse(Either.left("User not found"));
    }

    @GetMapping
    public Page<UserResponse> list(Pageable pageable) {
        return userService.getAll(pageable).map(userMapper::toResponse);
    }

    @PutMapping("/{id}")
    public Either<String, UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        try {
            var updated = userService.update(id, request);
            return Either.right(userMapper.toResponse(updated));
        } catch (NoSuchElementException e) {
            return Either.left("User not found");
        }
    }

    @DeleteMapping("/{id}")
    public Either<String, String> delete(@PathVariable Long id) {
        try {
            userService.delete(id);
            return Either.right("deleted");
        } catch (NoSuchElementException e) {
            return Either.left("User not found");
        }
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflict(DataIntegrityViolationException ex) {
        return ex.getMessage();
    }
}
