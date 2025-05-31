package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.common.JwtResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserLoginDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserRegisterDto;
import ar.edu.utn.frc.tup.piii.exception.InvalidCredentialsException;
import ar.edu.utn.frc.tup.piii.exception.UserNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.UserMapper;
import ar.edu.utn.frc.tup.piii.model.User;
import ar.edu.utn.frc.tup.piii.repository.UserRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.AuthService;
import ar.edu.utn.frc.tup.piii.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    @Autowired
    private UserMapper userMapper;

    public AuthServiceImpl(UserRepository repo, JwtUtils jwtUtils) {
        this.repo = repo;
        this.encoder = new BCryptPasswordEncoder();
        this.jwtUtils = jwtUtils;
    }

    @Override
    public JwtResponseDto register(UserRegisterDto dto) {
        if (repo.findByUsername(dto.getUsername()).isPresent()){
            throw  new RuntimeException("This user already exist");
        }if (repo.findByEmail(dto.getEmail()).isPresent()){
            throw  new RuntimeException("This email is already registered");
        }
        User u = new User();
        u.setUsername(dto.getUsername());
        u.setPasswordHash(encoder.encode(dto.getPassword()));
        u.setEmail(dto.getEmail());
        u.setAvatarUrl(dto.getAvatarUrl());
        u.setLastLogin(null); // Set last login to null on registration
        repo.save(userMapper.toEntity(u));
        String token = jwtUtils.generateToken(dto.getUsername());
        return new JwtResponseDto(token);
    }

    @Override
    public JwtResponseDto login(UserLoginDto dto) {
        User u = repo.findByUsername(dto.getUsername())
                .map(userMapper::toModel)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (!encoder.matches(dto.getPassword(), u.getPasswordHash())){
            throw new InvalidCredentialsException("Invalid Password");
        }
        String token = jwtUtils.generateToken(u.getUsername());
        return new JwtResponseDto(token);
    }
}
