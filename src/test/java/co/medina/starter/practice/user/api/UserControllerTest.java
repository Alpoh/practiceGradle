package co.medina.starter.practice.user.api;

import co.medina.starter.practice.security.JwtUtil;
import co.medina.starter.practice.user.api.dto.UserRequest;
import co.medina.starter.practice.user.api.dto.UserResponse;
import co.medina.starter.practice.user.domain.User;
import co.medina.starter.practice.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {UserController.class, EitherResponseHandler.class})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;


    @Test
    @DisplayName("POST /api/users - 201 Created happy path")
    void create_shouldReturn201() throws Exception {
        var req = new UserRequest("john.doe@example.com", "123", "John Doe", "Addr");
        var user = User.builder().id(1L).build();
        var resp = UserResponse.builder().id(1L).build();

        given(userService.create(any(UserRequest.class))).willReturn(Either.right(user));
        given(userMapper.toResponse(user)).willReturn(resp);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/users - 409 Conflict on duplicate email")
    void create_shouldReturn409_onDuplicateEmail() throws Exception {
        var req = new UserRequest("dup@example.com", null, "Dup", null);
        var error = new DataIntegrityViolationException("Email already exists");
        given(userService.create(any(UserRequest.class))).willReturn(Either.left(error));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    @DisplayName("GET /api/users/{id} - 200 OK when found")
    void get_shouldReturn200_whenFound() throws Exception {
        var user = User.builder().id(2L).build();
        var resp = UserResponse.builder().id(2L).build();
        given(userService.getById(2L)).willReturn(Either.right(user));
        given(userMapper.toResponse(user)).willReturn(resp);

        mockMvc.perform(get("/api/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @DisplayName("GET /api/users/{id} - 404 Not Found when missing")
    void get_shouldReturn404_whenMissing() throws Exception {
        var error = new NoSuchElementException("User not found");
        given(userService.getById(404L)).willReturn(Either.left(error));

        mockMvc.perform(get("/api/users/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("GET /api/users - 200 OK with user list")
    void list_shouldReturn200() throws Exception {
        var user = User.builder().id(1L).build();
        Page<User> userPage = new PageImpl<>(Collections.singletonList(user));

        given(userService.getAll(any(Pageable.class))).willReturn(Either.right(userPage));
        given(userMapper.toResponse(user)).willReturn(UserResponse.builder().id(1L).build());


        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - 200 OK on update")
    void update_shouldReturn200() throws Exception {
        var req = new UserRequest("x@y.com", null, "X", null);
        var updated = User.builder().id(5L).build();
        var resp = UserResponse.builder().id(5L).build();

        given(userService.update(eq(5L), any(UserRequest.class))).willReturn(Either.right(updated));
        given(userMapper.toResponse(updated)).willReturn(resp);

        mockMvc.perform(put("/api/users/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - 404 Not Found on update")
    void update_shouldReturn404_whenNotFound() throws Exception {
        var req = new UserRequest("x@y.com", null, "X", null);
        var error = new NoSuchElementException("User not found");
        given(userService.update(eq(9L), any(UserRequest.class))).willReturn(Either.left(error));

        mockMvc.perform(put("/api/users/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - 204 No Content on success")
    void delete_shouldReturn204() throws Exception {
        given(userService.delete(7L)).willReturn(Either.right(null));

        mockMvc.perform(delete("/api/users/7"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - 404 Not Found on delete")
    void delete_shouldReturn404_whenNotFound() throws Exception {
        var error = new NoSuchElementException("User not found");
        given(userService.delete(77L)).willReturn(Either.left(error));

        mockMvc.perform(delete("/api/users/77"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @DisplayName("GET /api/users - 500 Internal Server Error on unexpected exception")
    void get_shouldReturn500_onUnexpectedException() throws Exception {
        var error = new RuntimeException("boom");
        given(userService.getById(1L)).willReturn(Either.left(error));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
