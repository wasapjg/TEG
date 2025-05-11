package ar.edu.utn.frc.tup.piii.model;

import jakarta.persistence.*;

@Entity
@Table(name = "countries") public class Country { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    public Country() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

}

