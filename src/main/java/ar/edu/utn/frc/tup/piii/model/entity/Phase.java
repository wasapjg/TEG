package ar.edu.utn.frc.tup.piii.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "phases") public class Phase { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    public Phase() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

}

