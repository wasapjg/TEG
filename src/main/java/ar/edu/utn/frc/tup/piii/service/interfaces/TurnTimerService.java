package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.model.entity.TurnTimer;
import ar.edu.utn.frc.tup.piii.model.entity.Game;
import ar.edu.utn.frc.tup.piii.model.entity.Player;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TurnTimerService {

    // CRUD básico
    TurnTimer save(TurnTimer turnTimer);
    Optional<TurnTimer> findById(Long id);
    List<TurnTimer> findAll();
    Optional<TurnTimer> findActiveTimerByGame(Long gameId);
    void deleteById(Long id);

    // Gestión de temporizadores
    TurnTimer startTimer(Game game, Player player);
    void cancelTimer(Long timerId);
    void cancelActiveTimer(Long gameId);
    boolean hasTimedOut(Long timerId);
    int getRemainingTime(Long timerId); // en segundos

    // Validaciones
    boolean isTimerActive(Long gameId);
    boolean isTimeEnabled(Game game);

    // Acciones automáticas por timeout
    void handleTimeout(Long gameId);
    void autoEndTurn(Game game);
    void autoPlaceReinforcements(Game game, Player player);

    // Configuración
    void setTimeLimit(Game game, int minutes);
    int getTimeLimit(Game game);

    // Estadísticas
    List<TurnTimer> getTimerHistory(Long gameId);
    int getAverageResponseTime(Long playerId);
    int getTimeoutCount(Long playerId);
}
