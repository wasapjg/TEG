package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.bot.AddBotsDto;
import ar.edu.utn.frc.tup.piii.dtos.game.*;
import ar.edu.utn.frc.tup.piii.exceptions.*;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
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
    private GameService gameService;

    @Autowired
    private GameMapper gameMapper;

    /**
     * POST /api/games/create-lobby
     * Recibe: { "hostUserId": Long }
     * Crea en BD un GameEntity con:
     *    - gameCode único
     *    - hostUserId
     *    - maxPlayers  = 6   (valor por defecto)
     *    - turnTimeLimit = 120 (valor por defecto, en segundos)
     *    - chatEnabled = true
     *    - pactsAllowed = false
     *    - estado = WAITING_FOR_PLAYERS
     * Crea un PlayerEntity para el host, con color RED (por ej.), status WAITING.
     * Retorna: GameResponseDto completo (con lista de jugadores, configuraciones, gameCode, etc.)
     */
    @PostMapping("/create-lobby")
    public ResponseEntity<GameResponseDto> createLobby(@RequestBody CreateCodeDto dto) {
        if (dto.getHostUserId() == null) {
            throw new IllegalArgumentException("Debe enviar hostUserId en el CreateCodeDto");
        }

        // El service va a crear el GameEntity en BD con defaults y crear el player del host
        Game createdGame = gameService.createLobbyWithDefaults(dto.getHostUserId());

        // Mapeo a GameResponseDto
        GameResponseDto response = gameMapper.toResponseDto(createdGame);

        // HTTP 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    //obtiene los datos completos del juego a través del gamecode
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

    //obtiene un DTO con playerId y un mapa de countryId a cantidadDeEjercitosAPoner a través del gamecode
    @PostMapping("/{gameCode}/place-initial-armies")
    public ResponseEntity<String> placeInitialArmies(
            @PathVariable String gameCode,
            @RequestBody InitialArmyPlacementDto dto) {
        try {
            gameService.prepareInitialPlacementPhase(gameCode, dto.getPlayerId(), dto.getArmiesByCountry());
            return ResponseEntity.ok("Armies in place.");
        } catch (GameNotFoundException | PlayerNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("InternalError");
        }
    }


    /**
     * Unirse a partida existente.
     * Recibe JoinGameDto con:
     *  - gameCode (String)
     *  - userId   (Long)
     */
    @PostMapping("/join")
    public ResponseEntity<GameResponseDto> joinGame(@RequestBody JoinGameDto dto) {
        if (dto.getGameCode() == null || dto.getUserId() == null) {
            throw new IllegalArgumentException("Debe enviar gameCode y userId en el JoinGameDto");
        }
        Game updatedGame = gameService.joinGame(dto);
        GameResponseDto response = gameMapper.toResponseDto(updatedGame);
        return ResponseEntity.ok(response);
    }

    /**
     * Añadir bots a la partida (solo el anfitrión).
     * Recibe AddBotsDto con:
     *  - gameCode     (String)
     *  - numberOfBots (Integer)
     *  - botLevel     (BotLevel)
     *  - botStrategy  (BotStrategy)
     *  - requesterId  (Long)
     */
    @PostMapping("/add-bots")
    public ResponseEntity<GameResponseDto> addBotsToGame(@RequestBody AddBotsDto dto) {
        if (dto.getGameCode() == null || dto.getRequesterId() == null) {
            throw new IllegalArgumentException("Debe enviar gameCode y requesterId en el AddBotsDto");
        }
        Game existing = gameService.findByGameCode(dto.getGameCode());

        if (existing.getCreatedByUserId() == null) {
            throw new IllegalStateException("Error interno: createdByUserId es null para gameCode=" + dto.getGameCode());
        }
        if (!dto.getRequesterId().equals(existing.getCreatedByUserId())) {
            throw new ForbiddenException("Solo el anfitrión puede agregar bots a la partida");
        }
        Game updatedGame = gameService.addBotsToGame(dto);
        GameResponseDto response = gameMapper.toResponseDto(updatedGame);
        return ResponseEntity.ok(response);
    }

    /**
     * Iniciar la partida (solo el anfitrión).
     * Recibe StartGameDto con:
     *  - gameCode (String)
     *  - userId   (Long)
     */
    @PostMapping("/start")
    public ResponseEntity<GameResponseDto> startGame(@RequestBody StartGameDto dto) {
        if (dto.getGameCode() == null || dto.getUserId() == null) {
            throw new IllegalArgumentException("Debe enviar gameCode y userId en el StartGameDto");
        }
        Game existing = gameService.findByGameCode(dto.getGameCode());
        if (!Objects.equals(existing.getCreatedByUserId(), dto.getUserId())) {
            throw new ForbiddenException("Solo el anfitrión puede iniciar la partida");
        }
        Game startedGame = gameService.startGame(dto.getGameCode());

        GameResponseDto response = gameMapper.toResponseDto(startedGame);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar configuraciones del juego (solo el anfitrión).
     * Recibe UpdateGameSettingsDto con:
     *  - requesterId    (Long)     - ID del usuario que hace la petición
     *  - maxPlayers     (Integer)  - Nuevo límite de jugadores (opcional)
     *  - turnTimeLimit  (Integer)  - Nuevo límite de tiempo por turno (opcional)
     *  - chatEnabled    (Boolean)  - Habilitar/deshabilitar chat (opcional)
     *  - pactsAllowed   (Boolean)  - Permitir/prohibir pactos (opcional)
     */
    @PutMapping("/{gameCode}/settings")
    public ResponseEntity<GameResponseDto> updateGameSettings(@PathVariable String gameCode, @RequestBody UpdateGameSettingsDto dto) {

        if (dto.getRequesterId() == null) {
            throw new IllegalArgumentException("requesterId is required");
        }

        Game updatedGame = gameService.updateGameSettings(gameCode, dto);
        GameResponseDto response = gameMapper.toResponseDto(updatedGame);

        return ResponseEntity.ok(response);
    }

    /**
     * Expulsa a un jugador de la partida (solo el host puede hacerlo).
     * Recibe: { "gameCode": "...", "playerId": 2 }
     * Retorna: 200 + GameResponseDto actualizado (sin el jugador expulsado).
     */

    @PostMapping("/kick-player")
    public ResponseEntity<GameResponseDto> kickPlayer(@RequestBody KickPlayerDto dto) {
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
     * Permite a un jugador salir voluntariamente del jeugo mientras esta en el lobby
     * Solo es valido cuando el jeugo esta en estado Waiting_for_players

     * recibe: { "gameCode": "...", "playerId": 2 }
     * Devuelve: 200 +  GameResponseDto Actualizado (Sin el jugador que se fue).
     */
    @PostMapping("/leave")
    public ResponseEntity<GameResponseDto> leaveGame(@RequestBody LeaveGameDto dto) {
        if (dto.getGameCode() == null || dto.getUserId() == null) {
            throw new BadRequestException("Debe enviar gameCode y userId en el LeaveGameDto");
        }
        Game game = gameService.leaveGame(dto);
        return ResponseEntity.ok(gameMapper.toResponseDto(game));
    }
}