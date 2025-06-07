package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameStateService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameStateServiceImpl implements GameStateService {

    List<GameState> noGameList = List.of(GameState.WAITING_FOR_PLAYERS, GameState.PAUSED, GameState.FINISHED);
    // Cambiar estado del juego
    @Override
    public boolean changeGameState(Game game, GameState newState) {
        GameState currentState = game.getState();

        switch (currentState) {
            case WAITING_FOR_PLAYERS:
                if (newState == GameState.REINFORCEMENT_5 && game.canStart()) {
                    game.setState(newState);
                    game.setCurrentPhase(TurnPhase.REINFORCEMENT);
                    return true;
                }
                break;

            case REINFORCEMENT_5: // 5 tropas, 3 tropas, atacar // primera ronda de tod menos reinforcment despues de la colacion de
                if(newState == GameState.REINFORCEMENT_3) {
                    game.setState(newState);
                    game.setCurrentPhase(TurnPhase.REINFORCEMENT);
                    return true;
                }

            case REINFORCEMENT_3: // 5 tropas, 3 tropas, atacar // primera ronda de tod menos reinforcment despues de la colacion de
                if(newState == GameState.HOSTILITY_ONLY) {
                    game.setState(newState);
                    game.setCurrentPhase(TurnPhase.REINFORCEMENT);
                    return true;
                }

            case HOSTILITY_ONLY: // 5 tropas, 3 tropas, atacar // primera ronda de tod menos reinforcment despues de la colacion de
                if(newState == GameState.HOSTILITY_ONLY) {
                    game.setState(newState);
                    game.setCurrentPhase(TurnPhase.ATTACK);
                    return true;
                }

                // 4 tipos de rondas // reinforcment 5 // reinforcment 3 // sin reinforcment // ostilidades

            case NORMAL_PLAY: // 5 tropas, 3 tropas, atacar // primera ronda de tod menos reinforcment despues de la colacion de
                if (newState == GameState.PAUSED ||
                        newState == GameState.FINISHED) {
                    game.setState(newState);
                    return true;
                }
                break;

            case PAUSED:
                if (newState == GameState.NORMAL_PLAY) {
                    game.setState(newState);
                    return true;
                }
                break;

            case FINISHED:
                // No se puede cambiar desde FINISHED
                return false;
        }

        return false; // Transición no válida
    }

    // Cambiar fase del turno
    @Override
    public boolean changeTurnPhase(Game game, TurnPhase newPhase) {
        if (noGameList.contains(game.getState())) {
            return false;
        }

        TurnPhase currentPhase = game.getCurrentPhase();

        switch (currentPhase) {
            case REINFORCEMENT:
                if (newPhase == TurnPhase.ATTACK || (game.getState().equals(GameState.REINFORCEMENT_5) && newPhase == TurnPhase.END_TURN)
                        || (game.getState().equals(GameState.REINFORCEMENT_3) && newPhase == TurnPhase.END_TURN)) {
                    game.setCurrentPhase(newPhase);
                    return true;
                }
                break;

            case ATTACK:
                if (newPhase == TurnPhase.FORTIFY ) {
                    game.setCurrentPhase(newPhase);
                    return true;
                }
                break;

            case FORTIFY:
                if (newPhase == TurnPhase.END_TURN) {
                    game.setCurrentPhase(newPhase);
                    return true;
                }
                break;

            // pedir carta
            case CLAIM_CARD:
                if (newPhase == TurnPhase.END_TURN) {
                    game.setCurrentPhase(newPhase);
                    return true;
                }
                break;

            case END_TURN:
                if (newPhase == TurnPhase.REINFORCEMENT) {
                    // Pasar al siguiente jugador
                    nextPlayer(game);
                    game.setCurrentPhase(newPhase);
                    return true;
                }
                break;
        }

        return false;
    }

    // Iniciar juego
    @Override
    public boolean startGame(Game game) {
        if (game.getState() == GameState.WAITING_FOR_PLAYERS && game.canStart()) {
            return changeGameState(game, GameState.REINFORCEMENT_5);
        }
        return false;
    }

    // Pausar juego
    @Override
    public boolean pauseGame(Game game) {
        return changeGameState(game, GameState.PAUSED);
    }

    // Reanudar juego
    @Override
    public boolean resumeGame(Game game) {
        if (game.getState() == GameState.PAUSED) {
            return changeGameState(game, GameState.NORMAL_PLAY);
        }
        return false;
    }

    // Finalizar juego
    @Override
    public boolean finishGame(Game game) {
        return changeGameState(game, GameState.FINISHED);
    }

    // Siguiente turno
    @Override
    public void nextTurn(Game game) {
        nextPlayer(game);
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);
        game.setCurrentTurn(game.getCurrentTurn() + 1);
    }

    // Siguiente jugador
    private void nextPlayer(Game game) {
        int nextIndex = (game.getCurrentPlayerIndex() + 1) % game.getPlayers().size();
        game.setCurrentPlayerIndex(nextIndex);

        // Saltar jugadores eliminados
        Player nextPlayer = game.getPlayers().get(nextIndex);
        if (nextPlayer.isEliminated()) {
            nextPlayer(game); // Recursivo hasta encontrar jugador activo
        }
    }

    // Verificar si se puede realizar una acción
    @Override
    public boolean canPerformAction(Game game, String action) {
        if (noGameList.contains(game.getState())) {
            return false;
        }

        TurnPhase currentPhase = game.getCurrentPhase();

        switch (action.toLowerCase()) {
            case "reinforce":
                return currentPhase == TurnPhase.REINFORCEMENT;

            case "attack":
                return currentPhase == TurnPhase.ATTACK;

            case "fortify":
                return currentPhase == TurnPhase.FORTIFY;

            case "end_turn":
                return currentPhase == TurnPhase.END_TURN;

            case "skip_attack":
                return currentPhase == TurnPhase.ATTACK;

            case "skip_fortify":
                return currentPhase == TurnPhase.FORTIFY;

            default:
                return false;
        }
    }

    // Obtener acciones disponibles
    @Override
    public String[] getAvailableActions(Game game) {
        if (noGameList.contains(game.getState())) {
            return new String[0];
        }

        switch (game.getCurrentPhase()) {
            case REINFORCEMENT:
                return new String[]{"reinforce", "next_phase"};

            case ATTACK:
                return new String[]{"attack", "skip_attack", "next_phase"};

            case FORTIFY:
                return new String[]{"fortify", "skip_fortify", "end_turn"};

            case END_TURN:
                return new String[]{"next_turn"};

            default:
                return new String[0];
        }
    }
    @Override
    public boolean isGameActive(Game game) {
        return !noGameList.contains(game.getState());
    }

    @Override
    public boolean isPlayerTurn(Game game, Long playerId) {
        if (!isGameActive(game)) {
            return false;
        }
        Player currentPlayer = game.getCurrentPlayer();
        return currentPlayer != null && currentPlayer.getId().equals(playerId);
    }

    @Override
    public String getCurrentPhaseDescription(Game game) {
        if (!isGameActive(game)) {
            return "Game is not active";
        }

        switch (game.getCurrentPhase()) {
            case REINFORCEMENT:
                return "Place your reinforcement armies on your territories";
            case ATTACK:
                return "Attack enemy territories or skip to fortification";
            case FORTIFY:
                return "Move armies between your connected territories or end turn";
            case END_TURN:
                return "Confirm to end your turn";
            default:
                return "Unknown phase";
        }
    }

    @Override
    public boolean isValidPhaseTransition(TurnPhase currentPhase, TurnPhase targetPhase) {
        switch (currentPhase) {
            case REINFORCEMENT:
                return targetPhase == TurnPhase.ATTACK;
            case ATTACK:
                return targetPhase == TurnPhase.FORTIFY || targetPhase == TurnPhase.END_TURN;
            case FORTIFY:
                return targetPhase == TurnPhase.END_TURN;
            case END_TURN:
                return targetPhase == TurnPhase.REINFORCEMENT;
            default:
                return false;
        }
    }

    @Override
    public boolean isValidStateTransition(GameState currentState, GameState targetState) {
        switch (currentState) {
            case WAITING_FOR_PLAYERS:
                return targetState == GameState.REINFORCEMENT_5;
            case REINFORCEMENT_5:
                return  targetState == GameState.REINFORCEMENT_3 ;
            case REINFORCEMENT_3:
                return targetState == GameState.HOSTILITY_ONLY;
            case HOSTILITY_ONLY:
                return targetState == GameState.NORMAL_PLAY;
            case NORMAL_PLAY:
                return targetState == GameState.PAUSED || targetState == GameState.FINISHED;
            case FINISHED:
                return false; // No se puede cambiar desde FINISHED
            default:
                return false;
        }
    }
}