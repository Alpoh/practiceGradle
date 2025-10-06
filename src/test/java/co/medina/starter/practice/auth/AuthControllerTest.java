package co.medina.starter.practice.auth;

import co.medina.starter.practice.user.api.EitherResponseHandler;
import co.medina.starter.practice.user.domain.User;
import co.medina.starter.practice.user.repo.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, EitherResponseHandler.class})
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private co.medina.starter.practice.security.JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("POST /auth/register - 201 Created on success")
    void register_shouldReturn201_onSuccess() throws Exception {
        var request = RegisterRequest.builder()
                .email("new@example.com")
                .password("secret")
                .name("New User")
                .mobileNumber("123")
                .address("Addr")
                .build();

        given(userRepository.existsByEmail("new@example.com")).willReturn(false);
        given(passwordEncoder.encode("secret")).willReturn("hashed");
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /auth/register - 409 Conflict when email exists")
    void register_shouldReturn409_whenEmailExists() throws Exception {
        var request = RegisterRequest.builder()
                .email("dup@example.com")
                .password("x")
                .name("Dup")
                .build();

        given(userRepository.existsByEmail("dup@example.com")).willReturn(true);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    @Test
    @DisplayName("POST /auth/register - 500 Internal Server Error on unexpected")
    void register_shouldReturn500_onUnexpectedError() throws Exception {
        var request = RegisterRequest.builder()
                .email("new2@example.com")
                .password("secret")
                .name("New2")
                .build();

        given(userRepository.existsByEmail("new2@example.com")).willReturn(false);
        given(passwordEncoder.encode("secret")).willReturn("hashed");
        given(userRepository.save(any(User.class))).willThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    @DisplayName("POST /auth/login - 200 OK with token")
    void login_shouldReturn200_withToken() throws Exception {
        var request = LoginRequest.builder().email("a@b.com").password("p").build();

        Authentication auth = Mockito.mock(Authentication.class);
        UserDetails userDetails = Mockito.mock(UserDetails.class);

        given(authenticationManager.authenticate(any())).willReturn(auth);
        given(auth.getPrincipal()).willReturn(userDetails);
        given(jwtUtil.generateToken(userDetails)).willReturn("abc123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("abc123"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /auth/login - 401 Unauthorized on bad credentials")
    void login_shouldReturn401_onBadCredentials() throws Exception {
        var request = LoginRequest.builder().email("a@b.com").password("wrong").build();

        given(authenticationManager.authenticate(any())).willThrow(new BadCredentialsException("bad"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @DisplayName("POST /auth/login - 500 Internal Server Error on unexpected")
    void login_shouldReturn500_onUnexpectedError() throws Exception {
        var request = LoginRequest.builder().email("a@b.com").password("p").build();

        given(authenticationManager.authenticate(any())).willThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
