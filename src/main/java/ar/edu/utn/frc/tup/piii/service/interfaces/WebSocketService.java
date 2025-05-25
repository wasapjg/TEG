package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.websocket.WebSocketMessageDto;
import ar.edu.utn.frc.tup.piii.model.entity.Game;
import ar.edu.utn.frc.tup.piii.model.entity.Player;

public interface WebSocketService {

    // Envío de mensajes
    void sendToGame(Long gameId, WebSocketMessageDto message);
    void sendToPlayer(Long playerId, WebSocketMessageDto message);
    void sendToAllPlayers(WebSocketMessageDto message);
    void broadcastGameUpdate(Game game);

    // Notificaciones específicas del juego
    void notifyTurnChange(Game game, Player currentPlayer);
    void notifyAttackResult(Game game, Object combatResult);
    void notifyTerritoryConquest(Game game, Player conqueror, String territory);
    void notifyPlayerElimination(Game game, Player eliminatedPlayer);
    void notifyGameEnd(Game game, Player winner);
    void notifyGameStart(Game game);

    // Gestión de conexiones
    void addPlayerConnection(Long playerId, String sessionId);
    void removePlayerConnection(Long playerId, String sessionId);
    boolean isPlayerConnected(Long playerId);
    int getConnectedPlayersCount(Long gameId);

    // Chat en tiempo real
    void broadcastChatMessage(Long gameId, Object chatMessage);

    // Gestión de estado
    void sendGameState(Long gameId, Long playerId);
    void syncGameState(Game game);
}
