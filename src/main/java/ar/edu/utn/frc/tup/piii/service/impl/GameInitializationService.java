package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.entities.*;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.piii.model.*;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.ObjectiveType;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.repository.GameRepository;
import ar.edu.utn.frc.tup.piii.repository.PlayerRepository; // AGREGAR ESTO
import ar.edu.utn.frc.tup.piii.service.interfaces.*;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class GameInitializationService {
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository; // AGREGAR ESTO - USAR DIRECTAMENTE

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

        // 1. Asignar orden de jugadores - CORREGIDO
        assignSeatOrderFixed(gameEntity);

        // 2. Repartir países según reglamento TEG
        distributeCountries(game);

        // 3. Asignar objetivos secretos
        assignObjectives(game);

        // 4. Asegurar que todos los jugadores estén activos
        setAllPlayersActive(gameEntity);

        // 5. Preparar fase inicial de colocación
        prepareInitialPlacement(game);

        // 6. Configurar estado del juego
        setupGameState(gameEntity, game);
    }

    /**
     * METODO CORREGIDO: Asigna orden aleatorio trabajando directamente con entidades
     */
    private void assignSeatOrderFixed(GameEntity gameEntity) {
        // Trabajar directamente con las entidades para evitar problemas de mapeo
        List<PlayerEntity> playerEntities = gameEntity.getPlayers();

        // Filtrar solo jugadores activos/esperando
        List<PlayerEntity> activePlayers = playerEntities.stream()
                .filter(p -> p.getStatus() != PlayerStatus.ELIMINATED)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        // Mezclar aleatoriamente
        Collections.shuffle(activePlayers, random);

        // Asignar orden y estado
        for (int i = 0; i < activePlayers.size(); i++) {
            PlayerEntity player = activePlayers.get(i);
            player.setSeatOrder(i);
            player.setStatus(PlayerStatus.ACTIVE);

            // Asegurar que no hay valores nulos
            if (player.getArmiesToPlace() == null) {
                player.setArmiesToPlace(0);
            }
            if (player.getJoinedAt() == null) {
                player.setJoinedAt(LocalDateTime.now());
            }

            // IMPORTANTE: Asegurar que game_id no se pierda
            player.setGame(gameEntity);
        }

        // Guardar todos los cambios usando el repositorio directamente
        playerRepository.saveAll(activePlayers);
    }

    /**
     * METODO ORIGINAL COMENTADO PARA REFERENCIA
     */
    /*
    private void assignSeatOrder(Game game) {
        List<Player> players = new ArrayList<>(game.getPlayers());
        Collections.shuffle(players, random);

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            // Actualizar el modelo
            player.setSeatOrder(i);
            player.setStatus(PlayerStatus.ACTIVE);
            // PROBLEMA: Aquí se pierde game_id al convertir modelo -> entidad
            playerService.save(player); // <- AQUI ESTA EL PROBLEMA
        }
    }
    */

    /**
     * Valida que el juego puede iniciarse según las reglas.
     */
    private void validateGameCanStart(Game game) {
        if (game.getState() != GameState.WAITING_FOR_PLAYERS) {
            throw new InvalidGameStateException("Cannot start game. Current state: " + game.getState());
        }

        long activePlayerCount = game.getPlayers().stream()
                .filter(p -> p.getStatus() != PlayerStatus.ACTIVE)
                .count();

        if (activePlayerCount < 2) {
            throw new InvalidGameStateException("Minimum 2 players required to start. Current: " + activePlayerCount);
        }
    }

    /**
     * Asegurar que todos los jugadores estén ACTIVE
     */
    private void setAllPlayersActive(GameEntity gameEntity) {

        List<PlayerEntity> allPlayers = gameEntity.getPlayers();

        for (PlayerEntity player : allPlayers) {
            if (player.getStatus() != PlayerStatus.ELIMINATED) {
                player.setStatus(PlayerStatus.ACTIVE);
            }
        }

        playerRepository.saveAll(allPlayers);
    }

    /**
     * Reparte países según el reglamento TEG
     */
    private void distributeCountries(Game game) {
        // Obtener todos los países disponibles a través del servicio de territorios
        List<Territory> allTerritories = gameTerritoryService.getAllAvailableTerritories();
        Collections.shuffle(allTerritories, random);

        List<Player> activePlayers = game.getPlayers().stream()
                .filter(p -> p.getStatus() != PlayerStatus.ELIMINATED)
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
            Collections.shuffle(playersForExtra, random);

            for (int i = 0; i < remainingCountries; i++) {
                assignCountryToPlayer(game, allTerritories.get(countryIndex++), playersForExtra.get(i));
            }
        }
    }

    /**
     * Asigna un país a un jugador a través del servicio de territorios.
     */
    private void assignCountryToPlayer(Game game, Territory territory, Player player) {
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
                .filter(p -> p.getStatus() != PlayerStatus.ELIMINATED)
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
     * Prepara la fase inicial de colocación según el reglamento TEG.
     */
    private void prepareInitialPlacement(Game game) {
        List<Player> activePlayers = game.getPlayers().stream()
                .filter(p -> p.getStatus() != PlayerStatus.ELIMINATED)
                .toList();

        for (Player player : activePlayers) {
            playerService.addArmiesToPlace(player.getId(), 5);
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