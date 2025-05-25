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
    @NotNull(message = "Attacker country ID is required")
    private Long attackerCountryId;

    @NotNull(message = "Defender country ID is required")
    private Long defenderCountryId;

    @NotNull(message = "Player ID is required")
    private Long playerId;

    @Min(value = 1, message = "At least 1 army must attack")
    private Integer attackingArmies;

    @Min(value = 1, message = "Must use at least 1 die")
    private Integer attackerDice;

    @Min(value = 1, message = "Must use at least 1 die")
    private Integer defenderDice;
}