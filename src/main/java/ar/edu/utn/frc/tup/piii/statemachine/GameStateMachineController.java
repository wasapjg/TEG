package ar.edu.utn.frc.tup.piii.statemachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games/{gameId}/state")
public class GameStateMachineController {

    private final GameStateMachineService gameStateMachineService;

    @Autowired
    public GameStateMachineController(GameStateMachineService gameStateMachineService) {
        this.gameStateMachineService = gameStateMachineService;
    }

    @PostMapping("/event/{event}")
    public void sendEvent(@PathVariable String gameId, @PathVariable GameEvents event) {
        StateMachine<GameStates, GameEvents> stateMachine =
                gameStateMachineService.getStateMachine(gameId);

        stateMachine.sendEvent(event);
    }

    @GetMapping("/current")
    public GameStates getCurrentState(@PathVariable String gameId) {
        StateMachine<GameStates, GameEvents> stateMachine =
                gameStateMachineService.getStateMachine(gameId);

        return stateMachine.getState() != null ? stateMachine.getState().getId() : null;
    }
}