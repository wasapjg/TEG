package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.bot.AddBotsDto;
import ar.edu.utn.frc.tup.piii.dtos.game.GameCreationDto;
import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.game.JoinGameDto;
import ar.edu.utn.frc.tup.piii.dtos.game.StartGameDto;
import ar.edu.utn.frc.tup.piii.exceptions.ForbiddenException;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * Controlador de partidas sin JWT. El cliente envía userId “en crudo” en cada DTO.
 */
@RestController
@RequestMapping("/api/games")
public class GameController {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameMapper gameMapper;

    /**
     * RF003 – Crear nueva partida.
     * Recibe GameCreationDto con:
     *  - createdByUserId (Long)
     *  - maxPlayers       (Integer)
     *  - turnTimeLimit    (Integer)
     *  - chatEnabled      (Boolean)
     *  - pactsAllowed     (Boolean)
     */
    @PostMapping
    public ResponseEntity<GameResponseDto> createGame(@RequestBody GameCreationDto dto) {
        if (dto.getCreatedByUserId() == null) {
            throw new IllegalArgumentException("Debe enviar createdByUserId en el GameCreationDto");
        }
        Game createdGame = gameService.createNewGame(dto);
        GameResponseDto response = gameMapper.toResponseDto(createdGame);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * RF004 – Unirse a partida existente.
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
     * RF003.1 / RF008 – Añadir bots a la partida (solo el anfitrión).
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
        // Validamos que quien llama sea el creador (host) de la partida
        Game existing = gameService.findByGameCode(dto.getGameCode());
        if (!existing.getCreatedByUserId().equals(dto.getRequesterId())) {
            throw new ForbiddenException("Solo el anfitrión puede agregar bots a la partida");
        }
        Game updatedGame = gameService.addBotsToGame(dto);
        GameResponseDto response = gameMapper.toResponseDto(updatedGame);
        return ResponseEntity.ok(response);
    }

    /**
     * RF005.2 – Iniciar la partida (solo el anfitrión).
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

}
