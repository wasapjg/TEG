package ar.edu.utn.frc.tup.piii.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "game_territories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameTerritory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player owner; // null si es neutral

    @Column(nullable = false)
    private Integer armies = 1;

    @PrePersist
    @PreUpdate
    protected void validateArmies() {
        if (armies != null && armies < 0) {
            armies = 0;
        }
    }
}

