package ar.edu.utn.frc.tup.piii.service.impl;
import ar.edu.utn.frc.tup.piii.dtos.bot.AddBotsDto;
import ar.edu.utn.frc.tup.piii.dtos.game.*;
import ar.edu.utn.frc.tup.piii.entities.*;
import ar.edu.utn.frc.tup.piii.exceptions.*;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.mappers.TerritoryMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.User;
import ar.edu.utn.frc.tup.piii.model.enums.*;
import ar.edu.utn.frc.tup.piii.repository.*;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.UserService;
import ar.edu.utn.frc.tup.piii.utils.CodeGenerator;
import ar.edu.utn.frc.tup.piii.utils.ColorManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameServiceImpl implements GameService {

    @Autowired
    private UserService userService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private BotProfileRepository botProfileRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private GameTerritoryRepository gameTerritoryRepository;

    @Autowired
    private CodeGenerator codeGenerator;

    @Autowired
    private ColorManager colorManager;



    private Random random = new Random();
    @Autowired
    private TerritoryMapper territoryMapper;
    @Autowired
    private ObjectiveRepository objectiveRepository;
    @Autowired
    private GameStateServiceImpl gameStateServiceImpl;


    @Override
    public Game findById(Long gameId) {
        return gameRepository.findById(gameId)
                .map(gameMapper::toModel)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));
    }

    @Override
    public Optional<Game> findByIdOptional(Long gameId) {
        return gameRepository.findById(gameId)
                .map(gameMapper::toModel);
    }

    @Override
    public Game findByGameCode(String gameCode) {
        return gameRepository.findByGameCode(gameCode)
                .map(gameMapper::toModel)
                .orElseThrow(() -> new GameNotFoundException("Game not found with code: " + gameCode));
    }

    @Override
    public Game save(Game game) {
        GameEntity entity = gameMapper.toEntity(game);
        GameEntity savedEntity = gameRepository.save(entity);
        return gameMapper.toModel(savedEntity);
    }

    @Override
    public boolean existsById(Long gameId) {
        return gameRepository.existsById(gameId);
    }




    @Override
    @Transactional
    public Game createLobbyWithDefaults(Long hostUserId) {
        UserEntity host = userRepository.findById(hostUserId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con id: " + hostUserId));
        String gameCode = codeGenerator.generateUniqueCode();
        if (gameRepository.existsByGameCode(gameCode)) {
            throw new InvalidGameConfigurationException("El código generado ya existe: " + gameCode);
        }

        GameEntity gameEntity = new GameEntity();
        gameEntity.setGameCode(gameCode);
        gameEntity.setCreatedBy(host);
        gameEntity.setStatus(GameState.WAITING_FOR_PLAYERS);

        gameEntity.setMaxPlayers(6);
        gameEntity.setTurnTimeLimit(120);
        gameEntity.setChatEnabled(true);
        gameEntity.setPactsAllowed(false);

        GameEntity savedGame = gameRepository.save(gameEntity);

        PlayerEntity hostPlayer = new PlayerEntity();
        hostPlayer.setUser(host);
        hostPlayer.setGame(savedGame);
        hostPlayer.setColor(PlayerColor.RED);
        hostPlayer.setStatus(PlayerStatus.WAITING);
        hostPlayer.setSeatOrder(1); // primer asiento
        playerRepository.save(hostPlayer);

        return gameMapper.toModel(savedGame);
    }



    @Override
    @Transactional(readOnly = true)
    public GameResponseDto getGameByCode(String gameCode) {

        GameEntity gameEntity = gameRepository.findByGameCode(gameCode)
                .orElseThrow(() -> new GameNotFoundException("Game not found with code: " + gameCode));
        return gameMapper.toResponseDto(gameEntity);
    }


    @Transactional
    @Override
    public Game joinGame(JoinGameDto dto) {
        GameEntity gameEntity = gameRepository.findByGameCode(dto.getGameCode())
                .orElseThrow(() -> new GameNotFoundException(
                        "Game not found with code: " + dto.getGameCode()));

        if (gameEntity.getStatus() != GameState.WAITING_FOR_PLAYERS) {
            throw new InvalidGameStateException(
                    "Game is not accepting new players. Current state: " + gameEntity.getStatus());
        }
        UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + dto.getUserId()));
        Optional<PlayerEntity> existingPlayerOpt = gameEntity.getPlayers().stream()
                .filter(p -> p.getUser() != null && p.getUser().getId().equals(dto.getUserId()))
                .findFirst();

        if (existingPlayerOpt.isPresent()) {
            PlayerEntity existingPlayer = existingPlayerOpt.get();
            if (existingPlayer.getStatus() == PlayerStatus.ELIMINATED) {
                // Reactivarlo si ya estaba eliminado
                existingPlayer.setStatus(PlayerStatus.WAITING);
                existingPlayer.setEliminatedAt(null);
                playerRepository.save(existingPlayer);

                // Añadimos a la lista en memoria (ya estaba en la lista, pero inactivo)
                // y forzamos flush para sincronizar en BD
                playerRepository.flush();
                return gameMapper.toModel(gameEntity);
            } else {
                throw new InvalidGameStateException("User is already in this game.");
            }
        }

        // Contar jugadores activos
        long activePlayers = gameEntity.getPlayers().stream()
                .filter(p -> p.getStatus() != PlayerStatus.ELIMINATED)
                .count();

        if (activePlayers >= gameEntity.getMaxPlayers()) {
            throw new GameFullException(
                    "Game is full. Max players: " + gameEntity.getMaxPlayers());
        }
        PlayerColor availableColor = colorManager.getAvailableRandomColor(gameEntity);
        if (availableColor == null) {
            throw new ColorNotAvailableException("No colors available");
        }
        PlayerEntity newPlayer = new PlayerEntity();
        newPlayer.setUser(user);
        newPlayer.setGame(gameEntity);
        newPlayer.setColor(availableColor);
        newPlayer.setStatus(PlayerStatus.WAITING);
        int nextSeatOrder = gameEntity.getPlayers().stream()
                .mapToInt(PlayerEntity::getSeatOrder)
                .max()
                .orElse(0) + 1;
        newPlayer.setSeatOrder(nextSeatOrder);
        playerRepository.save(newPlayer);

        // ==== CLAVE: añadir a la lista en memoria ANTES de mapear ====
        gameEntity.getPlayers().add(newPlayer);
        playerRepository.flush(); // opcional, pero asegura que BD y contexto estén alineados
        return gameMapper.toModel(gameEntity);
    }


    @Override
    @Transactional
    public Game addBotsToGame(AddBotsDto dto) {
        GameEntity gameEntity = gameRepository.findByGameCode(dto.getGameCode())
                .orElseThrow(() ->
                        new GameNotFoundException("Game not found with code: " + dto.getGameCode()));

        if (gameEntity.getStatus() != GameState.WAITING_FOR_PLAYERS) {
            throw new InvalidGameStateException("Cannot add bots. Game state: " + gameEntity.getStatus());
        }

        long activePlayers = gameEntity.getPlayers().stream()
                .filter(p -> p.getStatus() != PlayerStatus.ELIMINATED)
                .count();

        // Suponiendo que dto.getNumberOfBots() no sea nulo
        int requestedBots = dto.getNumberOfBots();

        if (activePlayers + requestedBots > gameEntity.getMaxPlayers()) {
            throw new GameFullException("Not enough space. Current active: " + activePlayers +
                    ", Requested bots: " + requestedBots + ", Max: " + gameEntity.getMaxPlayers());
        }

        // Buscar en BD el BotProfileEntity que coincida con nivel y estrategia
        BotProfileEntity botProfile = botProfileRepository
                .findByLevelAndStrategy(dto.getBotLevel(), dto.getBotStrategy())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No bot profile found for level=" + dto.getBotLevel() +
                                " and strategy=" + dto.getBotStrategy()));
        for (int i = 0; i < requestedBots; i++) {
            PlayerColor availableColorBot = colorManager.getAvailableRandomColor(gameEntity);
            if (availableColorBot == null) {
                break;
            }

            int nextSeatOrder = gameEntity.getPlayers().stream()
                    .mapToInt(PlayerEntity::getSeatOrder)
                    .max()
                    .orElse(0) + 1;

            PlayerEntity botPlayer = new PlayerEntity();
            botPlayer.setBotProfile(botProfile);
            botPlayer.setGame(gameEntity);
            botPlayer.setColor(availableColorBot);
            botPlayer.setStatus(PlayerStatus.WAITING);
            botPlayer.setSeatOrder(nextSeatOrder);

            playerRepository.save(botPlayer);
        }
        return gameMapper.toModel(gameEntity);
    }



    @Transactional
    @Override
    public Game startGame(String gameCode) {

        GameEntity gameEntity = gameRepository.findByGameCode(gameCode)
                .orElseThrow(() -> new GameNotFoundException("Game not found with code: " + gameCode));

        //aca validamos el estado y si el juego puede empezar.
        validatedGameCanStart(gameEntity);

        //asigno el orden en el que van a jugar
        assignSeatOrder(gameEntity);

        //reparto paises
        distributeCountries(gameEntity);

        //reparto objetivos secreto no comun
        assignObjective(gameEntity);

        //setea ejcitos para colocar y pasa a fase INITIAL_PLACEMENT
        prepareInitialPlacementPhase(gameEntity);

        //primer turno
        startFirstTurn(gameEntity);


        //TODO: Inicializar territorios y cartas (implementar después)
        // initializeTerritories(savedGame);
        // initializeCards(savedGame);

        //TODO: Activar StateMachine (implementar después)
        // stateMachineService.startGame(savedGame.getId());
        
        //  Guardar cambios
        GameEntity savedGame = gameRepository.save(gameEntity);
        return gameMapper.toModel(savedGame);
    }

    @Transactional
    @Override
    public Game leaveGame(LeaveGameDto dto) {
        GameEntity gameEntity = gameRepository.findByGameCode(dto.getGameCode())
                .orElseThrow(() -> new GameNotFoundException("Game not found with code: " + dto.getGameCode()));

        if (gameEntity.getStatus() != GameState.WAITING_FOR_PLAYERS) {
            throw new InvalidGameStateException("Cannot leave. Game already started.");
        }

        PlayerEntity playerEntity = gameEntity.getPlayers().stream()
                .filter(p -> p.getUser() != null && p.getUser().getId().equals(dto.getUserId()))
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException("Player not found in game."));

        // No permitir que el host se vaya
        if (gameEntity.getCreatedBy().getId().equals(dto.getUserId())) {
            throw new ForbiddenException("Host cannot leave the game.");
        }

        playerEntity.setStatus(PlayerStatus.ELIMINATED);
        playerEntity.setEliminatedAt(LocalDateTime.now());

        playerRepository.save(playerEntity);

        return gameMapper.toModel(gameEntity);
    }


    private void startFirstTurn(GameEntity gameEntity) {
        Game game = findByGameCode(gameEntity.getGameCode());
        //TODO: cambiar el eventType a GAME_STARTED

        // jugador con seatOrder = 0
        List<PlayerEntity> players = gameEntity.getPlayers();
        Optional<PlayerEntity> firstPlayerOpt = players.stream()
                .filter(p -> p.getSeatOrder() == 0)
                .findFirst();

        if (firstPlayerOpt.isEmpty()) {
            throw new IllegalStateException("No player with seatOrder = 0 found");
        }

        PlayerEntity firstPlayer = firstPlayerOpt.get();

        int index = players.indexOf(firstPlayer);
        gameEntity.setCurrentPlayerIndex(index);


        // Paso 5: Enviar evento a StateMachine
        // TODO: stateMachineService.sendEvent(gameEntity.getId(), EventType.GAME_STARTED)??;

        gameRepository.save(gameEntity);
    }


    private void prepareInitialPlacementPhase(GameEntity gameEntity) {
        Game game = gameMapper.toModel(gameEntity);
        gameStateServiceImpl.changeTurnPhase(game, TurnPhase.REINFORCEMENT);
        for (PlayerEntity player : gameEntity.getPlayers()) {
            player.setArmiesToPlace(8);
        }
        // El frontend debera permitir que cada jugador, en orden de seatOrder, coloque primero 5 armies, luego 3.
        //y despues mande todas juntas
    }
    private void assignObjective(GameEntity gameEntity) {
        List<ObjectiveEntity> objectives = objectiveRepository.findByIsCommonFalse();
        Collections.shuffle(objectives);

        List<PlayerEntity> players = gameEntity.getPlayers();
        List<ObjectiveEntity> objetivosUsados = new ArrayList<>();

        for (PlayerEntity player : players) {
            ObjectiveEntity objetivoAsignado = null;

            for (ObjectiveEntity obj : objectives) {
                if (objetivosUsados.contains(obj)) continue;

                if (obj.getType() == ObjectiveType.DESTRUCTION && !isValidDestructionObjective(player, obj, players)) {
                    fallbackToRightPlayerColor(player, obj, players);
                    if (!isValidDestructionObjective(player, obj, players)) continue;
                }

                objetivoAsignado = obj;
                break;
            }

            player.setObjective(objetivoAsignado);
            objetivosUsados.add(objetivoAsignado);
        }
    }


    private boolean isValidDestructionObjective(PlayerEntity player, ObjectiveEntity obj, List<PlayerEntity> players) {
        String targetColorString = obj.getTargetData();
        if (targetColorString == null || targetColorString.isEmpty()) return false;

        PlayerColor targetColor;
        try {
            targetColor = PlayerColor.valueOf(targetColorString.toUpperCase());
        } catch (Exception e) {
            return false;
        }

        if (player.getColor() == targetColor) return false;

        for (PlayerEntity otro : players) {
            if (!otro.getId().equals(player.getId()) && otro.getColor() == targetColor) {
                return true;
            }
        }

        return false;
    }
    private void fallbackToRightPlayerColor(PlayerEntity player, ObjectiveEntity obj, List<PlayerEntity> players) {
        int index = players.indexOf(player);
        int indexDerecha = (index + 1) % players.size();
        PlayerColor colorDerecha = players.get(indexDerecha).getColor();
        obj.setTargetData(colorDerecha.name());
    }

    private void distributeCountries(GameEntity gameEntity) {
        List<CountryEntity> countries = countryRepository.findAll();
        Collections.shuffle(countries);

        List<PlayerEntity> players = gameEntity.getPlayers();
        int playerCount = players.size();

        // Tirada de dados, esto es para que se cumple el reglamento, donde dice que dos jugadores random reciben las cartas sobrantes con una tiraada de dados
        Map<PlayerEntity, Integer> tiradas = new HashMap<>();
        Random random = new Random();

        for (PlayerEntity player : players) {
            tiradas.put(player, random.nextInt(6) + 1);
        }

        // Ordenar los jugadores por mayor numero que le toco en el "dado"
        List<PlayerEntity> jugadoresOrdenados = new ArrayList<>(players);
        jugadoresOrdenados.sort((a, b) -> tiradas.get(b) - tiradas.get(a));

        int totalCountries = countries.size();
        int base = totalCountries / playerCount;
        int sobrantes = totalCountries % playerCount;

        int countryIndex = 0;

        for (PlayerEntity player : players) {
            for (int i = 0; i < base; i++) {
                assignTerritory(gameEntity, countries.get(countryIndex++), player);
            }
        }

        // Reparto de los 2 paises que sobran a los jugadores con mayor suerte
        for (int i = 0; i < sobrantes; i++) {
            PlayerEntity ganadorDelDado = jugadoresOrdenados.get(i);
            assignTerritory(gameEntity, countries.get(countryIndex++), ganadorDelDado);
        }
        //todo: Registrar evento COUNTRIES_DISTRIBUTED en la StateMachine

    }

    private void assignTerritory(GameEntity gameEntity, CountryEntity countryEntity, PlayerEntity player) {
            GameTerritoryEntity territory = new GameTerritoryEntity();
            territory.setGame(gameEntity);
            territory.setCountry(countryEntity);
            territory.setOwner(player);
            territory.setArmies(1);

            gameTerritoryRepository.save(territory);
            player.getTerritories().add(territory);

            //TODO: Registrar evento COUNTRIES_DISTRIBUTED en la StateMachine.

    }

    private void assignSeatOrder(GameEntity gameEntity) {
        /// traigo los jugadores y los mezclo, va a ser aleatorio.
        List<PlayerEntity> players = gameEntity.getPlayers();
        Collections.shuffle(players);
        for(int i = 0; i<players.size();i++){
            players.get(i).setSeatOrder(i);
        }
        playerRepository.saveAll(players);
    }

    private static void validatedGameCanStart(GameEntity gameEntity) {
        if (gameEntity.getStatus() != GameState.WAITING_FOR_PLAYERS) {
            throw new InvalidGameStateException("Cannot start game. Current state: " + gameEntity.getStatus());
        }

        int playerCount = gameEntity.getPlayers().size();
        if (playerCount < 2) {
            throw new InvalidGameStateException("Minimum 2 players required to start. Current: " + playerCount);
        }

        gameEntity.setStatus(GameState.REINFORCEMENT_5);
        gameEntity.setStartedAt(LocalDateTime.now());
        gameEntity.setCurrentTurn(1);
        gameEntity.setCurrentPlayerIndex(0);
        gameEntity.getPlayers().forEach(player ->
                player.setStatus(PlayerStatus.ACTIVE));
    }

    private BotProfileEntity createDefaultBotProfile(BotLevel level, BotStrategy strategy) {
        BotProfileEntity botProfile = new BotProfileEntity();
        botProfile.setBotName("Bot " + level.name());
        botProfile.setLevel(level);
        botProfile.setStrategy(strategy);


        return botProfileRepository.save(botProfile);
    }




    @Transactional
    @Override
    public Game updateGameSettings(String gameCode, UpdateGameSettingsDto dto) {

        GameEntity gameEntity = gameRepository.findForSettings(gameCode)
                .orElseThrow(() -> new GameNotFoundException("Game not found with code: " + gameCode));

        if (gameEntity.getStatus() != GameState.WAITING_FOR_PLAYERS) {
            throw new InvalidGameStateException("Cannot modify settings once game has started");
        }

        if (!gameEntity.getCreatedBy().getId().equals(dto.getRequesterId())) {
            throw new ForbiddenException("Only the host can modify game settings");
        }

        if (dto.getMaxPlayers() != null) {
            if (dto.getMaxPlayers() < 2 || dto.getMaxPlayers() > 6) {
                throw new InvalidGameConfigurationException("Max players must be between 2 and 6");
            }

            int currentPlayerCount = gameEntity.getPlayers().size();
            if (dto.getMaxPlayers() < currentPlayerCount) {
                throw new InvalidGameConfigurationException("Cannot set max players below current player count: " + currentPlayerCount);
            }
        }

        if (dto.getTurnTimeLimit() != null && dto.getTurnTimeLimit() <= 0) {
            throw new InvalidGameConfigurationException("Turn time limit must be greater than 0");
        }

        if (dto.getMaxPlayers() != null) {
            gameEntity.setMaxPlayers(dto.getMaxPlayers());
        }
        if (dto.getTurnTimeLimit() != null) {
            gameEntity.setTurnTimeLimit(dto.getTurnTimeLimit());
        }
        if (dto.getChatEnabled() != null) {
            gameEntity.setChatEnabled(dto.getChatEnabled());
        }
        if (dto.getPactsAllowed() != null) {
            gameEntity.setPactsAllowed(dto.getPactsAllowed());
        }
        GameEntity savedGame = gameRepository.save(gameEntity);
        return gameMapper.toModel(savedGame);
    }

    @Override
    @Transactional
    public Game kickPlayer(KickPlayerDto dto)
            throws GameNotFoundException, PlayerNotFoundException, InvalidGameStateException, ForbiddenException {
        GameEntity gameEntity = gameRepository.findByGameCode(dto.getGameCode())
                .orElseThrow(() ->
                        new GameNotFoundException("Game not found with code: " + dto.getGameCode())
                );

        //permitir kick si el juego está en WAITING_FOR_PLAYERS
        if (gameEntity.getStatus() != GameState.WAITING_FOR_PLAYERS) {
            throw new InvalidGameStateException(
                    "Cannot kick player. Game is not in WAITING_FOR_PLAYERS state. Current: "
                            + gameEntity.getStatus()
            );
        }

        //Encontrar el PlayerEntity por su ID (playerId)
        PlayerEntity playerEntity = playerRepository.findById(dto.getPlayerId())
                .orElseThrow(() ->
                        new PlayerNotFoundException(
                                "Player with id " + dto.getPlayerId() +
                                        " not found at all."
                        )
                );

        //Verificar que ese PlayerEntity pertenezca efectivamente a este GameEntity
        if (!playerEntity.getGame().getGameCode().equals(dto.getGameCode())) {
            throw new PlayerNotFoundException(
                    "Player id " + dto.getPlayerId() +
                            " does not belong to game " + dto.getGameCode()
            );
        }

        //No permitir expulsar al host (siendo usuario humano)
        Long hostId = gameEntity.getCreatedBy().getId();
        Long userIdOfPlayer = playerEntity.getUser() != null
                ? playerEntity.getUser().getId()
                : null;
        if (hostId.equals(userIdOfPlayer)) {
            throw new ForbiddenException("Cannot kick the host of the game");
        }

        //Si es bot, borrarlo fisicamente; si es humano, dar baja logica
        if (playerEntity.getBotProfile() != null) {
            // Removerlo de la coleccion en memoria para que no aparezca al mapear
            gameEntity.getPlayers().remove(playerEntity);
            // Borrar de la BD
            playerRepository.delete(playerEntity);
        } else {
            playerEntity.setStatus(PlayerStatus.ELIMINATED);
            playerEntity.setEliminatedAt(LocalDateTime.now());
            playerRepository.save(playerEntity);
        }

        //Devolver el Game actualizado como modelo
        Game updatedGame = gameMapper.toModel(gameEntity);
        return updatedGame;
    }



    public void prepareInitialPlacementPhase(String gameCode, Long playerId, Map<Long, Integer> armiesByCountry) {
        Game game = findByGameCode(gameCode);
        gameStateServiceImpl.changeTurnPhase(game, TurnPhase.REINFORCEMENT);
        // el juego existe?
        GameEntity gameEntity = gameRepository.findByGameCode(gameCode)
                .orElseThrow(() -> new GameNotFoundException("Game not found with code: " + gameCode));

        // el jugador existe?
        PlayerEntity player = gameEntity.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException("Player not found in game"));

        gameStateServiceImpl.isPlayerTurn(game,playerId);

        // los territorios le pertenecen?
        for (Long countryId : armiesByCountry.keySet()) {
            GameTerritoryEntity territory = gameTerritoryRepository.findByGameAndCountry(gameEntity, countryRepository.getReferenceById(countryId))
                    .orElseThrow(() -> new IllegalArgumentException("Country not found in game: " + countryId));

            if (!territory.getOwner().getId().equals(playerId)) {
                throw new IllegalArgumentException("You don't own country with id: " + countryId);
            }
        }

        // debe poner exactamente 5 o 3 ejércitos
        int totalArmies = armiesByCountry.values().stream().mapToInt(Integer::intValue).sum();
        if (totalArmies != 5 && totalArmies != 3) {
            throw new IllegalArgumentException("You must place exactly 5 or 3 armies");
        }

        if (player.getArmiesToPlace() < totalArmies) {
            throw new IllegalArgumentException("Trying to place more armies than available");
        }

        // actualizo los ejercitos en los territorios
        for (Map.Entry<Long, Integer> entry : armiesByCountry.entrySet()) {
            Long countryId = entry.getKey();
            int armiesToAdd = entry.getValue();

            GameTerritoryEntity territory = gameTerritoryRepository.findByGameAndCountry(gameEntity, countryRepository.getReferenceById(countryId))
                    .orElseThrow(() -> new IllegalArgumentException("Country not found in game"));

            territory.setArmies(territory.getArmies() + armiesToAdd);
            gameTerritoryRepository.save(territory);
        }

        // actualizo ejrcitos restantes
        player.setArmiesToPlace(player.getArmiesToPlace() - totalArmies);
        playerRepository.save(player);

        // verifico si todos terminaron
        boolean allPlaced = gameEntity.getPlayers().stream()
                .allMatch(p -> p.getArmiesToPlace() == 0);

        if (allPlaced) {
            //TODO: a que cambio? que estado?
            //TODO: Una vez que todos los jugadores completaron ambas rondas, enviar
            //evento INITIAL_ARMIES_PLACED a la StateMachine
            //creo que aca deberia cambiar el eventType a REINFORCEMENTS_PLACED
        } else {
            gameStateServiceImpl.nextTurn(game);
        }

        gameRepository.save(gameEntity);
    }

}