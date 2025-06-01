package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.common.ErrorApi;
import ar.edu.utn.frc.tup.piii.dtos.common.JwtResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserLoginDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserRegisterDto;
import ar.edu.utn.frc.tup.piii.exception.EmailAlreadyExistsException;
import ar.edu.utn.frc.tup.piii.exception.InvalidCredentialsException;
import ar.edu.utn.frc.tup.piii.exception.UserAlreadyExistsException;
import ar.edu.utn.frc.tup.piii.exception.UserNotFoundException;
import ar.edu.utn.frc.tup.piii.service.interfaces.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRegisterDto registerDto;
    private UserLoginDto loginDto;
    private JwtResponseDto jwtResponse;

    @BeforeEach
    void setUp() {
        registerDto = new UserRegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setPassword("Password123!");
        registerDto.setEmail("test@email.com");
        registerDto.setAvatarUrl("http://example.com/avatar.jpg");

        loginDto = new UserLoginDto();
        loginDto.setUsername("testuser");
        loginDto.setPassword("Password123!");

        jwtResponse = new JwtResponseDto("mock-jwt-token");
    }

    @Test
    void register_WithValidData_ShouldReturnJwtToken() throws Exception {
        when(authService.register(any(UserRegisterDto.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    void register_WithInvalidDataUsername_ShouldReturnBadRequest() throws Exception {
        registerDto.setUsername(""); // Invalid username

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithInvalidDataPassword() throws Exception {
        registerDto.setUsername("test123");
        registerDto.setEmail("email@email.com");
        registerDto.setPassword("");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShortPassword() throws Exception {
        registerDto.setUsername("test123");
        registerDto.setEmail("email@email.com");
        registerDto.setPassword("1234");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithoutUpperCasePassword() throws Exception {
        registerDto.setUsername("test123");
        registerDto.setEmail("email@email.com");
        registerDto.setPassword("password1#");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithoutNumberPassword() throws Exception {
        registerDto.setUsername("test123");
        registerDto.setEmail("email@email.com");
        registerDto.setPassword("Password#");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithoutEspecialCharPassword() throws Exception {
        registerDto.setUsername("test123");
        registerDto.setEmail("email@email.com");
        registerDto.setPassword("Password1");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WhitExistingUser_ShouldReturnBadRequest() throws Exception {
        when(authService.register(any(UserRegisterDto.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    void register_WhitExistingEmail_ShouldReturnBadRequest() throws Exception {
        when(authService.register(any(UserRegisterDto.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    void register_WithGenericRuntimeException_ShouldReturnBadRequest() throws Exception {
        when(authService.register(any(UserRegisterDto.class)))
                .thenThrow(new RuntimeException("Generic error occurred"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Generic error occurred"));
    }

    @Test
    void login_WithInternalError() throws Exception {
        doThrow(new Error("System error")).when(authService).register(any(UserRegisterDto.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnJwtToken() throws Exception {
        when(authService.login(any(UserLoginDto.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception{
        when(authService.login(any(UserLoginDto.class))).thenThrow(new InvalidCredentialsException("Invalid Password"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithNonExistentUser_ShouldReturnNotFound() throws Exception {
        when(authService.login(any(UserLoginDto.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void login_WithEmptyUsername_ShouldReturnBadRequest() throws Exception {
        loginDto.setUsername("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithEmptyPassword_ShouldReturnBadRequest() throws Exception {
        loginDto.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
