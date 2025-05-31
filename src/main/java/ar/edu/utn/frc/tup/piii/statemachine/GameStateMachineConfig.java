package ar.edu.utn.frc.tup.piii.statemachine;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory(name = "gameStateMachineFactory")
public class GameStateMachineConfig extends StateMachineConfigurerAdapter<GameStates, GameEvents> {

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

                .and()
                .withExternal()
                .source(GameStates.DISTRIBUTING_COUNTRIES)
                .target(GameStates.INITIAL_PLACEMENT)
                .event(GameEvents.COUNTRIES_DISTRIBUTED)

                .and()
                .withExternal()
                .source(GameStates.INITIAL_PLACEMENT)
                .target(GameStates.GAME_STARTED)
                .event(GameEvents.INITIAL_ARMIES_PLACED)

                // Transiciones del ciclo de juego
                .and()
                .withExternal()
                .source(GameStates.GAME_STARTED)
                .target(GameStates.REINFORCEMENT_PHASE)
                .event(GameEvents.START_TURN)

                .and()
                .withExternal()
                .source(GameStates.REINFORCEMENT_PHASE)
                .target(GameStates.ATTACK_PHASE)
                .event(GameEvents.REINFORCE)

                .and()
                .withExternal()
                .source(GameStates.ATTACK_PHASE)
                .target(GameStates.FORTIFY_PHASE)
                .event(GameEvents.ATTACK_COMPLETED)

                .and()
                .withExternal()
                .source(GameStates.FORTIFY_PHASE)
                .target(GameStates.TURN_END)
                .event(GameEvents.FORTIFY_COMPLETED)

                .and()
                .withExternal()
                .source(GameStates.TURN_END)
                .target(GameStates.REINFORCEMENT_PHASE)
                .event(GameEvents.START_TURN)

                // Transiciones de fin de juego
                .and()
                .withExternal()
                .source(GameStates.ATTACK_PHASE)
                .target(GameStates.GAME_OVER)
                .event(GameEvents.GAME_WON)

                .and()
                .withExternal()
                .source(GameStates.REINFORCEMENT_PHASE)
                .target(GameStates.GAME_OVER)
                .event(GameEvents.GAME_WON)

                .and()
                .withExternal()
                .source(GameStates.FORTIFY_PHASE)
                .target(GameStates.GAME_OVER)
                .event(GameEvents.GAME_WON)

                // Transiciones de pausa
                .and()
                .withExternal()
                .source(GameStates.REINFORCEMENT_PHASE)
                .target(GameStates.PAUSED)
                .event(GameEvents.PAUSE_GAME)

                .and()
                .withExternal()
                .source(GameStates.ATTACK_PHASE)
                .target(GameStates.PAUSED)
                .event(GameEvents.PAUSE_GAME)

                .and()
                .withExternal()
                .source(GameStates.FORTIFY_PHASE)
                .target(GameStates.PAUSED)
                .event(GameEvents.PAUSE_GAME)

                .and()
                .withExternal()
                .source(GameStates.PAUSED)
                .target(GameStates.REINFORCEMENT_PHASE)
                .event(GameEvents.RESUME_GAME);
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

    // Bean del StateMachineService que falta
    @Bean
    public StateMachineService<GameStates, GameEvents> stateMachineService(
            StateMachineFactory<GameStates, GameEvents> stateMachineFactory) {
        return new DefaultStateMachineService<>(stateMachineFactory);
    }
}