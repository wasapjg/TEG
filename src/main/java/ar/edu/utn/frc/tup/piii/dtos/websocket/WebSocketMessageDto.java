package ar.edu.utn.frc.tup.piii.dtos.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketMessageDto {
    private String type; // "GAME_UPDATE", "CHAT", "PLAYER_ACTION", etc.
    private Long gameId;
    private Object payload;
    private String timestamp;
}
