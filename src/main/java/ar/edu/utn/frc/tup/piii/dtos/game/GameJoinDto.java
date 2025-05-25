package ar.edu.utn.frc.tup.piii.dtos.game;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameJoinDto {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Game code is required")
    private String gameCode;
}
