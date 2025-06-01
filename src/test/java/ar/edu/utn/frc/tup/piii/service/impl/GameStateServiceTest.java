package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.service.impl.GameStateServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;

public class GameStateServiceTest {

    private GameStateServiceImpl stateService;
    private Game game;

    @BeforeEach
    void setUp() {
        stateService = new GameStateServiceImpl();
        game = new Game();
        game.setState(GameState.WAITING_FOR_PLAYERS);
        game.setMaxPlayers(2);
        // Agregar 2 jugadores para que canStart() = true
    }

    @Test
    void startGame_ShouldChangeStateToInProgress() {
        boolean result = stateService.startGame(game);

        assertThat(result).isTrue();
        assertThat(game.getState()).isEqualTo(GameState.IN_PROGRESS);
        assertThat(game.getCurrentPhase()).isEqualTo(TurnPhase.REINFORCEMENT);
    }

    @Test
    void changeTurnPhase_ShouldAdvanceFromReinforcementToAttack() {
        game.setState(GameState.IN_PROGRESS);
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);

        boolean result = stateService.changeTurnPhase(game, TurnPhase.ATTACK);

        assertThat(result).isTrue();
        assertThat(game.getCurrentPhase()).isEqualTo(TurnPhase.ATTACK);
    }

    @Test
    void canPerformAction_ShouldReturnTrueForValidAction() {
        game.setState(GameState.IN_PROGRESS);
        game.setCurrentPhase(TurnPhase.ATTACK);

        boolean result = stateService.canPerformAction(game, "attack");

        assertThat(result).isTrue();
    }
}