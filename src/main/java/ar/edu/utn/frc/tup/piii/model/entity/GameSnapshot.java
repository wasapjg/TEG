package ar.edu.utn.frc.tup.piii.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_snapshots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "turn_number", nullable = false)
    private Integer turnNumber;

    @Lob
    @Column(name = "serialized_state", nullable = false)
    private String serializedState;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by_system")
    private Boolean createdBySystem = true;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static GameSnapshot createFrom(Game game) {
        GameSnapshot snapshot = new GameSnapshot();
        snapshot.setGame(game);
        snapshot.setTurnNumber(game.getCurrentTurn());
        // TODO La serialización sería implementada según la estrategia elegida
        snapshot.setSerializedState("{}"); // Placeholder
        return snapshot;
    }
}
