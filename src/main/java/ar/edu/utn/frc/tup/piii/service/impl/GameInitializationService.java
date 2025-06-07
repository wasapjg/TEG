package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.entities.*;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.piii.model.*;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.ObjectiveType;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.repository.GameRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.*;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio especializado en la inicialización de partidas TEG.
 * Maneja el reparto de países, asignación de objetivos y preparación de la fase inicial.
 * Solo usa GameRepository directamente y se comunica con otros servicios a través de sus interfaces.
 */
@Service
public class GameInitializationService {
    @Autowired
    private GameRepository gameRepository;

    // Servicios para comunicación entre capas
    @Autowired
    private PlayerService playerService;

    @Autowired
    private ObjectiveService objectiveService;

    @Autowired
    private GameTerritoryService gameTerritoryService;

    @Autowired
    private GameMapper gameMapper;

    private final Random random = new Random();

    /**
     * Inicializa una partida completa: reparto de países, objetivos y preparación inicial.
     */
    @Transactional
    public void initializeGame(GameEntity gameEntity) {
        // Convertir a modelo para trabajar con servicios
        Game game = gameMapper.toModel(gameEntity);

        validateGameCanStart(game);

        // 1. Asignar orden de jugadores
        assignSeatOrder(game);

        // 2. Repartir países según reglamento TEG
        distributeCountries(game);

        // 3. Asignar objetivos secretos
        assignObjectives(game);

        // 4. Preparar fase inicial de colocación
        prepareInitialPlacement(game);

        // 5. Configurar estado del juego
        setupGameState(gameEntity, game);
    }

    /**
     * Valida que el juego puede iniciarse según las reglas.
     */
    private void validateGameCanStart(Game game) {
        if (game.getState() != GameState.WAITING_FOR_PLAYERS) {
            throw new InvalidGameStateException("Cannot start game. Current state: " + game.getState());
        }

        long activePlayerCount = game.getPlayers().stream()
                .filter(p -> p.getStatus() != PlayerStatus.ELIMINATED)
                .count();

        if (activePlayerCount < 2) {
            throw new InvalidGameStateException("Minimum 2 players required to start. Current: " + activePlayerCount);
        }
    }

