package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.player.PlayerResponseDto;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.UserEntity;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;

import java.util.List;
import java.util.Optional;

public interface PlayerService {

    // CRUD básico
    Player save(Player player);
    Optional<Player> findById(Long id);
    List<Player> findAll();
    List<Player> findByGame(Long gameId);
    List<Player> findActivePlayersByGame(Long gameId);
    void deleteById(Long id);

    // Creación de jugadores
    Player createHumanPlayer(UserEntity user, GameEntity game);
    Player createBotPlayer(BotLevel level, BotStrategy strategy, GameEntity game);

    // Gestión de estado
    void updateStatus(Long playerId, PlayerStatus status);
    void eliminatePlayer(Long playerId);

    // Asignaciones
    PlayerColor assignAvailableColor(Long gameId);
    Integer getNextSeatOrder(Long gameId);

    // Validaciones
    boolean canJoinGame(Long userId, Long gameId);
    boolean isPlayerInGame(Long userId, Long gameId);

    // Conversiones
    PlayerResponseDto toResponseDto(Player player);
    Player toModel(PlayerEntity entity);
    PlayerEntity toEntity(Player model);
}