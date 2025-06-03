package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.exceptions.UserNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.UserMapper;
import ar.edu.utn.frc.tup.piii.model.User;
import ar.edu.utn.frc.tup.piii.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity userEntity;
    private User user;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");
        userEntity.setEmail("test@email.com");
        userEntity.setPasswordHash("hashedPassword");
        userEntity.setCreatedAt(LocalDateTime.now());
        userEntity.setLastLogin(LocalDateTime.now());
        userEntity.setIsActive(true);

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@email.com")
                .passwordHash("hashedPassword")
                .lastLogin(LocalDateTime.now())
                .build();
    }

    @Test
    void getUserByUserName_WhenUserExists_ShouldReturnUser() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(userMapper.toModel(userEntity)).thenReturn(user);

        // When
        User result = userService.getUserByUserName(username);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getUserByUserName_WhenUserNotExists_ShouldThrowException() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByUserName(username))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with username: nonexistent");
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        // Given
        UserEntity userEntity2 = new UserEntity();
        userEntity2.setId(2L);
        userEntity2.setUsername("testuser2");
        userEntity2.setEmail("test2@email.com");

        User user2 = User.builder()
                .id(2L)
                .username("testuser2")
                .email("test2@email.com")
                .build();

        List<UserEntity> userEntities = Arrays.asList(userEntity, userEntity2);

        when(userRepository.findAll()).thenReturn(userEntities);
        when(userMapper.toModel(userEntity)).thenReturn(user);
        when(userMapper.toModel(userEntity2)).thenReturn(user2);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUsername()).isEqualTo("testuser");
        assertThat(result.get(1).getUsername()).isEqualTo("testuser2");
    }

    @Test
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toModel(userEntity)).thenReturn(user);

        // When
        User result = userService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldThrowException() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 999");
    }


}