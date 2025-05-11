package ar.edu.utn.frc.tup.piii.model;

import jakarta.persistence.*;

@Entity
@Table(name = "turn_timers") public class TurnTimer { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Column(name = "turn_duration", nullable = false)
    private int turnDurationSeconds;

    public TurnTimer() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getTurnDurationSeconds() { return turnDurationSeconds; }
    public void setTurnDurationSeconds(int seconds) { this.turnDurationSeconds = seconds; }

}

