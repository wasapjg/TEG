package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.common.UserLoginDto;
import ar.edu.utn.frc.tup.piii.dtos.common.UserRegisterDto;
import ar.edu.utn.frc.tup.piii.dtos.common.UserUpdateDto;
import ar.edu.utn.frc.tup.piii.model.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {

    // CRUD básico
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    void deleteById(Long id);
    boolean existsById(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Autenticación
    User register(UserRegisterDto registrationDto);
    boolean authenticate(String username, String password);
    User login(UserLoginDto loginDto);

    // Gestión de perfil
    User updateProfile(UserUpdateDto updateDto);
    void updateEmail(Long userId, String newEmail);
    void updateAvatar(Long userId, String avatarUrl);
    void changePassword(Long userId, String currentPassword, String newPassword);

    // Estadísticas
    int getTotalGamesPlayed(Long userId);
    int getWins(Long userId);
    double getWinRate(Long userId);
}
