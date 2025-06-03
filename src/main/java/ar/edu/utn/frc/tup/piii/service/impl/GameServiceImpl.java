package ar.edu.utn.frc.tup.piii.service.impl;
import ar.edu.utn.frc.tup.piii.dtos.bot.AddBotsDto;
import ar.edu.utn.frc.tup.piii.dtos.game.*;
import ar.edu.utn.frc.tup.piii.entities.BotProfileEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.exceptions.*;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.User;
import ar.edu.utn.frc.tup.piii.model.enums.*;
import ar.edu.utn.frc.tup.piii.repository.BotProfileRepository;
import ar.edu.utn.frc.tup.piii.repository.GameRepository;
import ar.edu.utn.frc.tup.piii.repository.PlayerRepository;
import ar.edu.utn.frc.tup.piii.repository.UserRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.UserService;
import ar.edu.utn.frc.tup.piii.utils.CodeGenerator;
import ar.edu.utn.frc.tup.piii.utils.ColorManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

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
    private CodeGenerator codeGenerator;

    @Autowired
    private ColorManager colorManager;



    private Random random = new Random();


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
        // 1) Busco al usuario que será host
        UserEntity host = userRepository.findById(hostUserId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con id: " + hostUserId));

        // 2) Genero un código único
        String gameCode = codeGenerator.generateUniqueCode(); // ej. "AA56BJZZ89"
        // (Asegurate de que no exista en BD)
        if (gameRepository.existsByGameCode(gameCode)) {
            throw new InvalidGameConfigurationException("El código generado ya existe: " + gameCode);
        }

        // 3) Armo el GameEntity con valores por defecto
        GameEntity gameEntity = new GameEntity();
        gameEntity.setGameCode(gameCode);
        gameEntity.setCreatedBy(host);
        gameEntity.setStatus(GameState.WAITING_FOR_PLAYERS);

        // Valores por defecto:
        gameEntity.setMaxPlayers(6);
        gameEntity.setTurnTimeLimit(120); // 120 segundos = 2 minutos
        gameEntity.setChatEnabled(true);
        gameEntity.setPactsAllowed(false);

        // Otras columnas (por ej. createdAt) se llenan automáticamente si las tenés con @CreationTimestamp
        GameEntity savedGame = gameRepository.save(gameEntity);

        // 4) Crear PlayerEntity del host con color = RED (o como lo tengan definido)
        PlayerEntity hostPlayer = new PlayerEntity();
        hostPlayer.setUser(host);
        hostPlayer.setGame(savedGame);
        hostPlayer.setColor(PlayerColor.RED);
        hostPlayer.setStatus(PlayerStatus.WAITING);
        hostPlayer.setSeatOrder(1); // primer asiento
        playerRepository.save(hostPlayer);

        // 5) Mappear a modelo (o a DTO más adelante). Si tu service retorna un modelo "Game"
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
                .orElseThrow(() -> new GameNotFoundException("Game not found with code: " + dto.getGameCode()));
        if (gameEntity.getStatus() != GameState.WAITING_FOR_PLAYERS) {
            throw new InvalidGameStateException("Game is not accepting new players. Current state: " + gameEntity.getStatus());
        }
        UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + dto.getUserId()));
        if (!user.getIsActive()) {
            throw new InvalidGameStateException("User is not active");
        }
        boolean alreadyJoined = gameEntity.getPlayers().stream()
                .anyMatch(p -> p.getUser() != null && p.getUser().getId().equals(dto.getUserId()));

        if (alreadyJoined) {
            throw new InvalidGameStateException("User is already in this game");
        }

        int currentPlayers = gameEntity.getPlayers().size();
        if (currentPlayers >= gameEntity.getMaxPlayers()) {
            throw new GameFullException("Game is full. Max players: " + gameEntity.getMaxPlayers());
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
        newPlayer.setSeatOrder(currentPlayers + 1);

        playerRepository.save(newPlayer);

        return gameMapper.toModel(gameEntity);
    }


    @Transactional
    @Override
    public Game addBotsToGame(AddBotsDto dto) {
        GameEntity gameEntity = gameRepository.findByGameCode(dto.getGameCode())
                .orElseThrow(() -> new GameNotFoundException("Game not found with code: " + dto.getGameCode()));
        if (gameEntity.getStatus() != GameState.WAITING_FOR_PLAYERS) {
            throw new InvalidGameStateException("Cannot add bots. Game state: " + gameEntity.getStatus());
        }
        int currentPlayers = gameEntity.getPlayers().size();
        int requestedBots = 1;

        if (currentPlayers + requestedBots > gameEntity.getMaxPlayers()) {
            throw new GameFullException("Not enough space. Current: " + currentPlayers +
                    ", Requested bots: " + requestedBots + ", Max: " + gameEntity.getMaxPlayers());
        }

        BotProfileEntity botProfile = botProfileRepository.findByLevelAndStrategy(
                        dto.getBotLevel(), dto.getBotStrategy())
                .orElse(createDefaultBotProfile(dto.getBotLevel(), dto.getBotStrategy()));
        for (int i = 0; i < requestedBots; i++) {
            PlayerColor availableColorBot = colorManager.getAvailableRandomColor(gameEntity);
            if (availableColorBot == null) {
                break;
            }

            PlayerEntity botPlayer = new PlayerEntity();
            botPlayer.setBotProfile(botProfile);
            botPlayer.setGame(gameEntity);
            botPlayer.setColor(availableColorBot);
            botPlayer.setStatus(PlayerStatus.WAITING);
            botPlayer.setSeatOrder(currentPlayers + i + 1);

            playerRepository.save(botPlayer);
        }

        return gameMapper.toModel(gameEntity);
    }


    @Transactional
    @Override
    public Game startGame(String gameCode) {

        GameEntity gameEntity = gameRepository.findByGameCode(gameCode)
                .orElseThrow(() -> new GameNotFoundException("Game not found with code: " + gameCode));


        if (gameEntity.getStatus() != GameState.WAITING_FOR_PLAYERS) {
            throw new InvalidGameStateException("Cannot start game. Current state: " + gameEntity.getStatus());
        }

        int playerCount = gameEntity.getPlayers().size();
        if (playerCount < 2) {
            throw new InvalidGameStateException("Minimum 2 players required to start. Current: " + playerCount);
        }

        gameEntity.setStatus(GameState.IN_PROGRESS);
        gameEntity.setStartedAt(java.time.LocalDateTime.now());
        gameEntity.setCurrentTurn(1);
        gameEntity.setCurrentPlayerIndex(0);
        gameEntity.getPlayers().forEach(player ->
                player.setStatus(PlayerStatus.ACTIVE));

        //  Guardar cambios
        GameEntity savedGame = gameRepository.save(gameEntity);

        //TODO: Inicializar territorios y cartas (implementar después)
        // initializeTerritories(savedGame);
        // initializeCards(savedGame);

        //TODO: Activar StateMachine (implementar después)
        // stateMachineService.startGame(savedGame.getId());

        return gameMapper.toModel(savedGame);
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

        // CAMBIO: Usar findForSettings en lugar de findByGameCode
        GameEntity gameEntity = gameRepository.findForSettings(gameCode)
                .orElseThrow(() -> new GameNotFoundException("Game not found with code: " + gameCode));

        // Validar que el juego esté en estado correcto
        if (gameEntity.getStatus() != GameState.WAITING_FOR_PLAYERS) {
            throw new InvalidGameStateException("Cannot modify settings once game has started");
        }

        // Validar que el usuario sea el host
        if (!gameEntity.getCreatedBy().getId().equals(dto.getRequesterId())) {
            throw new ForbiddenException("Only the host can modify game settings");
        }

        // Validar maxPlayers si viene en el DTO
        if (dto.getMaxPlayers() != null) {
            if (dto.getMaxPlayers() < 2 || dto.getMaxPlayers() > 6) {
                throw new InvalidGameConfigurationException("Max players must be between 2 and 6");
            }

            // Validar que no sea menor al número actual de jugadores
            int currentPlayerCount = gameEntity.getPlayers().size();
            if (dto.getMaxPlayers() < currentPlayerCount) {
                throw new InvalidGameConfigurationException("Cannot set max players below current player count: " + currentPlayerCount);
            }
        }

        // Validar turnTimeLimit si viene en el DTO
        if (dto.getTurnTimeLimit() != null && dto.getTurnTimeLimit() <= 0) {
            throw new InvalidGameConfigurationException("Turn time limit must be greater than 0");
        }

        // Actualizar solo los campos que vienen en el DTO
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

        // Guardar cambios
        GameEntity savedGame = gameRepository.save(gameEntity);

        // Convertir a modelo y retornar
        return gameMapper.toModel(savedGame);
    }
}