package ar.edu.utn.frc.tup.piii.service;

import ar.edu.utn.frc.tup.piii.dtos.common.JwtResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.common.UserLoginDto;
import ar.edu.utn.frc.tup.piii.dtos.common.UserRegisterDto;

public interface AuthService {
    JwtResponseDto register(UserRegisterDto dto);
    JwtResponseDto login(UserLoginDto dto);
}


