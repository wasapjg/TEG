package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.entities.BotProfileEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import ar.edu.utn.frc.tup.piii.service.interfaces.BotService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameStateService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.web.mappings.MappingsEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador unificado para gestión de bots.
 * Incluye ejecución de turnos, consulta de estados y gestión de perfiles de bots.
 */
@RestController
@RequestMapping("/api/bots")
@Tag(name = "Bot Management", description = "Gestión completa de bots: turnos, estados y perfiles")
@Slf4j
public class BotController {

    private final BotService botService;
    private final GameService gameService;
    private final PlayerService playerService;
    private final GameStateService gameStateService;
    private final GameMapper gameMapper;
    private final PlayerMapper playerMapper;
    private final MappingsEndpoint mappingsEndpoint;

    @Autowired
    public BotController(BotService botService, GameService gameService, PlayerService playerService,
                         GameStateService gameStateService, GameMapper gameMapper, PlayerMapper playerMapper,
                         MappingsEndpoint mappingsEndpoint) {
        this.botService = botService;
        this.gameService = gameService;
        this.playerService = playerService;
        this.gameStateService = gameStateService;
        this.gameMapper = gameMapper;
        this.playerMapper = playerMapper;
        this.mappingsEndpoint = mappingsEndpoint;
    }

    // ===== ENDPOINTS RELACIONADOS CON JUEGOS =====

