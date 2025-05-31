package ar.edu.utn.frc.tup.piii.statemachine;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.GamePhase;
import ar.edu.utn.frc.tup.piii.model.enums.GameStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GameStateMachineService {

    private final StateMachineService<GameStates, GameEvents> stateMachineService;

    @Autowired
    public GameStateMachineService(StateMachineService<GameStates, GameEvents> stateMachineService) {
        this.stateMachineService = stateMachineService;
    }

    // Método para obtener una state machine para un juego específico
    public StateMachine<GameStates, GameEvents> getStateMachine(String gameId) {
        return stateMachineService.acquireStateMachine(gameId, true);
    }

    // Método para liberar una state machine
    public void releaseStateMachine(String gameId) {
        stateMachineService.releaseStateMachine(gameId);
    }

    // Métodos de acción para las transiciones
    public void onGameStart(StateContext<GameStates, GameEvents> context) {
        log.info("Iniciando juego - distribuyendo países");
        Game game = getGameFromContext(context);
        if (game != null) {
            game.setStatus(GameStatus.IN_PROGRESS);
            game.setCurrentPhase(GamePhase.SETUP);
        }
    }

    public void onCountriesDistributed(StateContext<GameStates, GameEvents> context) {
        log.info("Países distribuidos - iniciando colocación inicial de ejércitos");
        Game game = getGameFromContext(context);
        if (game != null) {
            // Lógica para colocación inicial de ejércitos
        }
    }

    public void onInitialArmiesPlaced(StateContext<GameStates, GameEvents> context) {
        log.info("Ejércitos iniciales colocados - iniciando primer turno");
        Game game = getGameFromContext(context);
        if (game != null) {
            game.setCurrentPhase(GamePhase.REINFORCEMENT);
        }
    }

    public void onTurnStart(StateContext<GameStates, GameEvents> context) {
        log.info("Iniciando turno");
        Game game = getGameFromContext(context);
        if (game != null) {
            game.setCurrentPhase(GamePhase.REINFORCEMENT);
            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer != null) {
                // Calcular ejércitos de refuerzo
                int reinforcements = calculateReinforcements(game, currentPlayer);
                currentPlayer.setArmiesToPlace(reinforcements);
            }
        }
    }

    public void onReinforcement(StateContext<GameStates, GameEvents> context) {
        log.info("Fase de refuerzos completada - iniciando fase de ataque");
        Game game = getGameFromContext(context);
        if (game != null) {
            game.setCurrentPhase(GamePhase.ATTACK);
        }
    }

    public void onAttackCompleted(StateContext<GameStates, GameEvents> context) {
        log.info("Fase de ataque completada - iniciando fase de reagrupación");
        Game game = getGameFromContext(context);
        if (game != null) {
            game.setCurrentPhase(GamePhase.FORTIFY);
        }
    }

    public void onFortifyCompleted(StateContext<GameStates, GameEvents> context) {
        log.info("Fase de reagrupación completada - finalizando turno");
        Game game = getGameFromContext(context);
        if (game != null) {
            game.setCurrentPhase(GamePhase.END_TURN);
        }
    }

    public void onNextTurn(StateContext<GameStates, GameEvents> context) {
        log.info("Iniciando siguiente turno");
        Game game = getGameFromContext(context);
        if (game != null) {
            game.nextPlayer();
            game.nextTurn();
            game.setCurrentPhase(GamePhase.REINFORCEMENT);
        }
    }

    public void onGameWon(StateContext<GameStates, GameEvents> context) {
        log.info("Juego terminado - hay un ganador");
        Game game = getGameFromContext(context);
        if (game != null) {
            game.setStatus(GameStatus.FINISHED);
        }
    }

    public void onGamePaused(StateContext<GameStates, GameEvents> context) {
        log.info("Juego pausado");
        Game game = getGameFromContext(context);
        if (game != null) {
            game.setStatus(GameStatus.PAUSED);
        }
    }

    public void onGameResumed(StateContext<GameStates, GameEvents> context) {
        log.info("Juego reanudado");
        Game game = getGameFromContext(context);
        if (game != null) {
            game.setStatus(GameStatus.IN_PROGRESS);
        }
    }

    public void onTimeout(StateContext<GameStates, GameEvents> context) {
        log.info("Timeout - avanzando automáticamente de fase");
        Game game = getGameFromContext(context);
        if (game != null) {
            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer != null) {
                switch (game.getCurrentPhase()) {
                    case REINFORCEMENT:
                        autoPlaceReinforcements(game, currentPlayer);
                        break;
                    case ATTACK:
                        // No hacer nada, pasar a siguiente fase
                        break;
                    case FORTIFY:
                        // No reagrupar
                        break;
                }
            }
        }
    }

    private Game getGameFromContext(StateContext<GameStates, GameEvents> context) {
        // Obtener el juego del contexto
        Object game = context.getExtendedState().getVariables().get("game");
        return game instanceof Game ? (Game) game : null;
    }

    private int calculateReinforcements(Game game, Player player) {
        int territories = player.getTerritoryCount();
        int baseReinforcements = Math.max(3, territories / 2);

        // Agregar bonus por continentes controlados
        int continentBonus = calculateContinentBonus(game, player);

        return baseReinforcements + continentBonus;
    }

    private int calculateContinentBonus(Game game, Player player) {
        // TODO lógica de bonus por continentes
        return 0; // Placeholder
    }

    private void autoPlaceReinforcements(Game game, Player player) {
        // TODO colocación automática de ejércitos
        log.info("Colocando ejércitos automáticamente para {}", player.getDisplayName());
    }
}