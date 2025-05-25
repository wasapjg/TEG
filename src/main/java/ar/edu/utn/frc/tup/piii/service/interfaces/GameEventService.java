package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.event.GameEventDto;
import ar.edu.utn.frc.tup.piii.model.entity.GameEvent;
import ar.edu.utn.frc.tup.piii.model.entity.Game;
import ar.edu.utn.frc.tup.piii.model.entity.Player;
import ar.edu.utn.frc.tup.piii.model.enums.EventType;
import java.util.List;
import java.util.Optional;

public interface GameEventService {

    // CRUD básico
    GameEvent save(GameEvent gameEvent);
    Optional<GameEvent> findById(Long id);
    List<GameEvent> findAll();
    List<GameEvent> findByGame(Game game);
    List<GameEvent> findByPlayer(Player player);
    List<GameEvent> findByEventType(EventType eventType);
    void deleteById(Long id);

    // Creación de eventos
    GameEvent createEvent(Game game, Player actor, EventType type, String data);
    GameEvent recordAttack(Game game, Player attacker, String fromCountry, String toCountry, String result);
    GameEvent recordTerritoryConquest(Game game, Player conqueror, String territory);
    GameEvent recordReinforcement(Game game, Player player, String reinforcements);
    GameEvent recordFortification(Game game, Player player, String fortification);
    GameEvent recordCardTrade(Game game, Player player, String cards);
    GameEvent recordPlayerElimination(Game game, Player eliminatedPlayer);
    GameEvent recordGameStart(Game game);
    GameEvent recordGameEnd(Game game, Player winner);
    GameEvent recordTurnStart(Game game, Player player);
    GameEvent recordTurnEnd(Game game, Player player);

    // Consultas específicas
    List<GameEventDto> getGameHistory(Long gameId);
    List<GameEventDto> getPlayerHistory(Long playerId);
    List<GameEventDto> getTurnHistory(Long gameId, int turnNumber);
    List<GameEventDto> getRecentEvents(Long gameId, int limit);

    // Estadísticas
    int getAttackCount(Long gameId);
    int getAttackCountByPlayer(Long playerId);
    int getConquestCount(Long gameId);
    int getConquestCountByPlayer(Long playerId);
}
