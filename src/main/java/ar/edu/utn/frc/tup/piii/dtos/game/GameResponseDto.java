package ar.edu.utn.frc.tup.piii.dtos.game;

//import ar.edu.utn.frc.tup.piii.dtos.player.PlayerResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.player.PlayerResponseDto;
import ar.edu.utn.frc.tup.piii.model.entity.Game;
import ar.edu.utn.frc.tup.piii.model.enums.GamePhase;
import ar.edu.utn.frc.tup.piii.model.enums.GameStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameResponseDto {
    private Long id;
    private String gameCode;
    private String createdByUsername;
    private GameStatus status;
    private GamePhase currentPhase;
    private Integer currentTurn;
    private Integer currentPlayerIndex;
    private Integer maxPlayers;
    private Integer turnTimeLimit;
    private Boolean chatEnabled;
    private Boolean pactsAllowed;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PlayerResponseDto> players;
    private String currentPlayerName;

    public static GameResponseDto fromEntity(Game game) {
        return GameResponseDto.builder()
                .id(game.getId())
                .gameCode(game.getGameCode())
                .createdByUsername(game.getCreatedBy().getUsername())
                .status(game.getStatus())
                .currentPhase(game.getCurrentPhase())
                .currentTurn(game.getCurrentTurn())
                .currentPlayerIndex(game.getCurrentPlayerIndex())
                .maxPlayers(game.getMaxPlayers())
                .turnTimeLimit(game.getTurnTimeLimit())
                .chatEnabled(game.getChatEnabled())
                .pactsAllowed(game.getPactsAllowed())
                .createdAt(game.getCreatedAt())
                .startedAt(game.getStartedAt())
                .finishedAt(game.getFinishedAt())
                .players(game.getPlayers().stream()
                        .map(PlayerResponseDto::fromEntity)
                        .collect(Collectors.toList()))
                .currentPlayerName(
                        game.getPlayers().size() > game.getCurrentPlayerIndex()
                                ? PlayerResponseDto.fromEntity(game.getPlayers().get(game.getCurrentPlayerIndex())).getUsername()
                                : null
                )
                .build();
    }

}
