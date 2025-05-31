package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.user.UserRegisterDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserUpdateDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserStatsDto;
import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.repository.UserRepository;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserRepository {

    UserResponseDto registerUser(UserRegisterDto userRegisterDto);

    UserResponseDto updateUser(Long userId, UserUpdateDto userUpdateDto);

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<UserResponseDto> findActiveUsers();

    List<UserResponseDto> searchActiveUsersByUsername(String searchTerm);

    UserStatsDto getUserStats(Long userId);
}
