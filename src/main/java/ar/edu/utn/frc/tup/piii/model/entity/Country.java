package ar.edu.utn.frc.tup.piii.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "countries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "continent_id", nullable = false)
    private Continent continent;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "country_neighbors",
            joinColumns = @JoinColumn(name = "country_id"),
            inverseJoinColumns = @JoinColumn(name = "neighbor_id")
    )
    private Set<Country> neighbors = new HashSet<>();

    @Column(name = "position_x")
    private Double positionX;

    @Column(name = "position_y")
    private Double positionY;

    public boolean isNeighbor(Country other) {
        return neighbors.contains(other);
    }

    public boolean canAttackFrom(Country from) {
        return this.neighbors.contains(from);
    }
}
