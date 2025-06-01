//package ar.edu.utn.frc.tup.piii.entities;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDateTime;
//import java.util.List;
//

// TODO: Implementar cuando se necesiten estadísticas detalladas

//@Entity
//@Table(name = "combat_results")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class CombatResult {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    /** Partida a la que pertenece este combate */
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "game_id", nullable = false)
//    private Game game;
//
//    /** Ronda o fase dentro de la batalla */
//    @Column(name = "round_number", nullable = false)
//    private int roundNumber;
//
//    /** Momento en que se registró el combate */
//    @Column(name = "played_at", nullable = false)
//    private LocalDateTime timestamp;
//
//    /** País atacante (origen del ataque) */
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "attacker_origin_id", nullable = false)
//    private Country attackerOrigin;
//
//    /** País defensor (objetivo del ataque) */
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "defender_target_id", nullable = false)
//    private Country defenderTarget;
//
//    /** Unidades del atacante al inicio del combate */
//    @Column(name = "attacker_initial_units", nullable = false)
//    private int attackerInitialUnits;
//
//    /** Unidades del defensor al inicio del combate */
//    @Column(name = "defender_initial_units", nullable = false)
//    private int defenderInitialUnits;
//
//    /** Dados que sacó el atacante */
//    @ElementCollection
//    @CollectionTable(
//            name = "combat_attacker_dice",
//            joinColumns = @JoinColumn(name = "combat_result_id")
//    )
//    @Column(name = "die_value", nullable = false)
//    private List<Integer> attackerDice;
//
//    /** Dados que sacó el defensor */
//    @ElementCollection
//    @CollectionTable(
//            name = "combat_defender_dice",
//            joinColumns = @JoinColumn(name = "combat_result_id")
//    )
//    @Column(name = "die_value", nullable = false)
//    private List<Integer> defenderDice;
//
//    /** Pérdidas de unidades del atacante */
//    @Column(name = "attacker_losses", nullable = false)
//    private int attackerLosses;
//
//    /** Pérdidas de unidades del defensor */
//    @Column(name = "defender_losses", nullable = false)
//    private int defenderLosses;
//
//    /** ¿Se conquistó el territorio? */
//    @Column(name = "territory_conquered", nullable = false)
//    private boolean territoryConquered;
//}