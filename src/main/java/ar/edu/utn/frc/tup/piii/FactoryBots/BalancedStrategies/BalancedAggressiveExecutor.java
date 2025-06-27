package ar.edu.utn.frc.tup.piii.FactoryBots.BalancedStrategies;

import ar.edu.utn.frc.tup.piii.FactoryBots.BotStrategyExecutor;
import ar.edu.utn.frc.tup.piii.dtos.game.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.game.CombatResultDto;
import ar.edu.utn.frc.tup.piii.dtos.game.FortifyDto;
import ar.edu.utn.frc.tup.piii.entities.CountryEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import ar.edu.utn.frc.tup.piii.service.interfaces.CombatService;
import ar.edu.utn.frc.tup.piii.service.interfaces.FortificationService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class BalancedAggressiveExecutor implements BotStrategyExecutor {

    @Autowired
    private CombatService combatService;

    @Autowired
    private FortificationService fortificationService;

    //falta el servicio de Refuerzos.

    @Autowired
    private GameTerritoryService gameTerritoryService;

    private final Random random = new Random();

    @Override
    public BotLevel getLevel() {
        return BotLevel.BALANCED;
    }

    @Override
    public BotStrategy getStrategy() {
        return BotStrategy.AGGRESSIVE;
    }

    @Override
    public void executeTurn(PlayerEntity botPlayer, GameEntity game) {
        log.info("Ejecutando turno para bot BALANCED-AGGRESSIVE: {}", botPlayer.getBotProfile().getBotName());

        // Orden correcto de las fases del turno
        performBotReinforcement(botPlayer, game);
        performBotAttack(botPlayer, game);
        performBotFortify(botPlayer, game);
    }

    @Override
    public void performBotReinforcement(PlayerEntity botPlayer, GameEntity game) {
        log.info("Bot BALANCED-AGGRESSIVE realizando refuerzos para jugador: {}", botPlayer.getId());

        try {
            // Estrategia BALANCED-AGGRESSIVE para refuerzos:
            // - Refuerza territorios fronterizos más débiles primero
            // - Distribuye refuerzos de manera más inteligente que NOVICE
            // - Considera tanto defensa como ataque

            List<Territory> playerTerritories = gameTerritoryService.getTerritoriesByOwner(
                    game.getId(), botPlayer.getId());

            if (playerTerritories.isEmpty()) {
                log.warn("Bot no tiene territorios para reforzar");
                return;
            }

            // Encontrar territorios fronterizos
            List<Territory> borderTerritories = playerTerritories.stream()
                    .filter(territory -> {
                        List<Territory> neighbors = gameTerritoryService.getNeighborTerritories(
                                game.getId(), territory.getId());
                        return neighbors.stream()
                                .anyMatch(neighbor -> !neighbor.getOwnerId().equals(botPlayer.getId()));
                    })
                    .toList();

            if (borderTerritories.isEmpty()) {
                log.info("Bot no tiene territorios fronterizos, reforzando territorios propios");
                // Si no hay frontera, reforzar el más débil
                Territory weakestTerritory = playerTerritories.stream()
                        .min((t1, t2) -> Integer.compare(t1.getArmies(), t2.getArmies()))
                        .orElse(playerTerritories.get(0));

                log.info("Bot reforzaría territorio más débil: {}", weakestTerritory.getName());
                return;
            }

            // Estrategia balanceada: reforzar territorios fronterizos más débiles
            // pero también considerar potencial ofensivo
            Territory priorityTerritory = borderTerritories.stream()
                    .min((t1, t2) -> {
                        // Priorizar por: menos ejércitos + más vecinos enemigos débiles
                        int t1Score = calculateTerritoryPriority(t1, game, botPlayer.getId());
                        int t2Score = calculateTerritoryPriority(t2, game, botPlayer.getId());
                        return Integer.compare(t2Score, t1Score); // Mayor score = mayor prioridad
                    })
                    .orElse(borderTerritories.get(0));

            //todo: Implementar refuerzos cuando esté el servicio
            // reinforcementService.reinforceTerritory(game.getGameCode(), botPlayer.getId(),
            //     priorityTerritory.getId(), availableReinforcements);

            log.info("Bot priorizaría reforzar territorio: {} (Ejércitos actuales: {})",
                    priorityTerritory.getName(), priorityTerritory.getArmies());

        } catch (Exception e) {
            log.error("Error en refuerzos del bot BALANCED-AGGRESSIVE: {}", e.getMessage());
        }
    }

    @Override
    public void performBotAttack(PlayerEntity botPlayer, GameEntity game) {
        log.info("Bot BALANCED-AGGRESSIVE realizando ataques para jugador: {}", botPlayer.getId());

        try {
            // Estrategia BALANCED-AGGRESSIVE para ataques:
            // - Evalúa probabilidades de éxito antes de atacar
            // - Ataca solo cuando tiene ventaja significativa
            // - Puede realizar más ataques que NOVICE pero de forma más calculada

            List<Territory> attackableTerritories = combatService.getAttackableTerritoriesForPlayer(
                    game.getGameCode(), botPlayer.getId());

            if (attackableTerritories.isEmpty()) {
                log.info("Bot no tiene territorios desde donde atacar");
                return;
            }

            // Realizar ataques calculados
            int maxAttacks = 5; // Más agresivo que NOVICE pero no ilimitado
            int attackCount = 0;

            for (Territory attackerTerritory : attackableTerritories) {
                if (attackCount >= maxAttacks) break;

                List<Territory> targets = combatService.getTargetsForTerritory(
                        game.getGameCode(), attackerTerritory.getId(), botPlayer.getId());

                if (targets.isEmpty()) continue;

                // Encontrar el mejor objetivo evaluando probabilidades
                Territory bestTarget = null;
                double bestProbability = 0.0;

                for (Territory target : targets) {
                    double probability = evaluateAttackProbability(
                            botPlayer, attackerTerritory.getArmies(), target.getArmies());

                    if (probability > bestProbability && probability > 0.65) { // Solo atacar si prob > 65%
                        bestProbability = probability;
                        bestTarget = target;
                    }
                }

                if (bestTarget != null) {
                    // Calcular ejércitos para atacar de manera más inteligente
                    int attackingArmies = calculateOptimalAttackForce(
                            attackerTerritory.getArmies(), bestTarget.getArmies());

                    if (attackingArmies > 0) {
                        AttackDto attackDto = AttackDto.builder()
                                .playerId(botPlayer.getId())
                                .attackerCountryId(attackerTerritory.getId())
                                .defenderCountryId(bestTarget.getId())
                                .attackingArmies(attackingArmies)
                                .build();

                        CombatResultDto result = combatService.performCombat(game.getGameCode(), attackDto);

                        log.info("Bot atacó desde {} hacia {} con probabilidad {}: Conquistado={}",
                                attackerTerritory.getName(), bestTarget.getName(),
                                String.format("%.2f", bestProbability), result.getTerritoryConquered());

                        attackCount++;
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error en ataques del bot BALANCED-AGGRESSIVE: {}", e.getMessage());
        }
    }

    @Override
    public void performBotFortify(PlayerEntity botPlayer, GameEntity game) {
        log.info("Bot BALANCED-AGGRESSIVE realizando fortificación para jugador: {}", botPlayer.getId());

        try {
            // Estrategia BALANCED-AGGRESSIVE para fortificación:
            // - Identifica territorios vulnerables y los refuerza
            // - Balancea entre defensa y preparación para ataques futuros

            List<Territory> fortifiableTerritories = fortificationService.getFortifiableTerritoriesForPlayer(
                    game.getGameCode(), botPlayer.getId());

            if (fortifiableTerritories.isEmpty()) {
                log.info("Bot no tiene territorios desde donde fortificar");
                return;
            }

            // Estrategia más sofisticada: encontrar el mejor movimiento
            Territory bestSource = null;
            Territory bestTarget = null;
            int bestScore = 0;

            for (Territory source : fortifiableTerritories) {
                List<Territory> possibleTargets = fortificationService.getFortificationTargetsForTerritory(
                        game.getGameCode(), source.getId(), botPlayer.getId());

                for (Territory target : possibleTargets) {
                    int score = calculateFortificationScore(source, target, game, botPlayer.getId());
                    if (score > bestScore) {
                        bestScore = score;
                        bestSource = source;
                        bestTarget = target;
                    }
                }
            }

            if (bestSource != null && bestTarget != null) {
                int maxMovableArmies = fortificationService.getMaxMovableArmies(
                        game.getGameCode(), bestSource.getId());

                if (maxMovableArmies > 0) {
                    // Calcular cuántos ejércitos mover (más estratégico que NOVICE)
                    int armiesToMove = calculateOptimalFortificationAmount(
                            bestSource.getArmies(), bestTarget.getArmies(), maxMovableArmies);

                    FortifyDto fortifyDto = new FortifyDto();
                    fortifyDto.setPlayerId(botPlayer.getId());
                    fortifyDto.setFromCountryId(bestSource.getId());
                    fortifyDto.setToCountryId(bestTarget.getId());
                    fortifyDto.setArmies(armiesToMove);

                    boolean success = fortificationService.performFortification(
                            game.getGameCode(), fortifyDto);

                    if (success) {
                        log.info("Bot fortificó desde {} hacia {} con {} ejércitos (Score: {})",
                                bestSource.getName(), bestTarget.getName(), armiesToMove, bestScore);
                    } else {
                        log.warn("Falló la fortificación del bot");
                    }
                }
            } else {
                log.info("Bot no encontró movimientos de fortificación beneficiosos");
            }

        } catch (Exception e) {
            log.error("Error en fortificación del bot BALANCED-AGGRESSIVE: {}", e.getMessage());
        }
    }

    @Override
    public double evaluateAttackProbability(PlayerEntity botPlayer, int attackerArmies, int defenderArmies) {
        // Evaluación más sofisticada que NOVICE
        if (attackerArmies <= 1) return 0.0;

        // Fórmula mejorada considerando ventaja numérica
        double ratio = (double) attackerArmies / defenderArmies;

        // Probabilidad base según ratio
        double probability;
        if (ratio >= 3.0) {
            probability = 0.9; // Muy alta probabilidad si tengo 3x más ejércitos
        } else if (ratio >= 2.0) {
            probability = 0.75; // Alta probabilidad si tengo 2x más
        } else if (ratio >= 1.5) {
            probability = 0.6; // Moderada si tengo 1.5x más
        } else if (ratio >= 1.2) {
            probability = 0.4; // Baja si tengo solo 1.2x más
        } else {
            probability = 0.2; // Muy baja si tengo menos o similar
        }

        return probability;
    }

    @Override
    public List<CountryEntity> getBestAttackTargets(PlayerEntity botPlayer, GameEntity game) {
        // Implementación futura - por ahora retorna lista vacía
        return List.of();
    }

    @Override
    public List<CountryEntity> getBestDefensePositions(PlayerEntity botPlayer, GameEntity game) {
        // Implementación futura - por ahora retorna lista vacía
        return List.of();
    }

    // Métodos auxiliares para cálculos estratégicos

    private int calculateTerritoryPriority(Territory territory, GameEntity game, Long playerId) {
        // Calcula prioridad basada en vulnerabilidad y potencial ofensivo
        List<Territory> neighbors = gameTerritoryService.getNeighborTerritories(
                game.getId(), territory.getId());

        int enemyNeighbors = 0;
        int weakEnemyNeighbors = 0;

        for (Territory neighbor : neighbors) {
            if (!neighbor.getOwnerId().equals(playerId)) {
                enemyNeighbors++;
                if (neighbor.getArmies() < territory.getArmies()) {
                    weakEnemyNeighbors++;
                }
            }
        }

        // Score: más puntos por tener vecinos enemigos débiles, menos por ser muy débil
        int score = (weakEnemyNeighbors * 3) + (enemyNeighbors * 2) - (territory.getArmies() / 2);
        return score;
    }

    private int calculateOptimalAttackForce(int attackerArmies, int defenderArmies) {
        // Calcula la fuerza óptima para atacar
        int maxAttackForce = attackerArmies - 1; // Dejar al menos 1

        if (defenderArmies <= 2) {
            return Math.min(maxAttackForce, 2); // Ataque conservador contra débiles
        } else if (defenderArmies <= 4) {
            return Math.min(maxAttackForce, 3); // Ataque moderado
        } else {
            return Math.min(maxAttackForce, Math.max(3, defenderArmies - 1)); // Ataque fuerte
        }
    }

    private int calculateFortificationScore(Territory source, Territory target, GameEntity game, Long playerId) {
        // Calcula score para movimiento de fortificación
        List<Territory> targetNeighbors = gameTerritoryService.getNeighborTerritories(
                game.getId(), target.getId());
        List<Territory> sourceNeighbors = gameTerritoryService.getNeighborTerritories(
                game.getId(), source.getId());

        int targetEnemyNeighbors = (int) targetNeighbors.stream()
                .filter(n -> !n.getOwnerId().equals(playerId))
                .count();

        int sourceEnemyNeighbors = (int) sourceNeighbors.stream()
                .filter(n -> !n.getOwnerId().equals(playerId))
                .count();

        // Priorizar mover hacia territorios fronterizos desde territorios seguros
        int score = (targetEnemyNeighbors * 3) - (sourceEnemyNeighbors * 2);

        // Bonus si el objetivo es muy débil
        if (target.getArmies() <= 2) {
            score += 2;
        }

        return score;
    }

    private int calculateOptimalFortificationAmount(int sourceArmies, int targetArmies, int maxMovable) {
        // Calcula cantidad óptima a mover
        int idealTargetStrength = 4; // Fuerza ideal para territorio fronterizo
        int needed = Math.max(0, idealTargetStrength - targetArmies);

        // Mover lo necesario o hasta 2/3 de lo disponible
        return Math.min(needed, Math.min(maxMovable, (sourceArmies - 1) * 2 / 3));
    }
}