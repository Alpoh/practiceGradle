package co.medina.starter.practice.user.service;

import co.medina.starter.practice.user.api.dto.UserRequest;
import co.medina.starter.practice.user.domain.User;
import co.medina.starter.practice.user.repo.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class UserServiceImplMoreTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private AutoCloseable mocks;

    @BeforeEach
    void setup() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void update_shouldUpdateFields_whenExistsAndNoConflict() {
        var existing = User.builder().id(10L).email("old@example.com").name("Old").mobileNumber("111").address("A").build();
        var req = new UserRequest("new@example.com", "222", "New Name", "B");

        given(userRepository.findById(10L)).willReturn(Optional.of(existing));
        given(userRepository.existsByEmail("new@example.com")).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        var result = userService.update(10L, req);

        assertThat(result.isRight()).isTrue();
        var updated = result.get();
        assertThat(updated.getEmail()).isEqualTo("new@example.com");
        assertThat(updated.getName()).isEqualTo("New Name");
        verify(userRepository).save(existing);
    }

    @Test
    void update_shouldReturnError_whenNotFound() {
        var req = new UserRequest("x@example.com", null, "X", null);
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        var result = userService.update(999L, req);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void update_shouldReturnConflict_whenEmailTakenByAnother() {
        var existing = User.builder().id(11L).email("a@example.com").name("A").build();
        var req = new UserRequest("taken@example.com", null, "A2", null);

        given(userRepository.findById(11L)).willReturn(Optional.of(existing));
        given(userRepository.existsByEmail("taken@example.com")).willReturn(true);

        var result = userService.update(11L, req);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    void delete_shouldDelete_whenExists() {
        given(userRepository.existsById(5L)).willReturn(true);

        var result = userService.delete(5L);

        assertThat(result.isRight()).isTrue();
        verify(userRepository).deleteById(5L);
    }

    @Test
    void delete_shouldReturnError_whenNotExists() {
        given(userRepository.existsById(eq(6L))).willReturn(false);

        var result = userService.delete(6L);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("User not found");

        verify(userRepository, never()).deleteById(any());
    }
}
