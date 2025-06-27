package ar.edu.utn.frc.tup.piii.FactoryBots.NoviceStrategies;

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
public class NoviceAggressiveExecutor implements BotStrategyExecutor {

    @Autowired
    private CombatService combatService;

    @Autowired
    private FortificationService fortificationService;

    //faltaría implementar el servicio de refuerzos (reinforcementService)

    @Autowired
    private GameTerritoryService gameTerritoryService;

    private final Random random = new Random();

    @Override
    public BotLevel getLevel() {
        return BotLevel.NOVICE;
    }

    @Override
    public BotStrategy getStrategy() {
        return BotStrategy.AGGRESSIVE;
    }

    @Override
    public void executeTurn(PlayerEntity botPlayer, GameEntity game) {
        log.info("Ejecutando turno para bot NOVICE-AGGRESSIVE: {}", botPlayer.getBotProfile().getBotName());

        // ORDEN CORRECTO de las fases del turno
        performBotReinforcement(botPlayer, game);  // 1º Refuerzo
        performBotAttack(botPlayer, game);         // 2º Ataque
        performBotFortify(botPlayer, game);        // 3º Reagrupación
    }

    @Override
    public void performBotReinforcement(PlayerEntity botPlayer, GameEntity game) {
        log.info("Bot NOVICE-AGGRESSIVE realizando refuerzos para jugador: {}", botPlayer.getId());

        try {
            // Estrategia NOVICE-AGGRESSIVE para refuerzos:
            // - Refuerza territorios fronterizos (que tienen enemigos vecinos)
            // - Prioriza territorios con más ejércitos para crear "puntos fuertes"

            List<Territory> playerTerritories = gameTerritoryService.getTerritoriesByOwner(
                    game.getId(), botPlayer.getId());

            if (playerTerritories.isEmpty()) {
                log.warn("Bot no tiene territorios para reforzar");
                return;
            }

            // Encontrar territorios fronterizos (que tienen enemigos vecinos)
            List<Territory> borderTerritories = playerTerritories.stream()
                    .filter(territory -> {
                        List<Territory> neighbors = gameTerritoryService.getNeighborTerritories(
                                game.getId(), territory.getId());
                        return neighbors.stream()
                                .anyMatch(neighbor -> !neighbor.getOwnerId().equals(botPlayer.getId()));
                    })
                    .toList();

            List<Territory> targetTerritories = borderTerritories.isEmpty() ?
                    playerTerritories : borderTerritories;

            // Estrategia agresiva: reforzar el territorio más fuerte para crear amenaza
            Territory strongestTerritory = targetTerritories.stream()
                    .max((t1, t2) -> Integer.compare(t1.getArmies(), t2.getArmies()))
                    .orElse(targetTerritories.get(0));


            //todo: Terminar de reforzar el territorio

            // Usar el servicio de refuerzos (todavia no está implementado),(pedro)
            // reinforcementService.reinforceTerritory(game.getGameCode(), botPlayer.getId(),
            //     strongestTerritory.getId(), availableReinforcements);


            log.info("Bot reforzó territorio: {} con {} ejércitos",
                    strongestTerritory.getName(), strongestTerritory.getArmies());

        } catch (Exception e) {
            log.error("Error en refuerzos del bot NOVICE-AGGRESSIVE: {}", e.getMessage());
        }
    }

    @Override
    public void performBotAttack(PlayerEntity botPlayer, GameEntity game) {
        log.info("Bot NOVICE-AGGRESSIVE realizando ataques para jugador: {}", botPlayer.getId());

        try {
            // Estrategia NOVICE-AGGRESSIVE para ataques:
            // - Ataca desde territorios con más de 2 ejércitos
            // - Elige objetivos más débiles
            // - No planifica mucho, ataca oportunísticamente

            List<Territory> attackableTerritories = combatService.getAttackableTerritoriesForPlayer(
                    game.getGameCode(), botPlayer.getId());

            if (attackableTerritories.isEmpty()) {
                log.info("Bot no tiene territorios desde donde atacar");
                return;
            }

            // Realizar varios ataques (comportamiento agresivo)
            int maxAttacks = 3; // Novice no ataca demasiado
            int attackCount = 0;

            for (Territory attackerTerritory : attackableTerritories) {
                if (attackCount >= maxAttacks) break;

                List<Territory> targets = combatService.getTargetsForTerritory(
                        game.getGameCode(), attackerTerritory.getId(), botPlayer.getId());

                if (targets.isEmpty()) continue;

                // Estrategia agresiva: atacar el territorio más débil
                Territory weakestTarget = targets.stream()
                        .min((t1, t2) -> Integer.compare(t1.getArmies(), t2.getArmies()))
                        .orElse(targets.get(0));

                // Calcular ejércitos para atacar (estrategia simple)
                int attackingArmies = Math.min(
                        attackerTerritory.getArmies() - 1, // Dejar al menos 1
                        3 // Máximo 3 para un ataque simple
                );

                if (attackingArmies > 0) {
                    AttackDto attackDto = AttackDto.builder()
                            .playerId(botPlayer.getId())
                            .attackerCountryId(attackerTerritory.getId())
                            .defenderCountryId(weakestTarget.getId())
                            .attackingArmies(attackingArmies)
                            .build();

                    CombatResultDto result = combatService.performCombat(game.getGameCode(), attackDto);

                    log.info("Bot atacó desde {} hacia {}: Conquistado={}",
                            attackerTerritory.getName(), weakestTarget.getName(),
                            result.getTerritoryConquered());

                    attackCount++;
                }
            }

        } catch (Exception e) {
            log.error("Error en ataques del bot NOVICE-AGGRESSIVE: {}", e.getMessage());
        }
    }

