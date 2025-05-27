package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.bot.AddBotsDto;
import ar.edu.utn.frc.tup.piii.dtos.bot.BotCreateDto;
import ar.edu.utn.frc.tup.piii.dtos.game.GameCreationDto;
import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.game.JoinGameDto;
import ar.edu.utn.frc.tup.piii.dtos.game.KickPlayerDto;
import ar.edu.utn.frc.tup.piii.dtos.player.PlayerResponseDto;
import ar.edu.utn.frc.tup.piii.model.entity.Game;
import ar.edu.utn.frc.tup.piii.model.entity.Player;
import ar.edu.utn.frc.tup.piii.model.entity.User;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final UserService userService;
    private final PlayerService playerService;

    @PostMapping("/create")
    public ResponseEntity<GameResponseDto> create(@RequestBody GameCreationDto dto) {
        User creator = userService.findById(dto.getCreatedByUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Game game = gameService.createGame(dto);
        return ResponseEntity.ok(toDto(game));
    }


    @PostMapping("/join")
    public ResponseEntity<GameResponseDto> join(@RequestBody JoinGameDto dto) {
        User user = userService.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Game game = gameService.joinGame(dto.getGameCode(), dto.getUserId());
        return ResponseEntity.ok(toDto(game));
    }

    @PostMapping("/bots")
    public ResponseEntity<?> addBots(@RequestBody AddBotsDto dto) {
        try {
            Game game = gameService.addBots(
                    dto.getGameCode(),
                    dto.getCount(),
                    dto.getBotLevel(),
                    dto.getBotStrategy()
            );

            return ResponseEntity.ok(GameResponseDto.fromEntity(game));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error interno");
        }
    }







    @PostMapping("/kick")
    public ResponseEntity<GameResponseDto> kick(@RequestBody KickPlayerDto dto) {
        Game game = gameService.kickPlayer(dto.getGameCode(), dto.getPlayerId());
        return ResponseEntity.ok(toDto(game));
    }

    // Método auxiliar para convertir entidad Game → GameResponseDto
    private GameResponseDto toDto(Game game) {
        List<PlayerResponseDto> players = game.getPlayers().stream()
                .map(this::playerToDto)
                .collect(Collectors.toList());

        String currentPlayerName = game.getCurrentPlayer() != null
                ? game.getCurrentPlayer().getDisplayName()
                : null;

        return GameResponseDto.builder()
                .id(game.getId())
                .gameCode(game.getGameCode())
                .createdByUsername(game.getCreatedBy().getUsername())
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
                .players(players)
                .currentPlayerName(currentPlayerName)
                .build();
    }

    // Adaptar según tu PlayerResponseDto
    private PlayerResponseDto playerToDto(Player p) {
        return PlayerResponseDto.builder()
                .id(p.getId())
                .username(p.getDisplayName())
                .status(p.getStatus().name())
                // añade más campos si lo deseas
                .build();
    }
}
