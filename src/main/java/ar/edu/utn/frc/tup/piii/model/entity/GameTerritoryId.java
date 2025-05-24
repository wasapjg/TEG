package ar.edu.utn.frc.tup.piii.model.entity;

import java.util.Objects;

public class GameTerritoryId implements java.io.Serializable {
    private Long game;
    private Long country;

    public GameTerritoryId() {}

    public GameTerritoryId(Long game, Long country) {
        this.game = game;
        this.country = country;
    }

    // hashCode and equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameTerritoryId that = (GameTerritoryId) o;
        return Objects.equals(game, that.game) && Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(game, country);
    }

}