    @Override
    public void performBotFortify(PlayerEntity botPlayer, GameEntity game) {
        log.info("Bot NOVICE-AGGRESSIVE realizando fortificación para jugador: {}", botPlayer.getId());

        try {
            // Estrategia NOVICE-AGGRESSIVE para fortificación:
            // - Mueve ejércitos hacia territorios fronterizos
            // - Estrategia simple: desde territorios seguros hacia frontera

            List<Territory> fortifiableTerritories = fortificationService.getFortifiableTerritoriesForPlayer(
                    game.getGameCode(), botPlayer.getId());

            if (fortifiableTerritories.isEmpty()) {
                log.info("Bot no tiene territorios desde donde fortificar");
                return;
            }

            // Buscar un territorio "seguro" (que no tenga enemigos vecinos)
            Territory safeTerritory = fortifiableTerritories.stream()
                    .filter(territory -> {
                        List<Territory> neighbors = gameTerritoryService.getNeighborTerritories(
                                game.getId(), territory.getId());
                        return neighbors.stream()
                                .allMatch(neighbor -> neighbor.getOwnerId().equals(botPlayer.getId()));
                    })
                    .findFirst()
                    .orElse(null);

            if (safeTerritory == null) {
                log.info("Bot no encontró territorios seguros para fortificar");
                return;
            }

            List<Territory> possibleTargets = fortificationService.getFortificationTargetsForTerritory(
                    game.getGameCode(), safeTerritory.getId(), botPlayer.getId());

            if (possibleTargets.isEmpty()) {
                log.info("No hay objetivos válidos para fortificación");
                return;
            }

            // Elegir un objetivo fronterizo al azar (comportamiento novice)
            Territory targetTerritory = possibleTargets.get(random.nextInt(possibleTargets.size()));

            int maxMovableArmies = fortificationService.getMaxMovableArmies(
                    game.getGameCode(), safeTerritory.getId());

            if (maxMovableArmies > 0) {
                // Mover la mitad de los ejércitos disponibles (estrategia simple)
                int armiesToMove = Math.max(1, maxMovableArmies / 2);

                FortifyDto fortifyDto = new FortifyDto();
                fortifyDto.setPlayerId(botPlayer.getId());
                fortifyDto.setFromCountryId(safeTerritory.getId());
                fortifyDto.setToCountryId(targetTerritory.getId());
                fortifyDto.setArmies(armiesToMove);

                boolean success = fortificationService.performFortification(
                        game.getGameCode(), fortifyDto);

                if (success) {
                    log.info("Bot fortificó desde {} hacia {} con {} ejércitos",
                            safeTerritory.getName(), targetTerritory.getName(), armiesToMove);
                } else {
                    log.warn("Falló la fortificación del bot");
                }
            }

        } catch (Exception e) {
            log.error("Error en fortificación del bot NOVICE-AGGRESSIVE: {}", e.getMessage());
        }
    }

    //no lo usamos, pero lo dejamos por si sí
    @Override
    public double evaluateAttackProbability(PlayerEntity botPlayer, int attackerArmies, int defenderArmies) {
        return 0;
    }

    @Override
    public List<CountryEntity> getBestAttackTargets(PlayerEntity botPlayer, GameEntity game) {
        return List.of();
    }

    @Override
    public List<CountryEntity> getBestDefensePositions(PlayerEntity botPlayer, GameEntity game) {
        return List.of();
    }
}