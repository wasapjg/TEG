package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.common.JwtResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.common.UserLoginDto;
import ar.edu.utn.frc.tup.piii.dtos.common.UserRegisterDto;
import ar.edu.utn.frc.tup.piii.exception.InvalidCredentialsException;
import ar.edu.utn.frc.tup.piii.exception.UserNotFoundException;
import ar.edu.utn.frc.tup.piii.model.entity.User;
import ar.edu.utn.frc.tup.piii.repository.UserRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.AuthService;
import ar.edu.utn.frc.tup.piii.utils.JwtUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(UserRepository repo, JwtUtils jwtUtils) {
        this.repo = repo;
        this.encoder = new BCryptPasswordEncoder();
        this.jwtUtils = jwtUtils;
    }

    @Override
    public JwtResponseDto register(UserRegisterDto dto) {
        if (repo.findUserByUsername(dto.getUsername()).isPresent()){
            throw  new RuntimeException("This user already exist");
        }
        User u = new User();
        u.setUsername(dto.getUsername());
        u.setPasswordHash(encoder.encode(dto.getPassword()));
        repo.save(u);
        String token = jwtUtils.generateToken(dto.getUsername());
        return new JwtResponseDto(token);
    }

    @Override
    public JwtResponseDto login(UserLoginDto dto) {
        User u = repo.findUserByUsername(dto.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (!encoder.matches(dto.getPassword(), u.getPasswordHash())){
            throw new InvalidCredentialsException("Invalid Password");
        }
        String token = jwtUtils.generateToken(u.getUsername());
        return new JwtResponseDto(token);
    }
}
