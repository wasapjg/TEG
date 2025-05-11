package ar.edu.utn.frc.tup.piii.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_snapshots") public class GameSnapshot { @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @Lob
    @Column(nullable = false)
    private String snapshotData;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public GameSnapshot() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSnapshotData() { return snapshotData; }
    public void setSnapshotData(String snapshotData) { this.snapshotData = snapshotData; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}

