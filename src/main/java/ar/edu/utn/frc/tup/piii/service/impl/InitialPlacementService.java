package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.piii.exceptions.PlayerNotFoundException;
import ar.edu.utn.frc.tup.piii.model.*;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.service.interfaces.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Servicio para manejar la fase de colocación inicial de ejércitos.
 * Solo se comunica con otros servicios, no usa repositorios directamente.
 */
@Service
public class InitialPlacementService {

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private GameTerritoryService gameTerritoryService;

    @Autowired
    private GameStateService gameStateService;

    /**
     * Procesa la colocación de ejércitos en la fase inicial.
     */
    @Transactional
    public void placeInitialArmies(String gameCode, Long playerId, Map<Long, Integer> armiesByCountry) {
        // Obtener juego a través del servicio
        Game game = gameService.findByGameCode(gameCode);

        // Obtener jugador a través del servicio
        Player player = playerService.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + playerId));

        // Validar la colocación
        validatePlacement(game, player, armiesByCountry);

        // Colocar ejércitos en territorios
        placeArmiesOnTerritories(game.getId(), armiesByCountry);

        // Actualizar ejércitos del jugador
        int totalArmies = armiesByCountry.values().stream().mapToInt(Integer::intValue).sum();
        playerService.removeArmiesToPlace(playerId, totalArmies);

        // Verificar si debe avanzar el turno
        checkAndAdvanceTurn(game);
    }

    /**
     * Valida que la colocación sea correcta.
     */
    private void validatePlacement(Game game, Player player, Map<Long, Integer> armiesByCountry) {
        // Verificar fase del juego
        if (!isInitialPhase(game.getState())) {
            throw new InvalidGameStateException("Game is not in initial placement phase");
        }

        // Verificar turno del jugador
        if (!gameStateService.isPlayerTurn(game, player.getId())) {
            throw new InvalidGameStateException("It's not player's turn");
        }

        // Verificar cantidad de ejércitos
        int totalArmies = armiesByCountry.values().stream().mapToInt(Integer::intValue).sum();
        int expectedArmies = getExpectedArmiesForPhase(game.getState());
        int playerArmies = playerService.getArmiesToPlace(player.getId());

        if (totalArmies != expectedArmies) {
            throw new IllegalArgumentException(
                    String.format("Must place exactly %d armies, got %d", expectedArmies, totalArmies));
        }

        if (totalArmies > playerArmies) {
            throw new IllegalArgumentException(
                    String.format("Player only has %d armies to place", playerArmies));
        }

        // Verificar que los territorios pertenezcan al jugador
        validateTerritoryOwnership(game.getId(), player.getId(), armiesByCountry.keySet());
    }

    /**
     * Verifica si el juego está en fase inicial.
     */
    private boolean isInitialPhase(GameState state) {
        return state == GameState.REINFORCEMENT_5 || state == GameState.REINFORCEMENT_3;
    }

    /**
     * Obtiene la cantidad esperada de ejércitos según la fase.
     */
    private int getExpectedArmiesForPhase(GameState state) {
        return switch (state) {
            case REINFORCEMENT_5 -> 5;
            case REINFORCEMENT_3 -> 3;
            default -> throw new InvalidGameStateException("Invalid phase: " + state);
        };
    }

    /**
     * Valida que los territorios pertenezcan al jugador.
     */
    private void validateTerritoryOwnership(Long gameId, Long playerId, Iterable<Long> countryIds) {
        for (Long countryId : countryIds) {
            Territory territory = gameTerritoryService.getTerritoryByGameAndCountry(gameId, countryId);

            if (territory == null) {
                throw new IllegalArgumentException("Territory not found: " + countryId);
            }

            if (!playerId.equals(territory.getOwnerId())) {
                throw new IllegalArgumentException("Player doesn't own territory: " + countryId);
            }
        }
    }

    /**
     * Coloca los ejércitos en los territorios.
     */
    private void placeArmiesOnTerritories(Long gameId, Map<Long, Integer> armiesByCountry) {
        for (Map.Entry<Long, Integer> entry : armiesByCountry.entrySet()) {
            Long countryId = entry.getKey();
            Integer armies = entry.getValue();

            gameTerritoryService.addArmiesToTerritory(gameId, countryId, armies);
        }
    }

    /**
     * Verifica si debe avanzar el turno o la fase.
     */
    private void checkAndAdvanceTurn(Game game) {
        // Verificar si todos completaron la ronda actual
        if (allPlayersCompletedRound(game)) {
            advancePhase(game);
        } else {
            // Avanzar al siguiente jugador
            gameStateService.nextTurn(game);
        }

        // Guardar cambios
        gameService.save(game);
    }

    /**
     * Verifica si todos los jugadores completaron la ronda.
     */
    private boolean allPlayersCompletedRound(Game game) {
        return game.getPlayers().stream()
                .filter(p -> p.getStatus() == PlayerStatus.ACTIVE)
                .allMatch(p -> playerService.getArmiesToPlace(p.getId()) == 0);
    }

    /**
     * Avanza a la siguiente fase.
     */
    private void advancePhase(Game game) {
        switch (game.getState()) {
            case REINFORCEMENT_5 -> {
                gameStateService.changeGameState(game, GameState.REINFORCEMENT_3);
                prepareSecondRound(game);
            }
            case REINFORCEMENT_3 -> {
                gameStateService.changeGameState(game, GameState.HOSTILITY_ONLY);
                resetForNextPhase(game);
            }
        }
    }

    /**
     * Prepara la segunda ronda (3 ejércitos).
     */
    private void prepareSecondRound(Game game) {
        for (Player player : game.getPlayers()) {
            if (player.getStatus() == PlayerStatus.ACTIVE) {
                playerService.addArmiesToPlace(player.getId(), 3);
            }
        }
        game.setCurrentPlayerIndex(0);
    }

    /**
     * Resetea para la siguiente fase.
     */
    private void resetForNextPhase(Game game) {
        game.setCurrentPlayerIndex(0);
        game.setCurrentTurn(game.getCurrentTurn() + 1);
    }

    /**
     * Obtiene el estado actual de la colocación.
     */
    public InitialPlacementStatus getPlacementStatus(String gameCode) {
        Game game = gameService.findByGameCode(gameCode);

        if (!isInitialPhase(game.getState())) {
            return new InitialPlacementStatus(
                    false,
                    "Game is not in initial placement phase",
                    null,
                    0
            );
        }

        Player currentPlayer = getCurrentPlayer(game);
        if (currentPlayer == null) {
            return new InitialPlacementStatus(
                    false,
                    "No current player found",
                    null,
                    0
            );
        }

        return new InitialPlacementStatus(
                true,
                "Initial placement in progress",
                currentPlayer.getId(),
                getExpectedArmiesForPhase(game.getState())
        );
    }

    /**
     * Obtiene el jugador actual.
     */
    private Player getCurrentPlayer(Game game) {
        List<Player> activePlayers = game.getPlayers().stream()
                .filter(p -> p.getStatus() == PlayerStatus.ACTIVE)
                .sorted((p1, p2) -> Integer.compare(p1.getSeatOrder(), p2.getSeatOrder()))
                .toList();

        if (activePlayers.isEmpty() || game.getCurrentPlayerIndex() >= activePlayers.size()) {
            return null;
        }

        return activePlayers.get(game.getCurrentPlayerIndex());
    }

    /**
     * Obtiene información específica de un jugador.
     */
    public PlayerInitialStatus getPlayerStatus(String gameCode, Long playerId) {
        Game game = gameService.findByGameCode(gameCode);
        Player player = playerService.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found"));

        boolean isPlayerTurn = gameStateService.isPlayerTurn(game, playerId);
        int armiesToPlace = playerService.getArmiesToPlace(playerId);
        List<Territory> territories = gameTerritoryService.getTerritoriesByOwner(game.getId(), playerId);

        return new PlayerInitialStatus(
                playerId,
                player.getDisplayName(),
                isPlayerTurn,
                armiesToPlace,
                game.getState(),
                isInitialPhase(game.getState()) ? getExpectedArmiesForPhase(game.getState()) : 0,
                territories.stream().map(Territory::getId).toList(),
                isPlayerTurn && armiesToPlace > 0,
                isPlayerTurn ? "Your turn to place armies" : "Waiting for other players"
        );
    }


    @Data
    @AllArgsConstructor
    public static class InitialPlacementStatus {
        private final boolean isActive;
        private final String message;
        private final Long currentPlayerId;
        private final int expectedArmies;

    }
    @Data
    @AllArgsConstructor
    public static class PlayerInitialStatus {
        private final Long playerId;
        private final String playerName;
        private final boolean isPlayerTurn;
        private final int armiesToPlace;
        private final GameState currentPhase;
        private final int expectedArmiesThisRound;
        private final List<Long> ownedTerritoryIds;
        private final boolean canPlaceArmies;
        private final String message;
    }
}