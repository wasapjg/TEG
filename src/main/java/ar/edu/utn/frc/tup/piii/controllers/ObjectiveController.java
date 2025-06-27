package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.objective.WinnerDto;
import ar.edu.utn.frc.tup.piii.model.Objective;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.service.interfaces.ObjectiveService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/objectives")
public class ObjectiveController {

    @Autowired
    private ObjectiveService objectiveService;

    @Autowired
    private GameService gameService;

    @Autowired
    private PlayerService playerService;

    @GetMapping
    public List<Objective> getAllObjectives() {
        return objectiveService.findAll();
    }

    @GetMapping("/{id}")
    public Objective getObjectiveById(@PathVariable Long id) {
        return objectiveService.findById(id).orElse(null);
    }

    @PostMapping
    public Objective createObjective(@RequestBody Objective objective) {
        return objectiveService.save(objective);
    }

    @DeleteMapping("/{id}")
    public void deleteObjective(@PathVariable Long id) {
        objectiveService.deleteById(id);
    }

    @PostMapping("/assign/{gameId}")
    public void assignObjectives(@PathVariable Long gameId) {
        Game game = gameService.findById(gameId);
        objectiveService.assignObjectivesToPlayers(game);
    }

    @GetMapping("/validate")
    public boolean validateObjective(
            @RequestParam Long objectiveId,
            @RequestParam Long gameId,
            @RequestParam Long playerId) {
        Game game = gameService.findById(gameId);
        Player player = playerService.findById(playerId).orElse(null);
        if (player == null) return false;

        return objectiveService.isObjectiveAchieved(objectiveId, game, player);
    }

    @GetMapping("/winner")
    public WinnerDto getWinner(@RequestParam Long gameId) {
        Game game = gameService.findById(gameId);

        return objectiveService.findWinner(game)
                .map(player -> WinnerDto.builder()
                        .playerId(player.getId())
                        .playerName(player.getDisplayName()) // o getUser().getUsername()
                        .objectiveDescription(player.getObjective().getDescription())
                        .build())
                .orElse(null);
    }

}
