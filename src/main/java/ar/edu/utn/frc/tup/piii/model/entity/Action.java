package ar.edu.utn.frc.tup.piii.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "actions")
public class Action { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String payload;

    public Action() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

}

