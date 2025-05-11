package ar.edu.utn.frc.tup.piii.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity @Table(name = "games") public class Game { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Game() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}
