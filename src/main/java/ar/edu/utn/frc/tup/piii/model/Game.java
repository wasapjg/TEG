package ar.edu.utn.frc.tup.piii.model;

import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {
    private Long id;
    private String gameCode;
    private Long createdByUserId;
    private String createdByUsername;
    private GameState state;
    private TurnPhase currentPhase;
    private Integer currentTurn;
    private Integer currentPlayerIndex;
    private Integer maxPlayers;
    private Integer turnTimeLimit;
    private Boolean chatEnabled;
    private Boolean pactsAllowed;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime lastModified;

    @Builder.Default
    private List<Player> players = new ArrayList<>();

    @Builder.Default
    private List<Territory> territories = new ArrayList<>();

    @Builder.Default
    private List<Card> deck = new ArrayList<>();

    @Builder.Default
    private List<GameEvent> events = new ArrayList<>();

    @Builder.Default
    private List<ChatMessage> chatMessages = new ArrayList<>();


    public Player getCurrentPlayer() {
        if (players == null || players.isEmpty() || currentPlayerIndex == null) {
            return null;
        }

        // Buscar el jugador que tiene seatOrder = currentPlayerIndex
        return players.stream()
                .filter(p -> p.getSeatOrder() != null &&
                        p.getSeatOrder().equals(currentPlayerIndex) &&
                        p.getStatus() != PlayerStatus.ELIMINATED)
                .findFirst()
                .orElse(null);
    }

    public boolean canStart() {
        return players.size() >= 2 && state == GameState.WAITING_FOR_PLAYERS;
    }

    public long getActivePlayerCount() {
        return players.stream()
                .filter(p -> p.getStatus() != PlayerStatus.ELIMINATED)
                .count();
    }

}