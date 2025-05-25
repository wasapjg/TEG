package ar.edu.utn.frc.tup.piii.dtos.player;

import ar.edu.utn.frc.tup.piii.dtos.objective.ObjectiveResponseDto;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerResponseDto {
    private Long id;
    private String displayName;
    private Boolean isBot;
    private String botLevel; // solo si es bot
    private PlayerStatus status;
    private PlayerColor color;
    private Integer armiesToPlace;
    private Integer seatOrder;
    private LocalDateTime joinedAt;
    private Integer territoriesCount;
    private Integer totalArmies;
    private Integer cardsCount;
    private ObjectiveResponseDto objective;
}
