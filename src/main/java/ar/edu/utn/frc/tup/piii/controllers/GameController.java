package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.bot.AddBotsDto;
import ar.edu.utn.frc.tup.piii.dtos.game.GameCreationDto;
import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.game.JoinGameDto;
import ar.edu.utn.frc.tup.piii.dtos.game.StartGameDto;
import ar.edu.utn.frc.tup.piii.exceptions.ForbiddenException;
import ar.edu.utn.frc.tup.piii.exceptions.GameNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
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
     * – Crear nueva partida.
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

    //obitene los datos completos del juego a travez del gamecode
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
     * -Unirse a partida existente.
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
     * – Añadir bots a la partida (solo el anfitrión).
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
        if (! dto.getRequesterId().equals(existing.getCreatedByUserId())) {
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

}
