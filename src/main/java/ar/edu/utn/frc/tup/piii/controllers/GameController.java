package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.bot.AddBotsDto;
import ar.edu.utn.frc.tup.piii.dtos.game.*;
import ar.edu.utn.frc.tup.piii.exceptions.*;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.service.impl.GameServiceImpl;
import ar.edu.utn.frc.tup.piii.service.impl.InitialPlacementService;
import ar.edu.utn.frc.tup.piii.service.interfaces.CombatService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;


@RestController
@RequestMapping("/api/games")
public class GameController {

    @Autowired
    private GameServiceImpl gameService;

    @Autowired
    private InitialPlacementService initialPlacementService;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private CombatService combatService;

    /**
     * Crea un nuevo lobby con configuraciones por defecto.
     * POST /api/games/create-lobby
     */
    @PostMapping("/create-lobby")
    public ResponseEntity<GameResponseDto> createLobby(@Valid @RequestBody CreateCodeDto dto) {
        if (dto.getHostUserId() == null) {
            throw new IllegalArgumentException("Debe enviar hostUserId en el CreateCodeDto");
        }

        Game createdGame = gameService.createLobbyWithDefaults(dto.getHostUserId());
        GameResponseDto response = gameMapper.toResponseDto(createdGame);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene los datos completos del juego a través del gameCode.
     * GET /api/games/{gameCode}
     */
    @GetMapping("/{gameCode}")
    public ResponseEntity<GameResponseDto> getGameByCode(@PathVariable String gameCode) {
        try {
            GameResponseDto game = gameService.getGameByCode(gameCode);
            return ResponseEntity.ok(game);
        } catch (GameNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Permite a un usuario unirse a una partida existente.
     * POST /api/games/join
     */
    @PostMapping("/join")
    public ResponseEntity<GameResponseDto> joinGame(@Valid @RequestBody JoinGameDto dto) {
        if (dto.getGameCode() == null || dto.getUserId() == null) {
            throw new IllegalArgumentException("Debe enviar gameCode y userId en el JoinGameDto");
        }

        Game updatedGame = gameService.joinGame(dto);
        GameResponseDto response = gameMapper.toResponseDto(updatedGame);
        return ResponseEntity.ok(response);
    }

    /**
     * Añade bots a la partida (solo el anfitrión).
     * POST /api/games/add-bots
     */
    @PostMapping("/add-bots")
    public ResponseEntity<GameResponseDto> addBotsToGame(@Valid @RequestBody AddBotsDto dto) {
        if (dto.getGameCode() == null || dto.getRequesterId() == null) {
            throw new IllegalArgumentException("Debe enviar gameCode y requesterId en el AddBotsDto");
        }

        // Validar que el requesterId es el host (se hace en el servicio)
        Game updatedGame = gameService.addBotsToGame(dto);
        GameResponseDto response = gameMapper.toResponseDto(updatedGame);
        return ResponseEntity.ok(response);
    }

    /**
     * Inicia la partida (solo el anfitrión).
     * POST /api/games/start
     */
    @PostMapping("/start")
    public ResponseEntity<GameResponseDto> startGame(@Valid @RequestBody StartGameDto dto) {
        if (dto.getGameCode() == null || dto.getUserId() == null) {
            throw new IllegalArgumentException("Debe enviar gameCode y userId en el StartGameDto");
        }

        // Validar que el userId es el host
        Game existing = gameService.findByGameCode(dto.getGameCode());
        if (!Objects.equals(existing.getCreatedByUserId(), dto.getUserId())) {
            throw new ForbiddenException("Solo el anfitrión puede iniciar la partida");
        }

        Game startedGame = gameService.startGame(dto.getGameCode());
        GameResponseDto response = gameMapper.toResponseDto(startedGame);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza configuraciones del juego (solo el anfitrión).
     * PUT /api/games/{gameCode}/settings
     */
    @PutMapping("/{gameCode}/settings")
    public ResponseEntity<GameResponseDto> updateGameSettings(
            @PathVariable String gameCode,
            @Valid @RequestBody UpdateGameSettingsDto dto) {

        if (dto.getRequesterId() == null) {
            throw new IllegalArgumentException("requesterId is required");
        }

        Game updatedGame = gameService.updateGameSettings(gameCode, dto);
        GameResponseDto response = gameMapper.toResponseDto(updatedGame);

        return ResponseEntity.ok(response);
    }

    /**
     * Expulsa a un jugador de la partida (solo el host puede hacerlo).
     * POST /api/games/kick-player
     */
    @PostMapping("/kick-player")
    public ResponseEntity<GameResponseDto> kickPlayer(@Valid @RequestBody KickPlayerDto dto) {
        if (dto.getGameCode() == null || dto.getPlayerId() == null) {
            throw new IllegalArgumentException("Debe enviar gameCode y playerId en el KickPlayerDto");
        }

        try {
            Game updatedGame = gameService.kickPlayer(dto);
            GameResponseDto response = gameMapper.toResponseDto(updatedGame);
            return ResponseEntity.ok(response);

        } catch (GameNotFoundException | PlayerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (InvalidGameStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Permite a un jugador salir voluntariamente del juego mientras está en el lobby.
     * POST /api/games/leave
     */
    @PostMapping("/leave")
    public ResponseEntity<GameResponseDto> leaveGame(@Valid @RequestBody LeaveGameDto dto) {
        if (dto.getGameCode() == null || dto.getUserId() == null) {
            throw new BadRequestException("Debe enviar gameCode y userId en el LeaveGameDto");
        }

        Game game = gameService.leaveGame(dto);
        return ResponseEntity.ok(gameMapper.toResponseDto(game));
    }

    /**
     * Endpoint legacy para colocación inicial de ejércitos.
     * Redirige al servicio especializado para mantener compatibilidad.
     *
     * @deprecated Usar InitialPlacementController en su lugar
     */
    @PostMapping("/{gameCode}/place-initial-armies")
    public ResponseEntity<String> placeInitialArmiesLegacy(
            @PathVariable String gameCode,
            @Valid @RequestBody InitialArmyPlacementDto dto) {

        try {
            // Usar el servicio especializado directamente
            initialPlacementService.placeInitialArmies(gameCode, dto.getPlayerId(), dto.getArmiesByCountry());
            return ResponseEntity.ok("Armies placed successfully. Consider using /api/games/{gameCode}/initial-placement/place-armies for new implementations.");

        } catch (GameNotFoundException | PlayerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException | InvalidGameStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Error: " + e.getMessage());
        }
    }

    @PostMapping("/test-combat/{gameCode}")
    public ResponseEntity<CombatResultDto> testCombat(
            @PathVariable String gameCode,
            @RequestBody AttackDto attackDto) {
        return ResponseEntity.ok(combatService.performCombat(gameCode, attackDto));
    }
}