package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.game.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.game.FortifyDto;
import ar.edu.utn.frc.tup.piii.dtos.game.GameCreationDto;
import ar.edu.utn.frc.tup.piii.dtos.game.ReinforcementDto;
import ar.edu.utn.frc.tup.piii.model.entity.*;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import ar.edu.utn.frc.tup.piii.model.enums.GamePhase;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GameService {

    // CRUD básico
    Game save(Game game);

    Optional<Game> findById(Long id);

    List<Game> findAll();

    List<Game> findActiveGames();

    List<Game> findGamesByPlayer(User user);

    void deleteById(Long id);

    // Creación y configuración
    Game createGame(GameCreationDto dto);

    Game joinGame(String gameCode, Long userId);


    Game addBots(String gameCode, int count, String level, String strategy);

    Game kickPlayer(String gameCode, Long playerId);

    boolean hasSlot(Long gameId);

    void setGameOpen(Long gameId, boolean open);

    // Gestión del juego
    void startGame(Long gameId);

    void endGame(Long gameId);

    void nextTurn(Long gameId);

    void nextPhase(Long gameId);

    // Acciones de juego
    CombatResult performAttack(Long gameId, AttackDto attackDto);

    void performReinforcement(Long gameId, ReinforcementDto reinforcementDto);

    void performFortify(Long gameId, FortifyDto fortifyDto);

    void tradeCards(Long gameId, Long playerId, List<Card> cards);

    // Estado del juego
    boolean isGameOver(Long gameId);

    Player getWinner(Long gameId);

    Player getCurrentPlayer(Long gameId);

    GamePhase getCurrentPhase(Long gameId);

    int getCurrentTurn(Long gameId);

    // Guardado y carga
    void saveGameSnapshot(Long gameId);

    void loadGameSnapshot(Long gameId, Long snapshotId);

    void pauseGame(Long gameId);

    void resumeGame(Long gameId);

    // Validaciones
    boolean canStartGame(Long gameId);

    boolean isValidAttack(Long gameId, Country from, Country to, Long playerId);

    boolean isValidReinforcement(Long gameId, Map<Country, Integer> reinforcements, Long playerId);

    boolean isValidFortify(Long gameId, Country from, Country to, int armies, Long playerId);
}