    /**
     * Asigna orden aleatorio a los jugadores (reglamento TEG).
     */
    private void assignSeatOrder(Game game) {
        List<Player> players = new ArrayList<>(game.getPlayers());
        Collections.shuffle(players, random);

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            // Actualizar el modelo
            player.setSeatOrder(i);
            player.setStatus(PlayerStatus.ACTIVE);
            // Guardar a través del servicio
            playerService.save(player);
        }
    }

    /**
     * Reparte países según el reglamento TEG:
     * - Se reparten todos los países entre jugadores
     * - Los países sobrantes se asignan por tirada de dados
     */
    private void distributeCountries(Game game) {
        // Obtener todos los países disponibles a través del servicio de territorios
        List<Territory> allTerritories = gameTerritoryService.getAllAvailableTerritories();
        Collections.shuffle(allTerritories, random);

        List<Player> activePlayers = game.getPlayers().stream()
                .filter(p -> p.getStatus() == PlayerStatus.ACTIVE)
                .sorted(Comparator.comparing(Player::getSeatOrder))
                .toList();

        int playerCount = activePlayers.size();
        int totalCountries = allTerritories.size();
        int baseCountries = totalCountries / playerCount;
        int remainingCountries = totalCountries % playerCount;

        // Reparto base de países
        int countryIndex = 0;
        for (Player player : activePlayers) {
            for (int i = 0; i < baseCountries; i++) {
                assignCountryToPlayer(game, allTerritories.get(countryIndex++), player);
            }
        }

        // Reparto de países sobrantes por "tirada de dados"
        if (remainingCountries > 0) {
            List<Player> playersForExtra = new ArrayList<>(activePlayers);
            Collections.shuffle(playersForExtra, random); // Simula tirada de dados

            for (int i = 0; i < remainingCountries; i++) {
                assignCountryToPlayer(game, allTerritories.get(countryIndex++), playersForExtra.get(i));
            }
        }
    }

    /**
     * Asigna un país a un jugador a través del servicio de territorios.
     */
    private void assignCountryToPlayer(Game game, Territory territory, Player player) {
        // Usar el servicio de territorios para asignar
        gameTerritoryService.assignTerritoryToPlayer(game.getId(), territory.getId(), player.getId(), 1);
    }

    /**
     * Asigna objetivos secretos a los jugadores según el reglamento TEG.
     */
    private void assignObjectives(Game game) {
        List<Objective> availableObjectives = objectiveService.findByType(ObjectiveType.OCCUPATION);
        availableObjectives.addAll(objectiveService.findByType(ObjectiveType.DESTRUCTION));
        Collections.shuffle(availableObjectives, random);

        List<Player> activePlayers = game.getPlayers().stream()
                .filter(p -> p.getStatus() == PlayerStatus.ACTIVE)
                .sorted(Comparator.comparing(Player::getSeatOrder))
                .toList();

        for (int i = 0; i < activePlayers.size(); i++) {
            Player player = activePlayers.get(i);
            Objective objective = availableObjectives.get(i % availableObjectives.size());

            // Manejo especial para objetivos de destrucción
            if (objective.getType() == ObjectiveType.DESTRUCTION) {
                objective = handleDestructionObjective(objective, player, activePlayers);
            }

            // Asignar objetivo a través del servicio
            playerService.assignObjective(player.getId(), objective);
        }
    }

    /**
     * Maneja la asignación de objetivos de destrucción según el reglamento.
     * Si el objetivo no es válido, lo cambia por "destruir al jugador de la derecha".
     */
    private Objective handleDestructionObjective(Objective objective, Player player, List<Player> players) {
        PlayerColor targetColor = objective.getTargetColor();

        if (targetColor == null || player.getColor() == targetColor || !colorExistsInGame(targetColor, players)) {
            // Cambiar al jugador de la derecha
            Player rightPlayer = getRightPlayer(player, players);
            objective.setTargetColor(rightPlayer.getColor());
        }

        return objective;
    }

    /**
     * Verifica si un color existe en la partida.
     */
    private boolean colorExistsInGame(PlayerColor color, List<Player> players) {
        return players.stream().anyMatch(p -> p.getColor() == color);
    }

    /**
     * Obtiene el jugador a la derecha según el orden de asientos.
     */
    private Player getRightPlayer(Player player, List<Player> players) {
        int currentIndex = players.indexOf(player);
        int rightIndex = (currentIndex + 1) % players.size();
        return players.get(rightIndex);
    }

    /**
     * Prepara la fase inicial de colocación según el reglamento TEG:
     * - Primera ronda: 5 ejércitos por jugador
     * - Segunda ronda: 3 ejércitos por jugador
     */
    private void prepareInitialPlacement(Game game) {
        List<Player> activePlayers = game.getPlayers().stream()
                .filter(p -> p.getStatus() == PlayerStatus.ACTIVE)
                .toList();

        // Cada jugador debe colocar 8 ejércitos en total (5 + 3)
        for (Player player : activePlayers) {
            playerService.addArmiesToPlace(player.getId(), 5); // Empezamos con la primera ronda de 5
        }
    }

    /**
     * Configura el estado inicial del juego.
     */
    private void setupGameState(GameEntity gameEntity, Game game) {
        gameEntity.setStatus(GameState.REINFORCEMENT_5); // Primera fase de colocación
        gameEntity.setStartedAt(LocalDateTime.now());
        gameEntity.setCurrentTurn(1);
        gameEntity.setCurrentPlayerIndex(0); // Empieza el primer jugador (seatOrder = 0)

        // Guardar los cambios
        gameRepository.save(gameEntity);
    }
}
