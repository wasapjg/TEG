package ar.edu.utn.frc.tup.piii.dtos.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Identity;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDto {
    private Identity id;
    @NotBlank private String username;
    @NotBlank private String password;

    public @NotBlank String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank String username) {
        this.username = username;
    }

    public @NotBlank String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank String password) {
        this.password = password;
    }
}
