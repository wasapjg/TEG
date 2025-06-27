package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.player.PlayerRequestDto;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.User;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.service.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    @Autowired
    private UserService userService;
    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    @GetMapping
    public List<Player> getAllPlayers() {
        return playerService.findAll();
    }

    @GetMapping("/{id}")
    public Player getPlayerById(@PathVariable Long id) {
        return playerService.findById(id).orElse(null);
    }

    @PostMapping
    public Player createPlayer(@RequestBody PlayerRequestDto request) {
        User user = userService.getUserById(request.getUserId());
        Game game = gameService.findById(request.getGameId());

        Player player;

        if (Boolean.TRUE.equals(request.getIsBot())) {
            player = playerService.createBotPlayer(BotLevel.valueOf(request.getBotLevel()), game);
        } else {
            player = playerService.createHumanPlayer(user, game, request.getSeatOrder());
        }

        return player;
    }


    @DeleteMapping("/{id}")
    public void deletePlayer(@PathVariable Long id) {
        playerService.deleteById(id);
    }
}
