package co.medina.starter.practice.user.api;

import co.medina.starter.practice.user.api.dto.UserRequest;
import co.medina.starter.practice.user.api.dto.UserResponse;
import co.medina.starter.practice.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        var created = userService.create(request);
        return ResponseEntity.created(URI.create("/api/users/" + created.getId()))
                .body(userMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable Long id) {
        return userService.getById(id)
                .map(u -> ResponseEntity.ok(userMapper.toResponse(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public Page<UserResponse> list(Pageable pageable) {
        return userService.getAll(pageable).map(userMapper::toResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        try {
            var updated = userService.update(id, request);
            return ResponseEntity.ok(userMapper.toResponse(updated));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            userService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflict(DataIntegrityViolationException ex) {
        return ex.getMessage();
    }
}
