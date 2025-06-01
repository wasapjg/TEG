package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.chat.ChatMessageDto;
import ar.edu.utn.frc.tup.piii.dtos.chat.ChatMessageResponseDto;
import ar.edu.utn.frc.tup.piii.model.ChatMessage;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import java.util.List;
import java.util.Optional;

public interface ChatService {

    // CRUD básico
    ChatMessage save(ChatMessage chatMessage);
    Optional<ChatMessage> findById(Long id);
    List<ChatMessage> findAll();
    void deleteById(Long id);

    // Gestión de mensajes
    ChatMessageResponseDto sendMessage(ChatMessageDto messageDto);
    List<ChatMessageResponseDto> getMessagesByGame(Long gameId);
    List<ChatMessageResponseDto> getRecentMessages(Long gameId, int limit);

    // Mensajes del sistema
    void sendSystemMessage(Game game, String message);
    void notifyPlayerJoined(Game game, Player player);
    void notifyPlayerLeft(Game game, Player player);
    void notifyGameStarted(Game game);
    void notifyTurnChanged(Game game, Player currentPlayer);
    void notifyAttack(Game game, Player attacker, String fromCountry, String toCountry);
    void notifyTerritoryConquered(Game game, Player conqueror, String territory);
    void notifyPlayerEliminated(Game game, Player eliminatedPlayer);
    void notifyGameEnded(Game game, Player winner);

    // Validaciones
    boolean isChatEnabled(Long gameId);
    boolean canPlayerSendMessage(Long playerId, Long gameId);
    boolean validateMessageContent(String content);

    // Moderación
    void mutePlayer(Long playerId, Long gameId);
    void unmutePlayer(Long playerId, Long gameId);
    boolean isPlayerMuted(Long playerId, Long gameId);
}
