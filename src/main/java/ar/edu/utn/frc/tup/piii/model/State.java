package ar.edu.utn.frc.tup.piii.model;

import jakarta.persistence.*;

@Entity
@Table(name = "states") public class State { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    public State() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

}

