package ar.edu.utn.frc.tup.piii.model;

import jakarta.persistence.*;

@Entity
@Table(name = "borders") public class Border { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @ManyToOne
    @JoinColumn(name = "adjacent_country_id", nullable = false)
    private Country adjacent;

    public Border() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }
    public Country getAdjacent() { return adjacent; }
    public void setAdjacent(Country adjacent) { this.adjacent = adjacent; }

}