    /**
     * Ejecuta el turno completo de un bot (Refuerzo -> Ataque -> Fortificación).
     * Los bots siempre ejecutan todas las fases automáticamente.
     *
     * @param gameCode Código del juego
     * @param botId ID del bot que debe ejecutar su turno
     * @return Estado actualizado del juego después del turno del bot
     */
    @PostMapping("/games/{gameCode}/{botId}/execute-turn")
    @Operation(summary = "Ejecutar turno completo del bot",
            description = "El bot ejecuta automáticamente todas las fases: refuerzo, ataque y fortificación")
    public ResponseEntity<GameResponseDto> executeBotTurn(
            @PathVariable String gameCode,
            @PathVariable Long botId) {

        log.info("Executing complete bot turn for bot {} in game {}", botId, gameCode);

        try {
            // Validar que el bot existe y es efectivamente un bot
            Optional<Player> botPlayerOpt = playerService.findById(botId);
            if (botPlayerOpt.isEmpty() || !botPlayerOpt.get().getIsBot()) {
                log.warn("Bot not found or not a bot with ID: {}", botId);
                return ResponseEntity.badRequest().build();
            }

            Player botPlayer = botPlayerOpt.get();
            Game game = gameService.findByGameCode(gameCode);

            // Validar que es el turno del bot
            if (!gameStateService.isPlayerTurn(game, botId)) {
                log.warn("It's not bot's turn. Bot: {}, Current player: {}", botId, game.getCurrentPlayerIndex());
                return ResponseEntity.badRequest().build();
            }

            // Validar que el juego está en estado PLAYING
            if (!gameStateService.canPerformAction(game, "bot_turn")) {
                log.warn("Game not in valid state for bot turn execution: {}", game.getState());
                return ResponseEntity.badRequest().build();
            }

            // Ejecutar el turno completo del bot
            PlayerEntity botEntity = playerMapper.toEntity(botPlayer);
            GameEntity gameEntity = gameMapper.toEntity(game);

            // El bot ejecuta automáticamente todas las fases
            botService.executeBotTurn(botEntity, gameEntity);

            // Avanzar al siguiente turno
            gameStateService.nextTurn(game);

            // Guardar cambios
            Game savedGame = gameService.save(game);
            GameResponseDto response = gameMapper.toResponseDto(savedGame);

            log.info("Bot {} successfully completed turn in game {}. Next player: {}",
                    botId, gameCode, savedGame.getCurrentPlayerIndex());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error executing bot turn for bot {} in game {}: {}", botId, gameCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene información específica de un bot en el juego.
     *
     * @param gameCode Código del juego
     * @param botId ID del bot
     * @return Información del bot y su estado en el juego
     */
    @GetMapping("/games/{gameCode}/{botId}/status")
    @Operation(summary = "Obtener estado del bot en juego",
            description = "Devuelve información sobre el bot y su estado actual en el juego específico")
    public ResponseEntity<BotStatusDto> getBotStatus(
            @PathVariable String gameCode,
            @PathVariable Long botId) {

        try {
            Optional<Player> botPlayerOpt = playerService.findById(botId);
            if (botPlayerOpt.isEmpty() || !botPlayerOpt.get().getIsBot()) {
                return ResponseEntity.badRequest().build();
            }

            PlayerEntity botPlayer = playerMapper.toEntity(botPlayerOpt.orElseThrow());
            Game game = gameService.findByGameCode(gameCode);

            BotStatusDto status = BotStatusDto.builder()
                    .botId(botId)
                    .botName(botPlayer.getBotProfile().getBotName())
                    .level(botPlayer.getBotProfile().getLevel())
                    .strategy(botPlayer.getBotProfile().getStrategy())
                    .isCurrentTurn(gameStateService.isPlayerTurn(game, botId))
                    .territoriesCount((int)botPlayer.getTerritories().stream().count())
                    .armiesCount(botPlayer.getArmiesToPlace())
                    .isActive(botPlayer.getStatus().equals(ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus.ACTIVE))
                    .canExecuteTurn(gameStateService.isPlayerTurn(game, botId) &&
                            gameStateService.canPerformAction(game, "bot_turn"))
                    .build();

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("Error getting bot status for bot {} in game {}: {}", botId, gameCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lista todos los bots activos en el juego.
     *
     * @param gameCode Código del juego
     * @return Lista de bots en el juego
     */
    @GetMapping("/games/{gameCode}")
    @Operation(summary = "Listar bots del juego",
            description = "Devuelve todos los bots participantes en el juego específico")
    public ResponseEntity<List<BotStatusDto>> getGameBots(@PathVariable String gameCode) {
        try {
            Game game = gameService.findByGameCode(gameCode);

            List<BotStatusDto> bots = game.getPlayers().stream()
                    .filter(Player::getIsBot)
                    .map(botPlayer -> BotStatusDto.builder()
                            .botId(botPlayer.getId())
                            .botName(botPlayer.getDisplayName())
                            .level(botPlayer.getBotLevel())
                            .strategy(BotStrategy.AGGRESSIVE)
                            .isCurrentTurn(gameStateService.isPlayerTurn(game, botPlayer.getId()))
                            .territoriesCount(botPlayer.getTerritoryCount())
                            .armiesCount(botPlayer.getArmiesToPlace())
                            .isActive(botPlayer.getStatus().equals(ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus.ACTIVE))
                            .canExecuteTurn(gameStateService.isPlayerTurn(game, botPlayer.getId()) &&
                                    gameStateService.canPerformAction(game, "bot_turn"))
                            .build())
                    .toList();

            return ResponseEntity.ok(bots);

        } catch (Exception e) {
            log.error("Error getting bots for game {}: {}", gameCode, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ===== ENDPOINTS PARA PERFILES DE BOTS =====

    /**
     * Lista perfiles de bots por nivel.
     *
     * @param level Nivel de dificultad del bot
     * @return Lista de perfiles de bots del nivel especificado
     */
    @GetMapping("/profiles")
    @Operation(summary = "Listar perfiles de bots por nivel",
            description = "Devuelve todos los perfiles de bots disponibles de un nivel específico")
    public ResponseEntity<List<BotProfileEntity>> getBotsByLevel(@RequestParam BotLevel level) {
        try {
            List<BotProfileEntity> botProfiles = botService.findByLevel(level);
            return ResponseEntity.ok(botProfiles);
        } catch (Exception e) {
            log.error("Error getting bot profiles for level {}: {}", level, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene todos los perfiles de bots disponibles.
     *
     * @return Lista completa de perfiles de bots
     */
    @GetMapping("/profiles/all")
    @Operation(summary = "Listar todos los perfiles de bots",
            description = "Devuelve todos los perfiles de bots disponibles")
    public ResponseEntity<List<BotProfileEntity>> getAllBotProfiles() {
        try {
            List<BotProfileEntity> botProfiles = botService.findAll();
            return ResponseEntity.ok(botProfiles);
        } catch (Exception e) {
            log.error("Error getting all bot profiles: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene un perfil de bot específico por ID.
     *
     * @param profileId ID del perfil del bot
     * @return Perfil del bot
     */
    @GetMapping("/profiles/{profileId}")
    @Operation(summary = "Obtener perfil de bot por ID",
            description = "Devuelve el perfil específico de un bot")
    public ResponseEntity<BotProfileEntity> getBotProfile(@PathVariable Long profileId) {
        try {
            Optional<BotProfileEntity> botProfile = botService.findById(profileId);
            return botProfile.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting bot profile {}: {}", profileId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ===== CLASES DTO =====

    /**
     * DTO para información del estado del bot.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BotStatusDto {
        private Long botId;
        private String botName;
        private BotLevel level;
        private ar.edu.utn.frc.tup.piii.model.enums.BotStrategy strategy;
        private Boolean isCurrentTurn;
        private Integer territoriesCount;
        private Integer armiesCount;
        private Boolean isActive;
        private Boolean canExecuteTurn;
    }
}