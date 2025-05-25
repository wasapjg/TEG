package ar.edu.utn.frc.tup.piii.model.entity;

import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "players")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // null if bot

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_profile_id")
    private BotProfile botProfile; // null if human

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "objective_id")
    private Objective objective;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerStatus status = PlayerStatus.WAITING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerColor color;

    @Column(name = "armies_to_place")
    private Integer armiesToPlace = 0;

    @Column(name = "seat_order", nullable = false)
    private Integer seatOrder;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "eliminated_at")
    private LocalDateTime eliminatedAt;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Card> hand = new ArrayList<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GameTerritory> territories = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }

    public boolean isEliminated() {
        return status == PlayerStatus.ELIMINATED;
    }

    public boolean isHuman() {
        return user != null && botProfile == null;
    }

    public boolean isBot() {
        return botProfile != null && user == null;
    }

    public String getDisplayName() {
        if (isHuman()) {
            return user.getUsername();
        } else {
            return botProfile.getBotName();
        }
    }
}
