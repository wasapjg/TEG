package ar.edu.utn.frc.tup.piii.FactoryBots.ExpertStrategies;

import ar.edu.utn.frc.tup.piii.FactoryBots.BotStrategyExecutor;
import ar.edu.utn.frc.tup.piii.dtos.game.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.game.CombatResultDto;
import ar.edu.utn.frc.tup.piii.dtos.game.FortifyDto;
import ar.edu.utn.frc.tup.piii.entities.CountryEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.ObjectiveEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import ar.edu.utn.frc.tup.piii.model.enums.ObjectiveType;
import ar.edu.utn.frc.tup.piii.service.interfaces.CombatService;
import ar.edu.utn.frc.tup.piii.service.interfaces.FortificationService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ExpertAggressiveExecutor implements BotStrategyExecutor {

    @Autowired
    private CombatService combatService;

    @Autowired
    private FortificationService fortificationService;

    //falta el servicio de refuerzos, se debe implementar

    @Autowired
    private GameTerritoryService gameTerritoryService;

    private final Random random = new Random();

    // Estructura para representar el grafo de territorios
    private static class TerritoryNode {
        Long territoryId;
        String name;
        List<Long> neighbors;
        int armies;
        Long ownerId;
        int distance;
        Long previousNode;

        public TerritoryNode(Territory territory) {
            this.territoryId = territory.getId();
            this.name = territory.getName();
            this.armies = territory.getArmies();
            this.ownerId = territory.getOwnerId();
            this.neighbors = new ArrayList<>();
            this.distance = Integer.MAX_VALUE;
            this.previousNode = null;
        }
    }

    // Estructura para objetivos estratégicos
    private static class StrategicObjective {
        Long territoryId;
        String name;
        int priority;
        String type; // "CONTINENT_BONUS", "ENEMY_STRONGHOLD", "BRIDGE_TERRITORY", "MAIN_OBJECTIVE"
        List<Long> pathFromClosestOwned;
        int estimatedCost; // Ejércitos necesarios para conquistar
        boolean isMainObjective; // Indica si es parte del objetivo principal del jugador

        public StrategicObjective(Long territoryId, String name, int priority, String type) {
            this.territoryId = territoryId;
            this.name = name;
            this.priority = priority;
            this.type = type;
            this.pathFromClosestOwned = new ArrayList<>();
            this.estimatedCost = 0;
            this.isMainObjective = false;
        }
    }

    // Estructura para representar el objetivo principal del jugador
    private static class PlayerMainObjective {
        String objectiveType; // "OCCUPATION", "DESTRUCTION", "COMMON"
        String description;
        List<String> targetData; // Continentes a ocupar, jugador a destruir, etc.
        int priority;

        public PlayerMainObjective(ObjectiveEntity objective) {
            this.objectiveType = objective.getType().name();
            this.description = objective.getDescription();
            this.targetData = parseTargetData(objective.getTargetData());
            this.priority = 100; // Máxima prioridad para el objetivo principal
        }

        private List<String> parseTargetData(String targetData) {
            if (targetData == null || targetData.trim().isEmpty()) {
                return new ArrayList<>();
            }
            return Arrays.asList(targetData.split(","));
        }
    }

    @Override
    public BotLevel getLevel() {
        return BotLevel.EXPERT;
    }

    @Override
    public BotStrategy getStrategy() {
        return BotStrategy.AGGRESSIVE;
    }

    @Override
    public void executeTurn(PlayerEntity botPlayer, GameEntity game) {
        log.info("Ejecutando turno EXPERT-AGGRESSIVE para bot: {}", botPlayer.getBotProfile().getBotName());

        // Obtener el objetivo principal del jugador
        PlayerMainObjective mainObjective = getPlayerMainObjective(botPlayer);
        log.info("Objetivo principal del bot: {} - {}", mainObjective.objectiveType, mainObjective.description);

        // Análisis estratégico completo del tablero
        Map<Long, TerritoryNode> gameGraph = buildGameGraph(game, botPlayer.getId());
        List<StrategicObjective> objectives = identifyStrategicObjectives(gameGraph, botPlayer.getId(), mainObjective);

        log.info("Bot identificó {} objetivos estratégicos", objectives.size());

        // Ejecutar fases del turno con estrategia experta
        performBotReinforcement(botPlayer, game, gameGraph, objectives, mainObjective);
        performBotAttack(botPlayer, game, gameGraph, objectives, mainObjective);
        performBotFortify(botPlayer, game, gameGraph, objectives, mainObjective);
    }

    @Override
    public void performBotReinforcement(PlayerEntity botPlayer, GameEntity game) {
        // Versión simplificada para compatibilidad
        PlayerMainObjective mainObjective = getPlayerMainObjective(botPlayer);
        Map<Long, TerritoryNode> gameGraph = buildGameGraph(game, botPlayer.getId());
        List<StrategicObjective> objectives = identifyStrategicObjectives(gameGraph, botPlayer.getId(), mainObjective);
        performBotReinforcement(botPlayer, game, gameGraph, objectives, mainObjective);
    }

    public void performBotReinforcement(PlayerEntity botPlayer, GameEntity game,
                                        Map<Long, TerritoryNode> gameGraph,
                                        List<StrategicObjective> objectives,
                                        PlayerMainObjective mainObjective) {
        log.info("Bot EXPERT-AGGRESSIVE realizando refuerzos estratégicos para jugador: {}", botPlayer.getId());

        try {
            List<Territory> playerTerritories = gameTerritoryService.getTerritoriesByOwner(
                    game.getId(), botPlayer.getId());

            if (playerTerritories.isEmpty()) {
                log.warn("Bot no tiene territorios para reforzar");
                return;
            }

            // Estrategia EXPERT: Refuerzos basados en objetivo principal
            Map<Long, Integer> reinforcementPlan = planObjectiveBasedReinforcements(
                    playerTerritories, objectives, gameGraph, botPlayer.getId(), mainObjective);

            // Ejecutar plan de refuerzos
            for (Map.Entry<Long, Integer> entry : reinforcementPlan.entrySet()) {
                Territory territory = playerTerritories.stream()
                        .filter(t -> t.getId().equals(entry.getKey()))
                        .findFirst()
                        .orElse(null);

                if (territory != null) {
                    //todo: Implementar cuando esté el servicio de refuerzos
                    // reinforcementService.reinforceTerritory(game.getGameCode(),
                    //     botPlayer.getId(), territory.getId(), entry.getValue());

                    log.info("Bot planea reforzar {} con {} ejércitos (Estrategia: {})",
                            territory.getName(), entry.getValue(),
                            getObjectiveStrategyForTerritory(territory.getId(), objectives, mainObjective));
                }
            }

        } catch (Exception e) {
            log.error("Error en refuerzos estratégicos del bot EXPERT-AGGRESSIVE: {}", e.getMessage());
        }
    }

    @Override
    public void performBotAttack(PlayerEntity botPlayer, GameEntity game) {
        // Versión simplificada para compatibilidad
        PlayerMainObjective mainObjective = getPlayerMainObjective(botPlayer);
        Map<Long, TerritoryNode> gameGraph = buildGameGraph(game, botPlayer.getId());
        List<StrategicObjective> objectives = identifyStrategicObjectives(gameGraph, botPlayer.getId(), mainObjective);
        performBotAttack(botPlayer, game, gameGraph, objectives, mainObjective);
    }

    public void performBotAttack(PlayerEntity botPlayer, GameEntity game,
                                 Map<Long, TerritoryNode> gameGraph,
                                 List<StrategicObjective> objectives,
                                 PlayerMainObjective mainObjective) {
        log.info("Bot EXPERT-AGGRESSIVE ejecutando ataques estratégicos para jugador: {}", botPlayer.getId());

        try {
            // Estrategia EXPERT: Ataques coordinados hacia objetivo principal
            List<AttackPlan> attackPlans = planObjectiveBasedAttacks(gameGraph, objectives, botPlayer.getId(), mainObjective);

            if (attackPlans.isEmpty()) {
                log.info("Bot no identificó ataques estratégicos viables para su objetivo");
                return;
            }

            // Ejecutar planes de ataque en orden de prioridad
            int executedAttacks = 0;
            int maxAttacks = 8; // Expert puede hacer más ataques coordinados

            for (AttackPlan plan : attackPlans) {
                if (executedAttacks >= maxAttacks) break;

                boolean success = executeAttackPlan(plan, game, botPlayer.getId());
                if (success) {
                    executedAttacks++;
                    log.info("Bot ejecutó ataque estratégico: {} -> {} (Objetivo: {} - {})",
                            plan.attackerName, plan.targetName, mainObjective.objectiveType, plan.strategicPurpose);
                }
            }

        } catch (Exception e) {
            log.error("Error en ataques estratégicos del bot EXPERT-AGGRESSIVE: {}", e.getMessage());
        }
    }

    @Override
    public void performBotFortify(PlayerEntity botPlayer, GameEntity game) {
        // Versión simplificada para compatibilidad
        PlayerMainObjective mainObjective = getPlayerMainObjective(botPlayer);
        Map<Long, TerritoryNode> gameGraph = buildGameGraph(game, botPlayer.getId());
        List<StrategicObjective> objectives = identifyStrategicObjectives(gameGraph, botPlayer.getId(), mainObjective);
        performBotFortify(botPlayer, game, gameGraph, objectives, mainObjective);
    }

    @Override
    public double evaluateAttackProbability(PlayerEntity botPlayer, int attackerArmies, int defenderArmies) {
        // Estimación simplificada de probabilidad de éxito
        if (attackerArmies <= 1) return 0.0;

        double ratio = (double) attackerArmies / defenderArmies;

        if (ratio >= 3.0) return 0.85;
        if (ratio >= 2.0) return 0.70;
        if (ratio >= 1.5) return 0.55;
        if (ratio >= 1.2) return 0.40;
        return 0.25;
    }

    //no se usan, quedan por la intefaz
    @Override
    public List<CountryEntity> getBestAttackTargets(PlayerEntity botPlayer, GameEntity game) {
        return List.of();
    }

    @Override
    public List<CountryEntity> getBestDefensePositions(PlayerEntity botPlayer, GameEntity game) {
        return List.of();
    }

    public void performBotFortify(PlayerEntity botPlayer, GameEntity game,
                                  Map<Long, TerritoryNode> gameGraph,
                                  List<StrategicObjective> objectives,
                                  PlayerMainObjective mainObjective) {
        log.info("Bot EXPERT-AGGRESSIVE realizando fortificación estratégica para jugador: {}", botPlayer.getId());

        try {
            // Estrategia EXPERT: Consolidación de fuerzas usando Dijkstra
            List<FortificationPlan> fortificationPlans = planObjectiveBasedFortification(
                    gameGraph, objectives, botPlayer.getId(), mainObjective);

            if (fortificationPlans.isEmpty()) {
                log.info("Bot no identificó movimientos de fortificación estratégicos");
                return;
            }

            // Ejecutar el mejor plan de fortificación
            FortificationPlan bestPlan = fortificationPlans.get(0);
            boolean success = executeFortificationPlan(bestPlan, game, botPlayer.getId());

            if (success) {
                log.info("Bot ejecutó fortificación estratégica: {} -> {} ({} ejércitos) - Propósito: {} ({})",
                        bestPlan.sourceName, bestPlan.targetName, bestPlan.armies,
                        bestPlan.purpose, mainObjective.objectiveType);
            }

        } catch (Exception e) {
            log.error("Error en fortificación estratégica del bot EXPERT-AGGRESSIVE: {}", e.getMessage());
        }
    }

    // ===== MÉTODOS PARA OBTENER Y PROCESAR OBJETIVO PRINCIPAL =====

    /**
     * Obtiene y procesa el objetivo principal del jugador
     */
    private PlayerMainObjective getPlayerMainObjective(PlayerEntity player) {
        ObjectiveEntity objective = player.getObjective();
        if (objective == null) {
            log.warn("Player {} no tiene objetivo asignado", player.getId());
            // Crear un objetivo por defecto
            ObjectiveEntity defaultObjective = new ObjectiveEntity();
            defaultObjective.setDescription("Objetivo por defecto: Expansión territorial");
            // Nota: Necesitarías setear el ObjectiveType aquí según tu implementación
            defaultObjective.setType(ObjectiveType.DESTRUCTION);
            defaultObjective.setTargetData("Reventar Asia y america del Sur");
            defaultObjective.setIsCommon(false);
            return new PlayerMainObjective(defaultObjective);
        }

        log.info("Procesando objetivo del player {}: {} - {}",
                player.getId(), objective.getType(), objective.getDescription());
        return new PlayerMainObjective(objective);
    }

    /**
     * Identifica objetivos estratégicos basados en el objetivo principal del jugador
     */
    private List<StrategicObjective> identifyStrategicObjectives(Map<Long, TerritoryNode> graph,
                                                                 Long playerId,
                                                                 PlayerMainObjective mainObjective) {
        List<StrategicObjective> objectives = new ArrayList<>();

        switch (mainObjective.objectiveType.toUpperCase()) {
            case "OCCUPATION":
                objectives.addAll(identifyOccupationObjectives(graph, playerId, mainObjective));
                break;
            case "DESTRUCTION":
                objectives.addAll(identifyDestructionObjectives(graph, playerId, mainObjective));
                break;
            case "COMMON":
                objectives.addAll(identifyCommonObjectives(graph, playerId, mainObjective));
                break;
            default:
                log.warn("Tipo de objetivo desconocido: {}", mainObjective.objectiveType);
                objectives.addAll(identifyGeneralObjectives(graph, playerId));
        }

        // Agregar objetivos secundarios estratégicos
        objectives.addAll(identifySecondaryObjectives(graph, playerId));

        // Ordenar por prioridad descendente
        objectives.sort((a, b) -> Integer.compare(b.priority, a.priority));
        return objectives;
    }

    /**
     * Identifica objetivos para misiones de ocupación (ej: ocupar Asia)
     */
    private List<StrategicObjective> identifyOccupationObjectives(Map<Long, TerritoryNode> graph,
                                                                  Long playerId,
                                                                  PlayerMainObjective mainObjective) {
        List<StrategicObjective> objectives = new ArrayList<>();

        for (String targetContinent : mainObjective.targetData) {
            // Obtener territorios del continente objetivo
            List<Territory> continentTerritories = getContinentTerritories(targetContinent);

            for (Territory territory : continentTerritories) {
                TerritoryNode node = graph.get(territory.getId());
                if (node != null && !node.ownerId.equals(playerId)) {
                    StrategicObjective objective = new StrategicObjective(
                            node.territoryId, node.name, 90, "MAIN_OBJECTIVE");
                    objective.isMainObjective = true;
                    objective.pathFromClosestOwned = findPathToObjective(node, graph, playerId);
                    objective.estimatedCost = estimateConquestCost(node, graph, playerId);
                    objectives.add(objective);

                    log.info("Objetivo de ocupación identificado: {} en continente {}",
                            territory.getName(), targetContinent);
                }
            }
        }

        return objectives;
    }

    /**
     * Identifica objetivos para misiones de destrucción (eliminar un jugador)
     */
    private List<StrategicObjective> identifyDestructionObjectives(Map<Long, TerritoryNode> graph,
                                                                   Long playerId,
                                                                   PlayerMainObjective mainObjective) {
        List<StrategicObjective> objectives = new ArrayList<>();

        for (String targetPlayerData : mainObjective.targetData) {
            // Asumir que targetPlayerData contiene el ID del jugador objetivo
            try {
                Long targetPlayerId = Long.parseLong(targetPlayerData);

                // Encontrar todos los territorios del jugador objetivo
                List<TerritoryNode> targetTerritories = graph.values().stream()
                        .filter(node -> node.ownerId.equals(targetPlayerId))
                        .collect(Collectors.toList());

                // Priorizar territorios más débiles del enemigo
                for (TerritoryNode territory : targetTerritories) {
                    int priority = 95 - territory.armies; // Más débiles = mayor prioridad
                    StrategicObjective objective = new StrategicObjective(
                            territory.territoryId, territory.name, priority, "MAIN_OBJECTIVE");
                    objective.isMainObjective = true;
                    objective.pathFromClosestOwned = findPathToObjective(territory, graph, playerId);
                    objective.estimatedCost = estimateConquestCost(territory, graph, playerId);
                    objectives.add(objective);
                }

                log.info("Objetivos de destrucción identificados: {} territorios del jugador {}",
                        targetTerritories.size(), targetPlayerId);

            } catch (NumberFormatException e) {
                log.warn("No se pudo parsear el ID del jugador objetivo: {}", targetPlayerData);
            }
        }

        return objectives;
    }

    /**
     * Identifica objetivos para misiones comunes
     */
    private List<StrategicObjective> identifyCommonObjectives(Map<Long, TerritoryNode> graph,
                                                              Long playerId,
                                                              PlayerMainObjective mainObjective) {
        List<StrategicObjective> objectives = new ArrayList<>();

        // Los objetivos comunes generalmente requieren ocupar un número específico de territorios
        // Priorizar territorios fáciles de conquistar para alcanzar el número requerido

        List<TerritoryNode> enemyTerritories = graph.values().stream()
                .filter(node -> !node.ownerId.equals(playerId))
                .sorted(Comparator.comparingInt(node -> node.armies))
                .collect(Collectors.toList());

        int targetCount = Math.min(15, enemyTerritories.size()); // Ejemplo: 15 territorios

        for (int i = 0; i < targetCount; i++) {
            TerritoryNode territory = enemyTerritories.get(i);
            int priority = 80 - (i * 2); // Prioridad decreciente

            StrategicObjective objective = new StrategicObjective(
                    territory.territoryId, territory.name, priority, "MAIN_OBJECTIVE");
            objective.isMainObjective = true;
            objective.pathFromClosestOwned = findPathToObjective(territory, graph, playerId);
            objective.estimatedCost = estimateConquestCost(territory, graph, playerId);
            objectives.add(objective);
        }

        log.info("Objetivos comunes identificados: {} territorios prioritarios", targetCount);
        return objectives;
    }

    /**
     * Identifica objetivos generales cuando no se puede determinar el tipo específico
     */
    private List<StrategicObjective> identifyGeneralObjectives(Map<Long, TerritoryNode> graph, Long playerId) {
        List<StrategicObjective> objectives = new ArrayList<>();

        for (TerritoryNode node : graph.values()) {
            if (!node.ownerId.equals(playerId)) {
                int priority = evaluateStrategicValue(node, graph, playerId);

                if (priority > 0) {
                    String type = determineObjectiveType(node, graph, playerId);
                    StrategicObjective objective = new StrategicObjective(
                            node.territoryId, node.name, priority, type);

                    objective.pathFromClosestOwned = findPathToObjective(node, graph, playerId);
                    objective.estimatedCost = estimateConquestCost(node, graph, playerId);
                    objectives.add(objective);
                }
            }
        }

        return objectives;
    }

    /**
     * Identifica objetivos secundarios que complementan el objetivo principal
     */
    private List<StrategicObjective> identifySecondaryObjectives(Map<Long, TerritoryNode> graph, Long playerId) {
        List<StrategicObjective> objectives = new ArrayList<>();

        // Territorios que proporcionan bonificaciones de continente
        // Territorios puente estratégicos
        // Fortalezas enemigas que bloquean el progreso

        for (TerritoryNode node : graph.values()) {
            if (!node.ownerId.equals(playerId)) {
                // Evaluar como objetivo secundario
                if (node.neighbors.size() >= 4) { // Territorio central
                    StrategicObjective objective = new StrategicObjective(
                            node.territoryId, node.name, 30, "BRIDGE_TERRITORY");
                    objective.pathFromClosestOwned = findPathToObjective(node, graph, playerId);
                    objective.estimatedCost = estimateConquestCost(node, graph, playerId);
                    objectives.add(objective);
                }
            }
        }

        return objectives;
    }

    // ===== MÉTODOS DE PLANIFICACIÓN BASADA EN OBJETIVOS =====

    private Map<Long, Integer> planObjectiveBasedReinforcements(List<Territory> playerTerritories,
                                                                List<StrategicObjective> objectives,
                                                                Map<Long, TerritoryNode> graph,
                                                                Long playerId,
                                                                PlayerMainObjective mainObjective) {
        Map<Long, Integer> plan = new HashMap<>();

        // Priorizar refuerzos para territorios que conducen al objetivo principal
        List<StrategicObjective> mainObjectives = objectives.stream()
                .filter(obj -> obj.isMainObjective)
                .limit(3)
                .collect(Collectors.toList());

        for (StrategicObjective objective : mainObjectives) {
            if (!objective.pathFromClosestOwned.isEmpty()) {
                Long firstOwnedInPath = objective.pathFromClosestOwned.get(0);
                plan.put(firstOwnedInPath, plan.getOrDefault(firstOwnedInPath, 0) + 3);
            }
        }

        // Si no hay plan específico, reforzar según tipo de objetivo
        if (plan.isEmpty()) {
            plan = planDefaultReinforcements(playerTerritories, graph, playerId, mainObjective);
        }

        return plan;
    }

    private Map<Long, Integer> planDefaultReinforcements(List<Territory> playerTerritories,
                                                         Map<Long, TerritoryNode> graph,
                                                         Long playerId,
                                                         PlayerMainObjective mainObjective) {
        Map<Long, Integer> plan = new HashMap<>();

        switch (mainObjective.objectiveType.toUpperCase()) {
            case "DESTRUCTION":
                // Reforzar territorios fronterizos más agresivos
                Territory strongestBorder = playerTerritories.stream()
                        .filter(t -> isBorderTerritory(t, graph, playerId))
                        .max(Comparator.comparingInt(Territory::getArmies))
                        .orElse(playerTerritories.get(0));
                plan.put(strongestBorder.getId(), 4);
                break;

            case "OCCUPATION":
                // Reforzar territorios que dan acceso a continentes objetivo
                Territory strategicAccess = findBestAccessToTargetContinent(playerTerritories, mainObjective);
                if (strategicAccess != null) {
                    plan.put(strategicAccess.getId(), 3);
                } else {
                    plan.put(playerTerritories.get(0).getId(), 2);
                }
                break;

            default:
                // Refuerzo equilibrado
                Territory weakestBorder = playerTerritories.stream()
                        .filter(t -> isBorderTerritory(t, graph, playerId))
                        .min(Comparator.comparingInt(Territory::getArmies))
                        .orElse(playerTerritories.get(0));
                plan.put(weakestBorder.getId(), 2);
        }

        return plan;
    }

    private List<AttackPlan> planObjectiveBasedAttacks(Map<Long, TerritoryNode> graph,
                                                       List<StrategicObjective> objectives,
                                                       Long playerId,
                                                       PlayerMainObjective mainObjective) {
        List<AttackPlan> plans = new ArrayList<>();

        // Priorizar ataques hacia objetivos principales
        List<StrategicObjective> priorityObjectives = objectives.stream()
                .filter(obj -> obj.isMainObjective)
                .limit(5)
                .collect(Collectors.toList());

        for (StrategicObjective objective : priorityObjectives) {
            List<Long> path = objective.pathFromClosestOwned;

            if (path.size() >= 2) {
                Long attackerId = path.get(path.size() - 2);
                TerritoryNode attacker = graph.get(attackerId);
                TerritoryNode target = graph.get(objective.territoryId);

                if (attacker.ownerId.equals(playerId) && attacker.armies > 1) {
                    int attackingArmies = calculateOptimalAttackForce(attacker.armies, target.armies, mainObjective);
                    double probability = evaluateAttackProbability(null, attacker.armies, target.armies);

                    if (probability > getMinimumAttackProbability(mainObjective)) {
                        AttackPlan plan = new AttackPlan(
                                attackerId, attacker.name,
                                objective.territoryId, target.name,
                                attackingArmies, probability,
                                "Objetivo Principal: " + mainObjective.objectiveType,
                                objective.priority
                        );
                        plans.add(plan);
                    }
                }
            }
        }

        // Agregar ataques secundarios si es necesario
        if (plans.size() < 3) {
            plans.addAll(planSecondaryAttacks(graph, objectives, playerId));
        }

        plans.sort((a, b) -> {
            int priorityComparison = Integer.compare(b.priority, a.priority);
            if (priorityComparison != 0) return priorityComparison;
            return Double.compare(b.successProbability, a.successProbability);
        });

        return plans;
    }

    private List<FortificationPlan> planObjectiveBasedFortification(Map<Long, TerritoryNode> graph,
                                                                    List<StrategicObjective> objectives,
                                                                    Long playerId,
                                                                    PlayerMainObjective mainObjective) {
        List<FortificationPlan> plans = new ArrayList<>();

        // Consolidar fuerzas hacia el objetivo principal
        List<StrategicObjective> mainObjectives = objectives.stream()
                .filter(obj -> obj.isMainObjective)
                .limit(2)
                .collect(Collectors.toList());

        List<TerritoryNode> ownedTerritories = graph.values().stream()
                .filter(node -> node.ownerId.equals(playerId))
                .collect(Collectors.toList());

        for (StrategicObjective objective : mainObjectives) {
            if (!objective.pathFromClosestOwned.isEmpty()) {
                Long targetTerritoryId = objective.pathFromClosestOwned.get(0);
                TerritoryNode target = graph.get(targetTerritoryId);

                for (TerritoryNode source : ownedTerritories) {
                    if (source.armies > 3 && !source.territoryId.equals(targetTerritoryId)) {
                        List<Long> path = findShortestPath(graph, source.territoryId, targetTerritoryId, playerId);

                        if (!path.isEmpty() && path.size() <= getMaxFortificationDistance(mainObjective)) {
                            int armiesToMove = calculateOptimalFortificationForce(source.armies, mainObjective);
                            if (armiesToMove > 0) {
                                FortificationPlan plan = new FortificationPlan(
                                        source.territoryId, source.name,
                                        targetTerritoryId, target.name,
                                        armiesToMove,
                                        "Consolidación para " + mainObjective.objectiveType + ": " + objective.name,
                                        objective.priority,
                                        path
                                );
                                plans.add(plan);
                            }
                        }
                    }
                }
            }
        }

        plans.sort((a, b) -> Integer.compare(b.priority, a.priority));
        return plans;
    }

    // ===== MÉTODOS AUXILIARES ESPECÍFICOS PARA OBJETIVOS =====

    private int calculateOptimalAttackForce(int attackerArmies, int defenderArmies, PlayerMainObjective mainObjective) {
        int baseForce = Math.min(attackerArmies - 1, Math.max(2, defenderArmies + 1));

        // Ajustar según tipo de objetivo
        switch (mainObjective.objectiveType.toUpperCase()) {
            case "DESTRUCTION":
                return Math.min(attackerArmies - 1, baseForce + 2); // Más agresivo
            case "OCCUPATION":
                return baseForce; // Balanceado
            default:
                return Math.max(2, baseForce - 1); // Más conservador
        }
    }

    private double getMinimumAttackProbability(PlayerMainObjective mainObjective) {
        switch (mainObjective.objectiveType.toUpperCase()) {
            case "DESTRUCTION":
                return 0.45; // Más agresivo, acepta menor probabilidad
            case "OCCUPATION":
                return 0.55; // Balanceado
            default:
                return 0.60; // Más conservador
        }
    }

    private int getMaxFortificationDistance(PlayerMainObjective mainObjective) {
        switch (mainObjective.objectiveType.toUpperCase()) {
            case "DESTRUCTION":
                return 5; // Permite movimientos más largos para concentrar fuerzas
            case "OCCUPATION":
                return 4; // Distancia moderada
            default:
                return 3; // Más conservador
        }
    }

    private int calculateOptimalFortificationForce(int sourceArmies, PlayerMainObjective mainObjective) {
        int maxMove = sourceArmies - 1;

        switch (mainObjective.objectiveType.toUpperCase()) {
            case "DESTRUCTION":
                return Math.max(1, maxMove * 80 / 100); // Mueve 80% de los ejércitos
            case "OCCUPATION":
                return Math.max(1, maxMove * 60 / 100); // Mueve 60%
            default:
                return Math.max(1, maxMove * 40 / 100); // Mueve 40%
        }
    }

    // ===== MÉTODOS DE EJECUCIÓN DE PLANES =====

    private boolean executeAttackPlan(AttackPlan plan, GameEntity game, Long botPlayerId) {
        try {
            AttackDto attackDto = new AttackDto();
            attackDto.setPlayerId(botPlayerId);
            attackDto.setAttackerCountryId(plan.attackerTerritoryId);
            attackDto.setDefenderCountryId(plan.targetTerritoryId);
            attackDto.setAttackingArmies(plan.attackingArmies);

            CombatResultDto result = combatService.performCombat(game.getGameCode(),attackDto);
            return result != null;
        } catch (Exception e) {
            log.error("Error ejecutando ataque: {}", e.getMessage());
            return false;
        }
    }

    private boolean executeFortificationPlan(FortificationPlan plan, GameEntity game, Long botPlayerId) {
        try {
            FortifyDto fortifyDto = new FortifyDto();
            fortifyDto.setPlayerId(botPlayerId);
            fortifyDto.setFromCountryId(plan.sourceTerritoryId);
            fortifyDto.setToCountryId(plan.targetTerritoryId);
            fortifyDto.setArmies(plan.armies);

            return fortificationService.performFortification(game.getGameCode(),fortifyDto);
        } catch (Exception e) {
            log.error("Error ejecutando fortificación: {}", e.getMessage());
            return false;
        }
    }

    // ===== ESTRUCTURAS DE DATOS PARA PLANES =====

    private static class AttackPlan {
        Long attackerTerritoryId;
        String attackerName;
        Long targetTerritoryId;
        String targetName;
        int attackingArmies;
        double successProbability;
        String strategicPurpose;
        int priority;

        public AttackPlan(Long attackerTerritoryId, String attackerName,
                          Long targetTerritoryId, String targetName,
                          int attackingArmies, double successProbability,
                          String strategicPurpose, int priority) {
            this.attackerTerritoryId = attackerTerritoryId;
            this.attackerName = attackerName;
            this.targetTerritoryId = targetTerritoryId;
            this.targetName = targetName;
            this.attackingArmies = attackingArmies;
            this.successProbability = successProbability;
            this.strategicPurpose = strategicPurpose;
            this.priority = priority;
        }
    }

    private static class FortificationPlan {
        Long sourceTerritoryId;
        String sourceName;
        Long targetTerritoryId;
        String targetName;
        int armies;
        String purpose;
        int priority;
        List<Long> path;

        public FortificationPlan(Long sourceTerritoryId, String sourceName,
                                 Long targetTerritoryId, String targetName,
                                 int armies, String purpose, int priority, List<Long> path) {
            this.sourceTerritoryId = sourceTerritoryId;
            this.sourceName = sourceName;
            this.targetTerritoryId = targetTerritoryId;
            this.targetName = targetName;
            this.armies = armies;
            this.purpose = purpose;
            this.priority = priority;
            this.path = path != null ? path : new ArrayList<>();
        }
    }

    // ===== MÉTODOS AUXILIARES GENERALES =====

    private Map<Long, TerritoryNode> buildGameGraph(GameEntity game, Long playerId) {
        Map<Long, TerritoryNode> graph = new HashMap<>();

        try {
            List<Territory> allTerritories = gameTerritoryService.getAllAvailableTerritories();

            for (Territory territory : allTerritories) {
                TerritoryNode node = new TerritoryNode(territory);
                graph.put(territory.getId(), node);
            }

            // Establecer conexiones
            for (Territory territory : allTerritories) {
                TerritoryNode node = graph.get(territory.getId());
                List<Long> neighbors = getNeighborTerritories(territory.getId());
                node.neighbors.addAll(neighbors);
            }

        } catch (Exception e) {
            log.error("Error construyendo grafo del juego: {}", e.getMessage());
        }

        return graph;
    }

    private List<Long> findPathToObjective(TerritoryNode target, Map<Long, TerritoryNode> graph, Long playerId) {
        return findShortestPathToOwnedTerritory(graph, target.territoryId, playerId);
    }

    private List<Long> findShortestPathToOwnedTerritory(Map<Long, TerritoryNode> graph, Long targetId, Long playerId) {
        Map<Long, Integer> distances = new HashMap<>();
        Map<Long, Long> previous = new HashMap<>();
        PriorityQueue<Long> queue = new PriorityQueue<>(Comparator.comparing(distances::get));

        for (Long id : graph.keySet()) {
            distances.put(id, Integer.MAX_VALUE);
        }

        List<Long> ownedTerritories = graph.values().stream()
                .filter(node -> node.ownerId.equals(playerId))
                .map(node -> node.territoryId)
                .collect(Collectors.toList());

        for (Long ownedId : ownedTerritories) {
            distances.put(ownedId, 0);
            queue.offer(ownedId);
        }

        while (!queue.isEmpty()) {
            Long current = queue.poll();

            if (current.equals(targetId)) {
                return reconstructPath(previous, current, ownedTerritories);
            }

            TerritoryNode currentNode = graph.get(current);
            if (currentNode == null) continue;

            for (Long neighborId : currentNode.neighbors) {
                int newDistance = distances.get(current) + 1;
                if (newDistance < distances.get(neighborId)) {
                    distances.put(neighborId, newDistance);
                    previous.put(neighborId, current);
                    queue.offer(neighborId);
                }
            }
        }

        return new ArrayList<>();
    }

    private List<Long> findShortestPath(Map<Long, TerritoryNode> graph, Long startId, Long endId, Long playerId) {
        Map<Long, Integer> distances = new HashMap<>();
        Map<Long, Long> previous = new HashMap<>();
        PriorityQueue<Long> queue = new PriorityQueue<>(Comparator.comparing(distances::get));

        for (Long id : graph.keySet()) {
            distances.put(id, Integer.MAX_VALUE);
        }

        distances.put(startId, 0);
        queue.offer(startId);

        while (!queue.isEmpty()) {
            Long current = queue.poll();

            if (current.equals(endId)) {
                List<Long> path = new ArrayList<>();
                Long node = endId;
                while (node != null) {
                    path.add(0, node);
                    node = previous.get(node);
                }
                return path;
            }

            TerritoryNode currentNode = graph.get(current);
            if (currentNode == null) continue;

            for (Long neighborId : currentNode.neighbors) {
                TerritoryNode neighbor = graph.get(neighborId);
                if (neighbor != null && neighbor.ownerId.equals(playerId)) {
                    int newDistance = distances.get(current) + 1;
                    if (newDistance < distances.get(neighborId)) {
                        distances.put(neighborId, newDistance);
                        previous.put(neighborId, current);
                        queue.offer(neighborId);
                    }
                }
            }
        }

        return new ArrayList<>();
    }

    private List<Long> reconstructPath(Map<Long, Long> previous, Long target, List<Long> sources) {
        List<Long> path = new ArrayList<>();
        Long current = target;

        while (current != null && !sources.contains(current)) {
            path.add(0, current);
            current = previous.get(current);
        }

        if (current != null) {
            path.add(0, current);
        }

        return path;
    }

    private int estimateConquestCost(TerritoryNode target, Map<Long, TerritoryNode> graph, Long playerId) {
        return target.armies + 2; // Estimación simple
    }

    private int evaluateStrategicValue(TerritoryNode node, Map<Long, TerritoryNode> graph, Long playerId) {
        int value = 0;

        // Valor por número de conexiones
        value += node.neighbors.size() * 5;

        // Penalizar por ejércitos defensores
        value -= node.armies * 3;

        // Bonus si está cerca de territorios propios
        long ownedNeighbors = node.neighbors.stream()
                .map(graph::get)
                .filter(Objects::nonNull)
                .filter(neighbor -> neighbor.ownerId.equals(playerId))
                .count();

        value += (int) ownedNeighbors * 10;

        return Math.max(0, value);
    }

    private String determineObjectiveType(TerritoryNode node, Map<Long, TerritoryNode> graph, Long playerId) {
        if (node.neighbors.size() >= 4) {
            return "BRIDGE_TERRITORY";
        } else if (node.armies > 5) {
            return "ENEMY_STRONGHOLD";
        } else {
            return "EXPANSION_TARGET";
        }
    }

    private List<AttackPlan> planSecondaryAttacks(Map<Long, TerritoryNode> graph,
                                                  List<StrategicObjective> objectives,
                                                  Long playerId) {
        List<AttackPlan> plans = new ArrayList<>();

        // Buscar objetivos secundarios factibles
        List<StrategicObjective> secondaryObjectives = objectives.stream()
                .filter(obj -> !obj.isMainObjective && obj.priority > 20)
                .limit(3)
                .collect(Collectors.toList());

        for (StrategicObjective objective : secondaryObjectives) {
            TerritoryNode target = graph.get(objective.territoryId);
            if (target == null) continue;

            for (Long neighborId : target.neighbors) {
                TerritoryNode attacker = graph.get(neighborId);
                if (attacker != null && attacker.ownerId.equals(playerId) && attacker.armies > 2) {
                    double probability = evaluateAttackProbability(null, attacker.armies, target.armies);

                    if (probability > 0.50) {
                        AttackPlan plan = new AttackPlan(
                                attacker.territoryId, attacker.name,
                                target.territoryId, target.name,
                                Math.min(attacker.armies - 1, target.armies + 1),
                                probability, "Objetivo Secundario: " + objective.type,
                                objective.priority
                        );
                        plans.add(plan);
                    }
                }
            }
        }

        return plans;
    }

    private boolean isBorderTerritory(Territory territory, Map<Long, TerritoryNode> graph, Long playerId) {
        TerritoryNode node = graph.get(territory.getId());
        if (node == null) return false;

        return node.neighbors.stream()
                .map(graph::get)
                .filter(Objects::nonNull)
                .anyMatch(neighbor -> !neighbor.ownerId.equals(playerId));
    }

    private Territory findBestAccessToTargetContinent(List<Territory> playerTerritories, PlayerMainObjective mainObjective) {
        // Implementación simplificada - selecciona territorio con más ejércitos en frontera
        return playerTerritories.stream()
                .filter(t -> t.getArmies() > 2)
                .max(Comparator.comparingInt(Territory::getArmies))
                .orElse(null);
    }

    private String getObjectiveStrategyForTerritory(Long territoryId, List<StrategicObjective> objectives, PlayerMainObjective mainObjective) {
        return objectives.stream()
                .filter(obj -> obj.territoryId.equals(territoryId))
                .map(obj -> obj.type)
                .findFirst()
                .orElse("Estrategia General");
    }

    // ===== MÉTODOS QUE NECESITAN IMPLEMENTACIÓN ESPECÍFICA DEL SISTEMA =====

    /**
     * Obtiene todos los territorios de un continente específico
     * @param continentName Nombre del continente
     * @return Lista de territorios del continente
     */
    private List<Territory> getContinentTerritories(String continentName) {
        try {
            log.debug("Obteniendo territorios del continente: {}", continentName);

            // Obtener todos los territorios disponibles
            List<Territory> allTerritories = gameTerritoryService.getAllAvailableTerritories();

            // Filtrar por continente
            List<Territory> continentTerritories = allTerritories.stream()
                    .filter(territory -> territory.getContinentName() != null &&
                            territory.getContinentName().equalsIgnoreCase(continentName))
                    .collect(Collectors.toList());

            log.debug("Encontrados {} territorios en el continente {}",
                    continentTerritories.size(), continentName);

            return continentTerritories;

        } catch (Exception e) {
            log.error("Error obteniendo territorios del continente {}: {}", continentName, e.getMessage());
            return new ArrayList<>();
        }
    }

    /** Obtiene los IDs de los territorios vecinos de un territorio específico
    * @param territoryId ID del territorio
    * @return Lista de IDs de territorios vecinos
    */
    private List<Long> getNeighborTerritories(Long territoryId) {
        try {
            log.debug("Obteniendo vecinos del territorio con ID: {}", territoryId);

            // Obtener todos los territorios disponibles
            List<Territory> allTerritories = gameTerritoryService.getAllAvailableTerritories();

            // Buscar el territorio específico
            Territory territory = allTerritories.stream()
                    .filter(t -> t.getId().equals(territoryId))
                    .findFirst()
                    .orElse(null);

            if (territory == null) {
                log.warn("No se encontró territorio con ID: {}", territoryId);
                return new ArrayList<>();
            }

            // Obtener los IDs de los vecinos
            List<Long> neighborIds = new ArrayList<>(territory.getNeighborIds());

            log.debug("Territorio {} tiene {} vecinos: {}",
                    territory.getName(), neighborIds.size(), neighborIds);

            return neighborIds;

        } catch (Exception e) {
            log.error("Error obteniendo vecinos del territorio {}: {}", territoryId, e.getMessage());
            return new ArrayList<>();
        }
    }
}