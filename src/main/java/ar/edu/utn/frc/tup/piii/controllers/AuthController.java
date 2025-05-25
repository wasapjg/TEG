package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.common.JwtResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.common.UserLoginDto;
import ar.edu.utn.frc.tup.piii.dtos.common.UserRegisterDto;
import ar.edu.utn.frc.tup.piii.service.interfaces.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<JwtResponseDto> register(@Valid @RequestBody UserRegisterDto dto){
        return  ResponseEntity.ok(authService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@Valid @RequestBody UserLoginDto dto){
        return  ResponseEntity.ok(authService.login(dto));
    }
}
