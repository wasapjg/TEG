package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.model.entity.Player;
import ar.edu.utn.frc.tup.piii.model.entity.User;
import ar.edu.utn.frc.tup.piii.model.entity.Game;
import ar.edu.utn.frc.tup.piii.model.entity.BotProfile;
import ar.edu.utn.frc.tup.piii.model.entity.Objective;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import java.util.List;
import java.util.Optional;

public interface PlayerService {

    // CRUD básico
    Player save(Player player);
    Optional<Player> findById(Long id);
    List<Player> findAll();
    List<Player> findByGame(Game game);
    List<Player> findActivePlayersByGame(Game game);
    void deleteById(Long id);

    // Creación de jugadores
    Player createHumanPlayer(User user, Game game);
    Player createBotPlayer(BotLevel botLevel, Game game);

    // Gestión de estado
    void eliminatePlayer(Long playerId);
    void activatePlayer(Long playerId);
    void updateStatus(Long playerId, PlayerStatus status);
    boolean isEliminated(Long playerId);
    boolean isActive(Long playerId);

    // Objetivos
    void assignObjective(Long playerId, Objective objective);
    boolean hasWon(Long playerId, Game game);
    boolean hasAchievedObjective(Long playerId);

    // Gestión de ejércitos
    void addArmiesToPlace(Long playerId, int armies);
    void removeArmiesToPlace(Long playerId, int armies);
    int getArmiesToPlace(Long playerId);

    // Validaciones
    boolean canPerformAction(Long playerId, Game game);
    boolean isPlayerTurn(Long playerId, Game game);
    boolean belongsToGame(Long playerId, Long gameId);
}