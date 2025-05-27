package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.common.UserLoginDto;
import ar.edu.utn.frc.tup.piii.dtos.common.UserRegisterDto;
import ar.edu.utn.frc.tup.piii.dtos.common.UserUpdateDto;
import ar.edu.utn.frc.tup.piii.model.entity.User;
import ar.edu.utn.frc.tup.piii.repository.UserRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;

    public UserServiceImpl(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public User save(User user) {
        return null;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepo.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        return List.of();
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public boolean existsById(Long id) {
        return false;
    }

    @Override
    public boolean existsByUsername(String username) {
        return false;
    }

    @Override
    public boolean existsByEmail(String email) {
        return false;
    }

    @Override
    public User register(UserRegisterDto registrationDto) {
        return null;
    }

    @Override
    public boolean authenticate(String username, String password) {
        return false;
    }

    @Override
    public User login(UserLoginDto loginDto) {
        return null;
    }

    @Override
    public User updateProfile(UserUpdateDto updateDto) {
        return null;
    }

    @Override
    public void updateEmail(Long userId, String newEmail) {

    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {

    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {

    }

    @Override
    public int getTotalGamesPlayed(Long userId) {
        return 0;
    }

    @Override
    public int getWins(Long userId) {
        return 0;
    }

    @Override
    public double getWinRate(Long userId) {
        return 0;
    }

    // Implementa aquí otros métodos de UserService si los declaraste
}
