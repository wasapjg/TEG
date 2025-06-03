//package ar.edu.utn.frc.tup.piii.entities;
//
//import jakarta.persistence.*;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.AllArgsConstructor;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.ArrayList;

// TODO: Implementar cuando se necesiten estadísticas detalladas

//@Entity
//@Table(name = "rule_violation_reports")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class RuleViolationReport {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "reporter_id", nullable = false)
//    private Player reporter;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "reported_id", nullable = false)
//    private Player reported;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "game_id", nullable = false)
//    private Game game;
//
//    @Column(nullable = false, length = 1000)
//    private String reason;
//
//    @Column(name = "reported_at", nullable = false)
//    private LocalDateTime reportedAt;
//
//    @Column(name = "resolved_at")
//    private LocalDateTime resolvedAt;
//
//    @Column(name = "is_resolved")
//    private Boolean isResolved = false;
//
//    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<Vote> votes = new ArrayList<>();
//
//    @PrePersist
//    protected void onCreate() {
//        if (reportedAt == null) {
//            reportedAt = LocalDateTime.now();
//        }
//    }
//}