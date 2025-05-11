package ar.edu.utn.frc.tup.piii.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_events") public class GameEvent { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Column(nullable = false)
    private String event;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public GameEvent() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

}
