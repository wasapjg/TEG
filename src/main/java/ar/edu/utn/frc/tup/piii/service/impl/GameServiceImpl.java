package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.game.*;
import ar.edu.utn.frc.tup.piii.dtos.player.PlayerResponseDto;
import ar.edu.utn.frc.tup.piii.model.entity.*;
import ar.edu.utn.frc.tup.piii.model.enums.*;
import ar.edu.utn.frc.tup.piii.repository.*;
import ar.edu.utn.frc.tup.piii.service.interfaces.*;
import ar.edu.utn.frc.tup.piii.utils.CodeGenerator;
import ar.edu.utn.frc.tup.piii.utils.ColorManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepo;
    private final PlayerService playerService;
    private final BotProfileRepository botProfileRepo;
    private final CodeGenerator codeGenerator;
    private final ColorManager colorManager;
    private final UserRepository userRepo;

    private final PlayerRepository playerRepository;

    public GameServiceImpl(GameRepository gameRepo,
                           PlayerService playerService,
                           BotProfileRepository botProfileRepo,
                           CodeGenerator codeGenerator, ColorManager colorManager,
                           PlayerRepository playerRepository, UserRepository userRepo) {
        this.gameRepo = gameRepo;
        this.playerService = playerService;
        this.botProfileRepo = botProfileRepo;
        this.codeGenerator = codeGenerator;
        this.colorManager = colorManager;
        this.playerRepository = playerRepository;
        this.userRepo = userRepo;
    }

    @Override
    public Game save(Game game) {
        return null;
    }

    @Override
    public Optional<Game> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<Game> findAll() {
        return List.of();
    }

    @Override
    public List<Game> findActiveGames() {
        return List.of();
    }

    @Override
    public List<Game> findGamesByPlayer(User user) {
        return List.of();
    }

    @Override
    public void deleteById(Long id) {

    }

    @Transactional
    @Override
    public Game createGame(GameCreationDto requestDto) {
        User creator = userRepo.findById(requestDto.getCreatedByUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Game game = new Game();
        game.setGameCode(codeGenerator.generateUniqueCode());
        game.setCreatedBy(creator);
        game.setStatus(GameStatus.WAITING_FOR_PLAYERS);
        game.setMaxPlayers(requestDto.getMaxPlayers());
        game.setTurnTimeLimit(requestDto.getTurnTimeLimit());
        game.setChatEnabled(requestDto.getChatEnabled());
        game.setPactsAllowed(requestDto.getPactsAllowed());
        game.setCreatedAt(LocalDateTime.now());

        // Guardamos primero el juego para poder usarlo en relaciones
        game = gameRepo.save(game);

        // Crear el Player para el creador del juego
        Player creatorPlayer = new Player();
        creatorPlayer.setUser(creator);
        creatorPlayer.setGame(game);
        creatorPlayer.setStatus(PlayerStatus.WAITING);
        creatorPlayer.setSeatOrder(0); // el creador es el primero
        creatorPlayer.setJoinedAt(LocalDateTime.now());
        creatorPlayer.setColor(assignAvailableColor(game));

        playerRepository.save(creatorPlayer);

        // Agregamos el jugador al juego
        game.getPlayers().add(creatorPlayer);
        return gameRepo.save(game);
    }



//    @Override
//    @Transactional
//    public Game joinGame(String gameCode, User user) {
//        Game game = gameRepo.findByGameCode(gameCode)
//                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
//        if (!game.hasSlot()) throw new IllegalStateException("Game is full");
//        playerService.createHumanPlayer(user, game);
//        return game;
//    }

    @Override
    public Game joinGame(String gameCode, Long userId) {
        Game game = gameRepo.findByGameCode(gameCode)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Player player = new Player();
        player.setUser(user);
        player.setGame(game);
        player.setStatus(PlayerStatus.WAITING);
        player.setSeatOrder(game.getPlayers().size()); // o alguna lógica para el orden
        player.setJoinedAt(LocalDateTime.now());
        player.setColor(assignAvailableColor(game)); // método opcional si tenés colores únicos

        playerRepository.save(player);

        game.getPlayers().add(player);
        return gameRepo.save(game);
    }

    private PlayerColor assignAvailableColor(Game game) {
        List<PlayerColor> allColors = Arrays.asList(PlayerColor.values());

        Set<PlayerColor> usedColors = game.getPlayers().stream()
                .map(Player::getColor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return allColors.stream()
                .filter(color -> !usedColors.contains(color))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No hay colores disponibles"));
    }



    private PlayerColor getNextAvailableColor(Game game) {
        return null;
    }

    @Override
    @Transactional
    public GameResponseDto addBots(String gameCode, int count, BotLevel botLevel, BotStrategy botStrategy) {
        Game game = gameRepo.findByGameCode(gameCode)
                .orElseThrow(() -> new IllegalStateException("Game not found"));

        int currentPlayers = game.getPlayers().size();
        if (currentPlayers + count > game.getMaxPlayers()) {
            throw new IllegalStateException("Not enough space to add " + count + " bots");
        }

        BotProfile botProfile = BotProfile.create(botLevel, botStrategy);
        botProfile = botProfileRepo.save(botProfile);

        List<PlayerColor> availableColors = getAvailableColors(game);

        if (availableColors.size() < count) {
            throw new IllegalStateException("Not enough available colors for bots");
        }

        for (int i = 0; i < count; i++) {
            Player botPlayer = new Player();
            botPlayer.setGame(game);
            botPlayer.setBotProfile(botProfile);
            botPlayer.setUser(null);
            botPlayer.setStatus(PlayerStatus.WAITING);
            botPlayer.setColor(availableColors.get(i));
            botPlayer.setSeatOrder(game.getPlayers().size() + 1 + i);
            playerRepository.save(botPlayer);
            game.getPlayers().add(botPlayer);
        }

        game = gameRepo.save(game);

        return mapToGameResponseDto(game);
    }

    private GameResponseDto mapToGameResponseDto(Game game) {
        return GameResponseDto.builder()
                .id(game.getId())
                .gameCode(game.getGameCode())
                .createdByUsername(game.getCreatedBy() != null ? game.getCreatedBy().getUsername() : null)
                .status(game.getStatus())
                .currentPhase(game.getCurrentPhase())
                .currentTurn(game.getCurrentTurn())
                .currentPlayerIndex(game.getCurrentPlayerIndex())
                .maxPlayers(game.getMaxPlayers())
                .turnTimeLimit(game.getTurnTimeLimit())
                .chatEnabled(game.getChatEnabled())
                .pactsAllowed(game.getPactsAllowed())
                .createdAt(game.getCreatedAt())
                .startedAt(game.getStartedAt())
                .finishedAt(game.getFinishedAt())
                .players(game.getPlayers().stream()
                        .map(PlayerResponseDto::fromEntity) // Este método debe existir en tu DTO
                        .toList())
                .currentPlayerName(game.getCurrentPlayer() != null ?
                        (game.getCurrentPlayer().getUser() != null ?
                                game.getCurrentPlayer().getUser().getUsername() :
                                game.getCurrentPlayer().getBotProfile().getBotName())
                        : null)
                .build();
    }


    // Método privado para obtener colores disponibles
    private List<PlayerColor> getAvailableColors(Game game) {
        List<PlayerColor> allColors = List.of(PlayerColor.values());
        List<PlayerColor> usedColors = game.getPlayers().stream()
                .map(Player::getColor)
                .filter(color -> color != null)
                .collect(Collectors.toList());

        return allColors.stream()
                .filter(color -> !usedColors.contains(color))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Game kickPlayer(String gameCode, Long playerId) {
        Game game = gameRepo.findByGameCode(gameCode)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        playerService.deleteById(playerId);
        return game;
    }

    @Override
    public boolean hasSlot(Long gameId) {
        return false;
    }

    @Override
    public void setGameOpen(Long gameId, boolean open) {

    }

    @Override
    public void startGame(Long gameId) {

    }

    @Override
    public void endGame(Long gameId) {

    }

    @Override
    public void nextTurn(Long gameId) {

    }

    @Override
    public void nextPhase(Long gameId) {

    }

    @Override
    public CombatResult performAttack(Long gameId, AttackDto attackDto) {
        return null;
    }

    @Override
    public void performReinforcement(Long gameId, ReinforcementDto reinforcementDto) {

    }

    @Override
    public void performFortify(Long gameId, FortifyDto fortifyDto) {

    }

    @Override
    public void tradeCards(Long gameId, Long playerId, List<Card> cards) {

    }

    @Override
    public boolean isGameOver(Long gameId) {
        return false;
    }

    @Override
    public Player getWinner(Long gameId) {
        return null;
    }

    @Override
    public Player getCurrentPlayer(Long gameId) {
        return null;
    }

    @Override
    public GamePhase getCurrentPhase(Long gameId) {
        return null;
    }

    @Override
    public int getCurrentTurn(Long gameId) {
        return 0;
    }

    @Override
    public void saveGameSnapshot(Long gameId) {

    }

    @Override
    public void loadGameSnapshot(Long gameId, Long snapshotId) {

    }

    @Override
    public void pauseGame(Long gameId) {

    }

    @Override
    public void resumeGame(Long gameId) {

    }

    @Override
    public boolean canStartGame(Long gameId) {
        return false;
    }

    @Override
    public boolean isValidAttack(Long gameId, Country from, Country to, Long playerId) {
        return false;
    }

    @Override
    public boolean isValidReinforcement(Long gameId, Map<Country, Integer> reinforcements, Long playerId) {
        return false;
    }

    @Override
    public boolean isValidFortify(Long gameId, Country from, Country to, int armies, Long playerId) {
        return false;
    }

    // stub implementations for other GameService methods...
}