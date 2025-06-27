//package ar.edu.utn.frc.tup.piii.entities;
//
//import jakarta.persistence.*;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.AllArgsConstructor;
//import java.time.LocalDateTime;

// TODO: Implementar cuando se necesiten Fair Play

//@Entity
//@Table(name = "votes")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class Vote {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "report_id", nullable = false)
//    private RuleViolationReport report;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "voter_id", nullable = false)
//    private Player voter;
//
//    @Column(nullable = false)
//    private Boolean approve;
//
//    @Column(name = "voted_at", nullable = false)
//    private LocalDateTime votedAt;
//
//    @PrePersist
//    protected void onCreate() {
//        if (votedAt == null) {
//            votedAt = LocalDateTime.now();
//        }
//    }
//}