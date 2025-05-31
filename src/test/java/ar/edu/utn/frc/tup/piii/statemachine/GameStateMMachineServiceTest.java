package ar.edu.utn.frc.tup.piii.statemachine;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.service.StateMachineService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GameStateMachineServiceTest {

    @Autowired
    private GameStateMachineService gameStateMachineService;

    @Autowired
    private StateMachineService<GameStates, GameEvents> stateMachineService;

    @Test
    void stateMachineServiceIsInjected() {
        assertThat(stateMachineService).isNotNull();
        assertThat(gameStateMachineService).isNotNull();
    }

    @Test
    void canAcquireStateMachine() {
        // Given
        String gameId = "TEST123";

        // When
        StateMachine<GameStates, GameEvents> stateMachine =
                gameStateMachineService.getStateMachine(gameId);

        // Then
        assertThat(stateMachine).isNotNull();
        assertThat(stateMachine.getState().getId()).isEqualTo(GameStates.WAITING_FOR_PLAYERS);

        // Cleanup
        gameStateMachineService.releaseStateMachine(gameId);
    }

    @Test
    void canSendEvents() {
        // Given
        String gameId = "TEST456";
        StateMachine<GameStates, GameEvents> stateMachine =
                gameStateMachineService.getStateMachine(gameId);

        // When
        stateMachine.sendEvent(GameEvents.GAME_STARTED);

        // Then
        assertThat(stateMachine.getState().getId()).isEqualTo(GameStates.DISTRIBUTING_COUNTRIES);

        // Cleanup
        gameStateMachineService.releaseStateMachine(gameId);
    }
}