package co.medina.starter.practice.user.api;

import co.medina.starter.practice.user.api.dto.UserRequest;
import co.medina.starter.practice.user.api.dto.UserResponse;
import co.medina.starter.practice.user.domain.User;
import co.medina.starter.practice.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import co.medina.starter.practice.security.JwtUtil;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(UserControllerTest.TestConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @TestConfiguration(proxyBeanMethods = false)
    static class TestConfig {
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        public UserMapper userMapper() {
            return Mockito.mock(UserMapper.class);
        }

        @Bean
        public JwtUtil jwtUtil() {
            return Mockito.mock(JwtUtil.class);
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return Mockito.mock(UserDetailsService.class);
        }
    }

    @Test
    @DisplayName("POST /api/users - 201 Created happy path")
    void create_shouldReturn201() throws Exception {
        var req = new UserRequest("john.doe@example.com", "123", "John Doe", "Addr");
        var user = User.builder().id(1L).email(req.email()).name(req.name()).mobileNumber(req.mobileNumber()).address(req.address()).build();
        var resp = UserResponse.builder().id(1L).email(user.getEmail()).name(user.getName()).mobileNumber(user.getMobileNumber()).address(user.getAddress()).build();

        given(userService.create(any(UserRequest.class))).willReturn(user);
        given(userMapper.toResponse(user)).willReturn(resp);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/users - 400 Bad Request when validation fails")
    void create_shouldReturn400_onValidationError() throws Exception {
        // missing email and name -> invalid
        var invalidJson = "{\n  \"email\": \"\",\n  \"name\": \"\"\n}";

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/users/{id} - 200 OK when found")
    void get_shouldReturn200_whenFound() throws Exception {
        var user = User.builder().id(2L).email("a@b.com").name("A").build();
        var resp = UserResponse.builder().id(2L).email("a@b.com").name("A").build();
        given(userService.getById(2L)).willReturn(Optional.of(user));
        given(userMapper.toResponse(user)).willReturn(resp);

        mockMvc.perform(get("/api/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @DisplayName("GET /api/users/{id} - 404 Not Found when missing")
    void get_shouldReturn404_whenMissing() throws Exception {
        given(userService.getById(404L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/users/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/users/{id} - 200 OK on update")
    void update_shouldReturn200() throws Exception {
        var req = new UserRequest("x@y.com", null, "X", null);
        var updated = User.builder().id(5L).email(req.email()).name(req.name()).build();
        var resp = UserResponse.builder().id(5L).email(req.email()).name(req.name()).build();

        given(userService.update(eq(5L), any(UserRequest.class))).willReturn(updated);
        given(userMapper.toResponse(updated)).willReturn(resp);

        mockMvc.perform(put("/api/users/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - 404 Not Found when service throws NoSuchElementException")
    void update_shouldReturn404_whenNotFound() throws Exception {
        var req = new UserRequest("x@y.com", null, "X", null);
        given(userService.update(eq(9L), any(UserRequest.class))).willThrow(new NoSuchElementException("User not found"));

        mockMvc.perform(put("/api/users/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - 204 No Content on success")
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/users/7"))
                .andExpect(status().isNoContent());
        Mockito.verify(userService).delete(7L);
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - 404 Not Found when service throws NoSuchElementException")
    void delete_shouldReturn404_whenNotFound() throws Exception {
        Mockito.doThrow(new NoSuchElementException("User not found")).when(userService).delete(77L);

        mockMvc.perform(delete("/api/users/77"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/users - 409 Conflict on duplicate email")
    void create_shouldReturn409_onDuplicateEmail() throws Exception {
        var req = new UserRequest("dup@example.com", null, "Dup", null);
        given(userService.create(any(UserRequest.class))).willThrow(new DataIntegrityViolationException("Email already exists"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Email already exists")));
    }

    @Test
    @DisplayName("GET /api/users/{id} - 500 Internal Server Error on unexpected exception")
    void get_shouldReturn500_onUnexpectedException() throws Exception {
        given(userService.getById(1L)).willAnswer(inv -> { throw new RuntimeException("boom"); });

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Internal server error")));
    }
}
