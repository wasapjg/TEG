package ar.edu.utn.frc.tup.piii.service.interfaces;
import ar.edu.utn.frc.tup.piii.model.Game;
import java.util.Optional;

/**
 * Minimal service interface for Game operations needed by GameStateController.
 * Provides basic CRUD operations for Game entities.
 */
public interface GameService {

    /**
     * Finds a game by its ID.
     *
     * @param gameId the ID of the game to find
     * @return the game if found
     * @throws GameNotFoundException if game is not found
     */
    Game findById(Long gameId);

    /**
     * Finds a game by its ID, returning Optional.
     *
     * @param gameId the ID of the game to find
     * @return Optional containing the game if found, empty otherwise
     */
    Optional<Game> findByIdOptional(Long gameId);

    /**
     * Finds a game by its game code.
     *
     * @param gameCode the game code to search for
     * @return the game if found
     * @throws GameNotFoundException if game is not found
     */
    Game findByGameCode(String gameCode);

    /**
     * Saves or updates a game.
     *
     * @param game the game to save
     * @return the saved game
     */
    Game save(Game game);

    /**
     * Checks if a game exists by ID.
     *
     * @param gameId the ID to check
     * @return true if game exists, false otherwise
     */
    boolean existsById(Long gameId);
}
