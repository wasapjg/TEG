package ar.edu.utn.frc.tup.piii.dtos.game;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameCreationDto {
    @NotNull(message = "Creator user ID is required")
    private Long creatorUserId;

    @Min(value = 2, message = "Minimum 2 players required")
    @Max(value = 6, message = "Maximum 6 players allowed")
    private Integer maxPlayers;

    @Min(value = 1, message = "Turn time limit must be at least 1 minute")
    private Integer turnTimeLimit; // en minutos

    private Boolean chatEnabled = true;
    private Boolean pactsAllowed = false;
    private String gameCode; // opcional, se genera automáticamente si no se proporciona
}
