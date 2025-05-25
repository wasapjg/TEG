package ar.edu.utn.frc.tup.piii.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ar.edu.utn.frc.tup.piii.model.enums.GameStatus;
import ar.edu.utn.frc.tup.piii.model.enums.GamePhase;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Entity
@Table(name = "games")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_code", unique = true, nullable = false)
    private String gameCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status = GameStatus.WAITING_FOR_PLAYERS;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_phase")
    private GamePhase currentPhase;

    @Column(name = "current_turn")
    private Integer currentTurn = 0;

    @Column(name = "current_player_index")
    private Integer currentPlayerIndex = 0;

    @Column(name = "max_players")
    private Integer maxPlayers = 6;

    @Column(name = "turn_time_limit")
    private Integer turnTimeLimit; // en minutos

    @Column(name = "chat_enabled")
    private Boolean chatEnabled = true;

    @Column(name = "pacts_allowed")
    private Boolean pactsAllowed = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Player> players = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GameTerritory> gameterritories = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Card> deck = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GameEvent> events = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GameSnapshot> snapshots = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastModified = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastModified = LocalDateTime.now();
    }

    public boolean hasSlot() {
        return players.size() < maxPlayers;
    }

    public boolean isOver() {
        return status == GameStatus.FINISHED || status == GameStatus.CANCELLED;
    }

    public Player getCurrentPlayer() {
        if (players.isEmpty() || currentPlayerIndex >= players.size()) {
            return null;
        }
        return players.get(currentPlayerIndex);
    }

    public Map<Country, GameTerritory> getTerritories() {
        Map<Country, GameTerritory> territoryMap = new HashMap<>();
        for (GameTerritory territory : gameterritories) {
            territoryMap.put(territory.getCountry(), territory);
        }
        return territoryMap;
    }
}