package ar.edu.utn.frc.tup.piii.dtos.game;

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
public class AttackDto {
    @NotNull(message = "Player ID is required")
    private Long playerId;

    @NotNull(message = "Attacker country ID is required")
    private Long attackerCountryId;

    @NotNull(message = "Defender country ID is required")
    private Long defenderCountryId;

    @NotNull(message = "Attacking armies is required")
    @Min(value = 1, message = "Must attack with at least 1 army")
    private Integer attackingArmies;

    // Opcional: si quieres permitir al usuario elegir dados
    @Min(value = 1, message = "Must use at least 1 die")
    private Integer attackerDice;

    @Min(value = 1, message = "Must use at least 1 die")
    private Integer defenderDice;
}