package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.common.JwtResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserLoginDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserRegisterDto;

public interface AuthService {
    JwtResponseDto register(UserRegisterDto dto);
    JwtResponseDto login(UserLoginDto dto);
}


