package ar.edu.utn.frc.tup.piii.model;

import jakarta.persistence.*;

@Entity
@Table(name = "player_hands") public class PlayerHand { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    public PlayerHand() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public Card getCard() { return card; }
    public void setCard(Card card) { this.card = card; }

}

