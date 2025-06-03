package ar.edu.utn.frc.tup.piii.service.impl;
import ar.edu.utn.frc.tup.piii.dtos.bot.AddBotsDto;
import ar.edu.utn.frc.tup.piii.dtos.game.GameCreationDto;
import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.game.JoinGameDto;
import ar.edu.utn.frc.tup.piii.entities.BotProfileEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.exceptions.*;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.enums.*;
import ar.edu.utn.frc.tup.piii.repository.BotProfileRepository;
import ar.edu.utn.frc.tup.piii.repository.GameRepository;
import ar.edu.utn.frc.tup.piii.repository.PlayerRepository;
import ar.edu.utn.frc.tup.piii.repository.UserRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
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




    @Transactional
    @Override
    public Game createNewGame(GameCreationDto dto) {
        UserEntity creator = userRepository.findById(dto.getCreatedByUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + dto.getCreatedByUserId()));

        if (!creator.getIsActive()) {
            throw new InvalidGameStateException("User is not active");
        }

        if (dto.getMaxPlayers() != null && (dto.getMaxPlayers() < 2 || dto.getMaxPlayers() > 6)) {
            throw new InvalidGameConfigurationException("Max players must be between 2 and 6 for TEG");
        }

        String gameCode = codeGenerator.generateUniqueCode();

        //  Crear entidad de juego
        GameEntity gameEntity = new GameEntity();
        gameEntity.setGameCode(gameCode);
        gameEntity.setCreatedBy(creator);
        gameEntity.setStatus(GameState.WAITING_FOR_PLAYERS);
        gameEntity.setMaxPlayers(dto.getMaxPlayers() != null ? dto.getMaxPlayers() : 6);
        gameEntity.setTurnTimeLimit(dto.getTurnTimeLimit());
        gameEntity.setChatEnabled(dto.getChatEnabled() != null ? dto.getChatEnabled() : true);
        gameEntity.setPactsAllowed(dto.getPactsAllowed() != null ? dto.getPactsAllowed() : false);


        GameEntity savedGame = gameRepository.save(gameEntity);


        PlayerEntity creatorPlayer = new PlayerEntity();
        creatorPlayer.setUser(creator);
        creatorPlayer.setGame(savedGame);
        creatorPlayer.setColor(PlayerColor.RED);
        creatorPlayer.setStatus(PlayerStatus.WAITING);
        creatorPlayer.setSeatOrder(1);

        playerRepository.save(creatorPlayer);

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
}