package ar.edu.utn.frc.tup.piii.model;

import ar.edu.utn.frc.tup.piii.model.enums.CardSymbol;
import jakarta.persistence.*;

@Entity
@Table(name = "cards") public class Card { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardSymbol symbol;

    public Card() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }
    public CardSymbol getSymbol() { return symbol; }
    public void setSymbol(CardSymbol symbol) { this.symbol = symbol; }

}


