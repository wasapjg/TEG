package ar.edu.utn.frc.tup.piii.model;

import jakarta.persistence.*;

@Entity @Table(name = "players") public class Player { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerStatus status;

    public Player() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
    public PlayerStatus getStatus() { return status; }
    public void setStatus(PlayerStatus status) { this.status = status; }

}


