package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.user.UserRegisterDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserUpdateDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserStatsDto;
import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.exception.InvalidCredentialsException;
import ar.edu.utn.frc.tup.piii.exception.UserNotFoundException;
import ar.edu.utn.frc.tup.piii.repository.UserRepository;
import ar.edu.utn.frc.tup.piii.repository.PlayerRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.passay.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto registerUser(UserRegisterDto userRegisterDto) {
        validateUserRegisterDto(userRegisterDto);
        UserEntity user = new UserEntity();
        user.setUsername(userRegisterDto.getUsername());
        user.setEmail(userRegisterDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(userRegisterDto.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);
        userRepository.save(user);
        return UserResponseDto.fromEntity(user);
    }

    @Override
    public UserResponseDto updateUser(Long userId, UserUpdateDto userUpdateDto) {
        UserEntity user = findUserById(userId);
        if (userUpdateDto.getUsername() != null) {
            user.setUsername(userUpdateDto.getUsername());
        }
        if (userUpdateDto.getEmail() != null) {
            user.setEmail(userUpdateDto.getEmail());
        }
        if (userUpdateDto.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(userUpdateDto.getPassword()));
        }
        userRepository.save(user);
        return UserResponseDto.fromEntity(user);
    }

    @Override
    public UserResponseDto getUserById(Long userId) {
        return UserResponseDto.fromEntity(findUserById(userId));
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        UserEntity user = findUserById(userId);
        userRepository.delete(user);
    }

    @Override
    public UserStatsDto getUserStats(Long userId) {
        UserEntity user = findUserById(userId);
        long totalGamesPlayed = playerRepository.countByUser(user);
        long totalWins = playerRepository.countWinsByUser(user);

        return new UserStatsDto(totalGamesPlayed, totalWins);
    }

    private void validateUserRegisterDto(UserRegisterDto dto) {
        if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
    }

}
