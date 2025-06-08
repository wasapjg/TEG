package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.game.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.game.CombatResultDto;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.service.interfaces.CombatService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class CombatServiceImpl implements CombatService {

    @Autowired
    private GameTerritoryService gameTerritoryService;

    @Autowired
    private GameService gameService;

    private final Random random = new Random();

    /**
     * Ejecuta un combate completo entre dos territorios.
     */
    @Override
    @Transactional
    public CombatResultDto performCombat(String gameCode, AttackDto attackDto) {
        // 1. Obtener el juego y validar estado
        Game game = gameService.findByGameCode(gameCode);
        validateGameStateForCombat(game);

        // 2. Obtener territorios involucrados
        Territory attackerTerritory = gameTerritoryService.getTerritoryByGameAndCountry(
                game.getId(), attackDto.getAttackerCountryId());
        Territory defenderTerritory = gameTerritoryService.getTerritoryByGameAndCountry(
                game.getId(), attackDto.getDefenderCountryId());

        // 3. Validar el ataque
        validateAttack(game, attackDto, attackerTerritory, defenderTerritory);

        // 4. Determinar cantidad de dados
        int attackerDiceCount = determineAttackerDice(attackerTerritory, attackDto.getAttackingArmies());
        int defenderDiceCount = determineDefenderDice(defenderTerritory);

        // 5. Tirar dados
        List<Integer> attackerDice = rollDice(attackerDiceCount);
        List<Integer> defenderDice = rollDice(defenderDiceCount);

        // 6. Resolver combate
        CombatResult combatResult = resolveCombat(attackerDice, defenderDice);

        // 7. Aplicar pérdidas
        applyLosses(game.getId(), attackDto, combatResult);

        // 8. Verificar conquista
        boolean territoryConquered = checkAndHandleConquest(game.getId(), attackDto, combatResult);

        // 9. Construir resultado
        return buildCombatResultDto(
                attackerTerritory, defenderTerritory,
                attackerDice, defenderDice,
                combatResult, territoryConquered);
    }

    /**
     * Valida que el juego esté en un estado válido para combate.
     */
    private void validateGameStateForCombat(Game game) {
        switch (game.getState()) {
            case HOSTILITY_ONLY:
            case NORMAL_PLAY:
                // Estados válidos para combate
                break;
            default:
                throw new InvalidGameStateException(
                        "Combat not allowed in current game state: " + game.getState());
        }
    }

    /**
     * Valida que el ataque sea legal según las reglas del TEG.
     */
    private void validateAttack(Game game, AttackDto attackDto, Territory attacker, Territory defender) {
        // Verificar que los territorios existan
        if (attacker == null) {
            throw new IllegalArgumentException("Attacker territory not found: " + attackDto.getAttackerCountryId());
        }
        if (defender == null) {
            throw new IllegalArgumentException("Defender territory not found: " + attackDto.getDefenderCountryId());
        }

        // Verificar propiedad del territorio atacante
        if (!attackDto.getPlayerId().equals(attacker.getOwnerId())) {
            throw new IllegalArgumentException("Player doesn't own the attacking territory");
        }

        // Verificar que no sea su propio territorio
        if (attackDto.getPlayerId().equals(defender.getOwnerId())) {
            throw new IllegalArgumentException("Cannot attack your own territory");
        }

        // Verificar que sean territorios vecinos
        if (!areTerritoriesNeighbors(attacker.getId(), defender.getId())) {
            throw new IllegalArgumentException("Territories are not neighbors");
        }

        // Verificar que el atacante tenga suficientes ejércitos
        if (attacker.getArmies() <= 1) {
            throw new IllegalArgumentException("Attacking territory must have more than 1 army");
        }

        // Verificar ejércitos atacantes válidos
        int maxAttackingArmies = attacker.getArmies() - 1; // Debe dejar al menos 1
        if (attackDto.getAttackingArmies() < 1 || attackDto.getAttackingArmies() > maxAttackingArmies) {
            throw new IllegalArgumentException(
                    String.format("Invalid attacking armies. Must be between 1 and %d", maxAttackingArmies));
        }
    }

    /**
     * Determina la cantidad de dados que puede usar el atacante.
     */
    private int determineAttackerDice(Territory attacker, int attackingArmies) {
        // En TEG: 1 ejército = 1 dado, máximo 3 dados
        return Math.min(3, attackingArmies);
    }

    /**
     * Determina la cantidad de dados que puede usar el defensor.
     */
    private int determineDefenderDice(Territory defender) {
        // En TEG: Defensor puede usar 1-2 dados según sus ejércitos
        return Math.min(3, defender.getArmies());
    }

    /**
     * Tira la cantidad especificada de dados y los ordena de mayor a menor.
     */
    private List<Integer> rollDice(int diceCount) {
        return IntStream.range(0, diceCount)
                .map(i -> random.nextInt(6) + 1) // Dados de 1-6
                .boxed()
                .sorted(Collections.reverseOrder()) // Ordenar de mayor a menor
                .collect(Collectors.toList());
    }

    /**
     * Resuelve el combate comparando dados según las reglas del TEG.
     */
    private CombatResult resolveCombat(List<Integer> attackerDice, List<Integer> defenderDice) {
        int attackerLosses = 0;
        int defenderLosses = 0;

        // Comparar dados de mayor a menor
        int comparisons = Math.min(attackerDice.size(), defenderDice.size());

        for (int i = 0; i < comparisons; i++) {
            int attackerRoll = attackerDice.get(i);
            int defenderRoll = defenderDice.get(i);

            if (attackerRoll > defenderRoll) {
                // Atacante gana esta comparación
                defenderLosses++;
            } else {
                // Defensor gana (empate favorece al defensor)
                attackerLosses++;
            }
        }

        return new CombatResult(attackerLosses, defenderLosses);
    }

    /**
     * Aplica las pérdidas de ejércitos a los territorios.
     */
    private void applyLosses(Long gameId, AttackDto attackDto, CombatResult result) {
        // Reducir ejércitos del atacante
        if (result.attackerLosses > 0) {
            gameTerritoryService.addArmiesToTerritory(
                    gameId, attackDto.getAttackerCountryId(), -result.attackerLosses);
        }

        // Reducir ejércitos del defensor
        if (result.defenderLosses > 0) {
            gameTerritoryService.addArmiesToTerritory(
                    gameId, attackDto.getDefenderCountryId(), -result.defenderLosses);
        }
    }

    /**
     * Verifica si el territorio fue conquistado y maneja la transferencia.
     */
    private boolean checkAndHandleConquest(Long gameId, AttackDto attackDto, CombatResult result) {
        // Obtener estado actualizado del territorio defensor
        Territory defenderTerritory = gameTerritoryService.getTerritoryByGameAndCountry(
                gameId, attackDto.getDefenderCountryId());

        if (defenderTerritory.getArmies() <= 0) {
            // ¡Territorio conquistado!
            int armiesToMove = attackDto.getAttackingArmies() - result.attackerLosses;

            // Transferir propiedad del territorio
            gameTerritoryService.transferTerritoryOwnership(
                    gameId, attackDto.getDefenderCountryId(), attackDto.getPlayerId(), armiesToMove);

            // Reducir ejércitos del territorio atacante
            gameTerritoryService.addArmiesToTerritory(
                    gameId, attackDto.getAttackerCountryId(), -armiesToMove);

            return true;
        }

        return false;
    }

    /**
     * Construye el DTO de resultado del combate.
     */
    private CombatResultDto buildCombatResultDto(
            Territory attackerTerritory, Territory defenderTerritory,
            List<Integer> attackerDice, List<Integer> defenderDice,
            CombatResult result, boolean territoryConquered) {

        // Obtener estados actualizados de los territorios
        Territory updatedAttacker = gameTerritoryService.getTerritoryByGameAndCountry(
                attackerTerritory.getId(), attackerTerritory.getId());
        Territory updatedDefender = gameTerritoryService.getTerritoryByGameAndCountry(
                defenderTerritory.getId(), defenderTerritory.getId());

        return CombatResultDto.builder()
                .attackerCountryId(attackerTerritory.getId())
                .attackerCountryName(attackerTerritory.getName())
                .defenderCountryId(defenderTerritory.getId())
                .defenderCountryName(defenderTerritory.getName())
                .attackerPlayerName(attackerTerritory.getOwnerName())
                .defenderPlayerName(defenderTerritory.getOwnerName())
                .attackerDice(attackerDice)
                .defenderDice(defenderDice)
                .attackerLosses(result.attackerLosses)
                .defenderLosses(result.defenderLosses)
                .territoryConquered(territoryConquered)
                .attackerRemainingArmies(updatedAttacker != null ? updatedAttacker.getArmies() : 0)
                .defenderRemainingArmies(updatedDefender != null ? updatedDefender.getArmies() : 0)
                .build();
    }

    /**
     * Verifica si dos territorios son vecinos.
     */
    private boolean areTerritoriesNeighbors(Long territoryId1, Long territoryId2) {
        return gameTerritoryService.areTerritoriesNeighbors(territoryId1, territoryId2);
    }

    /**
     * Clase interna para resultado de combate.
     */
    private static class CombatResult {
        final int attackerLosses;
        final int defenderLosses;

        CombatResult(int attackerLosses, int defenderLosses) {
            this.attackerLosses = attackerLosses;
            this.defenderLosses = defenderLosses;
        }
    }

    // === MÉTODOS DE UTILIDAD ===

    /**
     * Obtiene todos los territorios que un jugador puede atacar.
     */
    @Override
    public List<Territory> getAttackableTerritoriesForPlayer(String gameCode, Long playerId) {
        Game game = gameService.findByGameCode(gameCode);
        List<Territory> playerTerritories = gameTerritoryService.getTerritoriesByOwner(game.getId(), playerId);

        return playerTerritories.stream()
                .filter(territory -> territory.getArmies() > 1) // Puede atacar
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los territorios enemigos que un territorio específico puede atacar.
     */
    @Override
    public List<Territory> getTargetsForTerritory(String gameCode, Long territoryId, Long playerId) {
        Game game = gameService.findByGameCode(gameCode);
        List<Territory> neighbors = gameTerritoryService.getNeighborTerritories(game.getId(), territoryId);

        return neighbors.stream()
                .filter(neighbor -> !playerId.equals(neighbor.getOwnerId())) // No es suyo
                .collect(Collectors.toList());
    }
}