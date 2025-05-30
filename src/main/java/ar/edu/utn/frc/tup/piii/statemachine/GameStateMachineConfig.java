package ar.edu.utn.frc.tup.piii.statemachine;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.EnumSet;

@Configuration
@EnableStateMachine
public class GameStateMachineConfig extends EnumStateMachineConfigurerAdapter<GameStates, GameEvents> {

    @Autowired
    private GameStateMachineService gameStateMachineService;

    @Override
    public void configure(StateMachineConfigurationConfigurer<GameStates, GameEvents> config)
            throws Exception {
        config
                .withConfiguration()
                .autoStartup(false)
                .listener(stateMachineListener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<GameStates, GameEvents> states)
            throws Exception {
        states
                .withStates()
                .initial(GameStates.WAITING_FOR_PLAYERS)
                .states(EnumSet.allOf(GameStates.class))
                .end(GameStates.GAME_OVER);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<GameStates, GameEvents> transitions)
            throws Exception {
        transitions
                // Transiciones de inicio de juego
                .withExternal()
                .source(GameStates.WAITING_FOR_PLAYERS)
                .target(GameStates.DISTRIBUTING_COUNTRIES)
                .event(GameEvents.GAME_STARTED)
                .action(gameStateMachineService::onGameStart)

                .and()
                .withExternal()
                .source(GameStates.DISTRIBUTING_COUNTRIES)
                .target(GameStates.INITIAL_PLACEMENT)
                .event(GameEvents.COUNTRIES_DISTRIBUTED)
                .action(gameStateMachineService::onCountriesDistributed)

                .and()
                .withExternal()
                .source(GameStates.INITIAL_PLACEMENT)
                .target(GameStates.GAME_STARTED)
                .event(GameEvents.INITIAL_ARMIES_PLACED)
                .action(gameStateMachineService::onInitialArmiesPlaced)

                // Transiciones del ciclo de juego
                .and()
                .withExternal()
                .source(GameStates.GAME_STARTED)
                .target(GameStates.REINFORCEMENT_PHASE)
                .event(GameEvents.START_TURN)
                .action(gameStateMachineService::onTurnStart)

                .and()
                .withExternal()
                .source(GameStates.REINFORCEMENT_PHASE)
                .target(GameStates.ATTACK_PHASE)
                .event(GameEvents.REINFORCE)
                .action(gameStateMachineService::onReinforcement)

                .and()
                .withExternal()
                .source(GameStates.ATTACK_PHASE)
                .target(GameStates.FORTIFY_PHASE)
                .event(GameEvents.ATTACK_COMPLETED)
                .action(gameStateMachineService::onAttackCompleted)

                .and()
                .withExternal()
                .source(GameStates.FORTIFY_PHASE)
                .target(GameStates.TURN_END)
                .event(GameEvents.FORTIFY_COMPLETED)
                .action(gameStateMachineService::onFortifyCompleted)

                .and()
                .withExternal()
                .source(GameStates.TURN_END)
                .target(GameStates.REINFORCEMENT_PHASE)
                .event(GameEvents.START_TURN)
                .action(gameStateMachineService::onNextTurn)

                // Transiciones de fin de juego
                .and()
                .withExternal()
                .source(GameStates.ATTACK_PHASE)
                .target(GameStates.GAME_OVER)
                .event(GameEvents.GAME_WON)
                .action(gameStateMachineService::onGameWon)

                .and()
                .withExternal()
                .source(GameStates.REINFORCEMENT_PHASE)
                .target(GameStates.GAME_OVER)
                .event(GameEvents.GAME_WON)
                .action(gameStateMachineService::onGameWon)

                .and()
                .withExternal()
                .source(GameStates.FORTIFY_PHASE)
                .target(GameStates.GAME_OVER)
                .event(GameEvents.GAME_WON)
                .action(gameStateMachineService::onGameWon)

                // Transiciones de pausa
                .and()
                .withExternal()
                .source(GameStates.REINFORCEMENT_PHASE)
                .target(GameStates.PAUSED)
                .event(GameEvents.PAUSE_GAME)
                .action(gameStateMachineService::onGamePaused)

                .and()
                .withExternal()
                .source(GameStates.ATTACK_PHASE)
                .target(GameStates.PAUSED)
                .event(GameEvents.PAUSE_GAME)
                .action(gameStateMachineService::onGamePaused)

                .and()
                .withExternal()
                .source(GameStates.FORTIFY_PHASE)
                .target(GameStates.PAUSED)
                .event(GameEvents.PAUSE_GAME)
                .action(gameStateMachineService::onGamePaused)

                .and()
                .withExternal()
                .source(GameStates.PAUSED)
                .target(GameStates.REINFORCEMENT_PHASE)
                .event(GameEvents.RESUME_GAME)
                .action(gameStateMachineService::onGameResumed)

                // Transiciones por timeout
                .and()
                .withExternal()
                .source(GameStates.REINFORCEMENT_PHASE)
                .target(GameStates.ATTACK_PHASE)
                .event(GameEvents.TIMEOUT)
                .action(gameStateMachineService::onTimeout)

                .and()
                .withExternal()
                .source(GameStates.ATTACK_PHASE)
                .target(GameStates.FORTIFY_PHASE)
                .event(GameEvents.TIMEOUT)
                .action(gameStateMachineService::onTimeout)

                .and()
                .withExternal()
                .source(GameStates.FORTIFY_PHASE)
                .target(GameStates.TURN_END)
                .event(GameEvents.TIMEOUT)
                .action(gameStateMachineService::onTimeout);
    }

    @Bean
    public StateMachineListener<GameStates, GameEvents> stateMachineListener() {
        return new StateMachineListenerAdapter<GameStates, GameEvents>() {
            @Override
            public void stateChanged(State<GameStates, GameEvents> from, State<GameStates, GameEvents> to) {
                System.out.println("State changed from " +
                        (from != null ? from.getId() : "null") + " to " + to.getId());
            }
        };
    }

}
