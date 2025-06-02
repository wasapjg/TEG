package ar.edu.utn.frc.tup.piii.service.interfaces;
import ar.edu.utn.frc.tup.piii.dtos.bot.AddBotsDto;
import ar.edu.utn.frc.tup.piii.dtos.game.GameCreationDto;
import ar.edu.utn.frc.tup.piii.dtos.game.JoinGameDto;
import ar.edu.utn.frc.tup.piii.exceptions.*;
import ar.edu.utn.frc.tup.piii.model.Game;
import jakarta.transaction.Transactional;

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

    /**
     * Creates a new game with the specified configuration.
     *
     * @param dto game creation data transfer object
     * @return the created game
     * @throws UserNotFoundException if the creator user is not found
     * @throws GameCodeAlreadyExistsException if generated code already exists
     */
    @Transactional
    Game createNewGame(GameCreationDto dto);

    /**
     * Allows a user to join an existing game.
     *
     * @param dto join game data transfer object
     * @return the updated game
     * @throws GameNotFoundException if game is not found
     * @throws GameFullException if game is already full
     * @throws InvalidGameStateException if game is not in WAITING_FOR_PLAYERS state
     * @throws UserNotFoundException if user is not found
     * @throws ColorNotAvailableException if no colors are available
     */
    @Transactional
    Game joinGame(JoinGameDto dto);

    /**
     * Adds bot players to an existing game.
     *
     * @param dto add bots data transfer object
     * @return the updated game
     * @throws GameNotFoundException if game is not found
     * @throws GameFullException if adding bots would exceed capacity
     * @throws InvalidGameStateException if game is not in WAITING_FOR_PLAYERS state
     * @throws BotProfileNotFoundException if specified bot profile is not found
     */
    @Transactional
    Game addBotsToGame(AddBotsDto dto);

    /**
     * Starts a game, changing its state from WAITING_FOR_PLAYERS to IN_PROGRESS.
     *
     * @param gameCode the code of the game to start
     * @return the updated game
     * @throws GameNotFoundException if game is not found
     * @throws InvalidGameStateException if game is not in WAITING_FOR_PLAYERS state
     */
    @Transactional
    Game startGame(String gameCode);
}
