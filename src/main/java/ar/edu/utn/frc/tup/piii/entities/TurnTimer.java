//package ar.edu.utn.frc.tup.piii.entities;
//
//import jakarta.persistence.*;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import lombok.AllArgsConstructor;
//import java.time.LocalDateTime;

// TODO: Implementar cuando se necesiten configuraciones

//@Entity
//@Table(name = "turn_timers")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class TurnTimer {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "game_id", nullable = false)
//    private Game game;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "player_id", nullable = false)
//    private Player player;
//
//    @Column(name = "start_time")
//    private LocalDateTime startTime;
//
//    @Column(name = "end_time")
//    private LocalDateTime endTime;
//
//    @Column(name = "timed_out")
//    private Boolean timedOut = false;
//
//    @Column(name = "is_active")
//    private Boolean isActive = false;
//
//    public void start() {
//        this.startTime = LocalDateTime.now();
//        this.isActive = true;
//        this.timedOut = false;
//    }
//
//    public void cancel() {
//        this.isActive = false;
//        this.endTime = LocalDateTime.now();
//    }
//
//    public boolean hasTimedOut() {
//        if (startTime == null || !isActive) {
//            return false;
//        }
//
//        Integer timeLimit = game.getTurnTimeLimit();
//        if (timeLimit == null) {
//            return false;
//        }
//
//        return LocalDateTime.now().isAfter(startTime.plusMinutes(timeLimit));
//    }
//}