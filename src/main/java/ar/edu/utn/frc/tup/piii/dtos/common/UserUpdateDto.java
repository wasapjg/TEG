package ar.edu.utn.frc.tup.piii.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserUpdateDto {
    private String username;
    private String password;
    private String avatarUrl;
}
