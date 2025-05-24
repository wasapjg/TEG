package ar.edu.utn.frc.tup.piii.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "continents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Continent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(name = "bonus_armies", nullable = false)
    private Integer bonusArmies;

    @OneToMany(mappedBy = "continent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Country> countries = new HashSet<>();

    public boolean isControlledBy(Player player, Game game) {
        return countries.stream()
                .allMatch(country -> {
                    GameTerritory territory = game.getTerritories().get(country);
                    return territory != null && player.equals(territory.getOwner());
                });
    }
}