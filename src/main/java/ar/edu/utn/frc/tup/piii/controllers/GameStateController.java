package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.game.GameStateResponse;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.service.impl.GameStateServiceImpl;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games/{gameId}/state")
public class GameStateController {

    @Autowired
    private GameStateServiceImpl stateService;

    @Autowired
    private GameService gameService;

    @PostMapping("/start")
    public ResponseEntity<String> startGame(@PathVariable Long gameId) {
        Game game = gameService.findById(gameId);

        if (stateService.startGame(game)) {
            gameService.save(game);
            return ResponseEntity.ok("Game started successfully");
        }

        return ResponseEntity.badRequest().body("Cannot start game");
    }

    @PostMapping("/pause")
    public ResponseEntity<String> pauseGame(@PathVariable Long gameId) {
        Game game = gameService.findById(gameId);

        if (stateService.pauseGame(game)) {
            gameService.save(game);
            return ResponseEntity.ok("Game paused");
        }

        return ResponseEntity.badRequest().body("Cannot pause game");
    }

    @PostMapping("/resume")
    public ResponseEntity<String> resumeGame(@PathVariable Long gameId) {
        Game game = gameService.findById(gameId);

        if (stateService.resumeGame(game)) {
            gameService.save(game);
            return ResponseEntity.ok("Game resumed");
        }

        return ResponseEntity.badRequest().body("Cannot resume game");
    }

    @PostMapping("/finish")
    public ResponseEntity<String> finishGame(@PathVariable Long gameId) {
        Game game = gameService.findById(gameId);

        if (stateService.finishGame(game)) {
            gameService.save(game);
            return ResponseEntity.ok("Game finished");
        }

        return ResponseEntity.badRequest().body("Cannot finish game");
    }

    @PostMapping("/action/{action}")
    public ResponseEntity<String> performAction(
            @PathVariable Long gameId,
            @PathVariable String action) {

        Game game = gameService.findById(gameId);

        if (!stateService.canPerformAction(game, action)) {
            return ResponseEntity.badRequest()
                    .body("Action '" + action + "' not allowed in current phase");
        }

        // Procesar la acción
        switch (action.toLowerCase()) {
            case "next_phase":
                advancePhase(game);
                break;
            case "next_turn":
                stateService.nextTurn(game);
                break;
            case "skip_attack":
                stateService.changeTurnPhase(game, TurnPhase.FORTIFY);
                break;
            case "skip_fortify":
                stateService.changeTurnPhase(game, TurnPhase.END_TURN);
                break;
            default:
                return ResponseEntity.badRequest().body("Unknown action: " + action);
        }

        gameService.save(game);
        return ResponseEntity.ok("Action performed: " + action);
    }

    @GetMapping("/current")
    public ResponseEntity<GameStateResponse> getCurrentState(@PathVariable Long gameId) {
        Game game = gameService.findById(gameId);

        GameStateResponse response = new GameStateResponse();
        response.setGameState(game.getState());
        response.setTurnPhase(game.getCurrentPhase());
        response.setCurrentPlayer(game.getCurrentPlayer() != null ?
                game.getCurrentPlayer().getDisplayName() : "No current player");
        response.setCurrentTurn(game.getCurrentTurn());
        response.setAvailableActions(stateService.getAvailableActions(game));
        response.setPhaseDescription(stateService.getCurrentPhaseDescription(game));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/can-perform/{action}")
    public ResponseEntity<Boolean> canPerformAction(
            @PathVariable Long gameId,
            @PathVariable String action) {

        Game game = gameService.findById(gameId);
        boolean canPerform = stateService.canPerformAction(game, action);

        return ResponseEntity.ok(canPerform);
    }

    private void advancePhase(Game game) {
        switch (game.getCurrentPhase()) {
            case REINFORCEMENT:
                stateService.changeTurnPhase(game, TurnPhase.ATTACK);
                break;
            case ATTACK:
                stateService.changeTurnPhase(game, TurnPhase.FORTIFY);
                break;
            case FORTIFY:
                stateService.changeTurnPhase(game, TurnPhase.END_TURN);
                break;
            default:
                // No hacer nada si está en END_TURN
                break;
        }
    }
}