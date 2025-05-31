package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.common.JwtResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserLoginDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserRegisterDto;
import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.exception.EmailAlreadyExistsException;
import ar.edu.utn.frc.tup.piii.exception.InvalidCredentialsException;
import ar.edu.utn.frc.tup.piii.exception.UserAlreadyExistsException;
import ar.edu.utn.frc.tup.piii.exception.UserNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.UserMapper;
import ar.edu.utn.frc.tup.piii.model.User;
import ar.edu.utn.frc.tup.piii.repository.UserRepository;
import ar.edu.utn.frc.tup.piii.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private BCryptPasswordEncoder encoder;

    private AuthServiceImpl authService;

    private UserRegisterDto registerDto;
    private UserLoginDto loginDto;
    private UserEntity userEntity;
    private User user;

    @BeforeEach
    void setUp() {
        // Crear el servicio manualmente y usar reflection para inyectar el mock del encoder
        authService = new AuthServiceImpl(userRepository, jwtUtils);
        ReflectionTestUtils.setField(authService, "encoder", encoder);
        ReflectionTestUtils.setField(authService, "userMapper", userMapper);

        registerDto = new UserRegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setPassword("Password123!");
        registerDto.setEmail("test@email.com");
        registerDto.setAvatarUrl("http://example.com/avatar.jpg");

        loginDto = new UserLoginDto();
        loginDto.setUsername("testuser");
        loginDto.setPassword("Password123!");

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");
        userEntity.setEmail("test@email.com");
        userEntity.setPasswordHash("$2a$10$hashedPassword");
        userEntity.setCreatedAt(LocalDateTime.now());
        userEntity.setIsActive(true);

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@email.com")
                .passwordHash("$2a$10$hashedPassword")
                .build();
    }

    @Test
    void register_WithValidData_ShouldReturnJwtToken() {
        // Given
        when(userRepository.findByUsername(registerDto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(registerDto.getEmail())).thenReturn(Optional.empty());
        when(encoder.encode(registerDto.getPassword())).thenReturn("$2a$10$hashedPassword");
        when(userMapper.toEntity(any(User.class))).thenReturn(userEntity);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(jwtUtils.generateToken(registerDto.getUsername())).thenReturn("mock-jwt-token");

        // When
        JwtResponseDto result = authService.register(registerDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("mock-jwt-token");
        verify(encoder).encode(registerDto.getPassword());
        verify(userRepository).save(any(UserEntity.class));
        verify(jwtUtils).generateToken(registerDto.getUsername());
    }

    @Test
    void register_WithExistingUsername_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername(registerDto.getUsername())).thenReturn(Optional.of(userEntity));

        // When & Then
        assertThatThrownBy(() -> authService.register(registerDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("Username already exists: testuser");

        verify(encoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(jwtUtils, never()).generateToken(anyString());
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername(registerDto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(registerDto.getEmail())).thenReturn(Optional.of(userEntity));

        // When & Then
        assertThatThrownBy(() -> authService.register(registerDto))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already registered: test@email.com");

        verify(encoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(jwtUtils, never()).generateToken(anyString());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnJwtToken() {
        // Given
        when(userRepository.findByUsername(loginDto.getUsername())).thenReturn(Optional.of(userEntity));
        when(userMapper.toModel(userEntity)).thenReturn(user);
        when(encoder.matches(loginDto.getPassword(), user.getPasswordHash())).thenReturn(true);
        when(jwtUtils.generateToken(user.getUsername())).thenReturn("mock-jwt-token");

        // When
        JwtResponseDto result = authService.login(loginDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("mock-jwt-token");
        verify(encoder).matches(loginDto.getPassword(), user.getPasswordHash());
        verify(jwtUtils).generateToken(user.getUsername());
    }

    @Test
    void login_WithNonExistentUser_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername(loginDto.getUsername())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(loginDto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");

        verify(encoder, never()).matches(anyString(), anyString());
        verify(jwtUtils, never()).generateToken(anyString());
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername(loginDto.getUsername())).thenReturn(Optional.of(userEntity));
        when(userMapper.toModel(userEntity)).thenReturn(user);
        when(encoder.matches(loginDto.getPassword(), user.getPasswordHash())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginDto))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid Password");

        verify(encoder).matches(loginDto.getPassword(), user.getPasswordHash());
        verify(jwtUtils, never()).generateToken(anyString());
    }

    @Test
    void register_ShouldSetUserFieldsCorrectly() {
        // Given
        when(userRepository.findByUsername(registerDto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(registerDto.getEmail())).thenReturn(Optional.empty());
        when(encoder.encode(registerDto.getPassword())).thenReturn("$2a$10$hashedPassword");
        when(userMapper.toEntity(any(User.class))).thenReturn(userEntity);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(jwtUtils.generateToken(registerDto.getUsername())).thenReturn("mock-jwt-token");

        // When
        authService.register(registerDto);

        // Then
        verify(userMapper).toEntity(argThat(user ->
                user.getUsername().equals(registerDto.getUsername()) &&
                        user.getEmail().equals(registerDto.getEmail()) &&
                        user.getAvatarUrl().equals(registerDto.getAvatarUrl()) &&
                        user.getLastLogin() == null
        ));
    }

    @Test
    void register_WithNullAvatarUrl_ShouldNotThrowException() {
        // Given
        registerDto.setAvatarUrl(null);
        when(userRepository.findByUsername(registerDto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(registerDto.getEmail())).thenReturn(Optional.empty());
        when(encoder.encode(registerDto.getPassword())).thenReturn("$2a$10$hashedPassword");
        when(userMapper.toEntity(any(User.class))).thenReturn(userEntity);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(jwtUtils.generateToken(registerDto.getUsername())).thenReturn("mock-jwt-token");

        // When
        JwtResponseDto result = authService.register(registerDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("mock-jwt-token");
    }
}