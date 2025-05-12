package ar.edu.utn.frc.tup.piii.model;

import jakarta.persistence.*;

@Entity
@Table(name = "game_territories") @IdClass(GameTerritoryId.class)
public class GameTerritory { @Id @ManyToOne @JoinColumn(name = "game_id") private Game game;

    @Id
    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    private int armies;

    public GameTerritory() {}

    // Getters and Setters
    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }
    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public int getArmies() { return armies; }
    public void setArmies(int armies) { this.armies = armies; }

}


