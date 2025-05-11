package ar.edu.utn.frc.tup.piii.model;

import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import jakarta.persistence.*;

@Entity
@Table(name = "bot_profiles") public class BotProfile { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BotLevel level;

    public BotProfile() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BotLevel getLevel() { return level; }
    public void setLevel(BotLevel level) { this.level = level; }

}


