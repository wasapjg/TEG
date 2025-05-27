
package ar.edu.utn.frc.tup.piii.dtos.player;

import ar.edu.utn.frc.tup.piii.model.entity.Player;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PlayerResponseDto {
    private Long id;
    private String username;
    private String status;
    private String color;


public static PlayerResponseDto fromEntity(Player player) {
    String username = player.getUser() != null
            ? player.getUser().getUsername()
            : player.getBotProfile() != null
            ? player.getBotProfile().getBotName()
            : "Unknown";

    return PlayerResponseDto.builder()
            .id(player.getId())
            .username(username)
            .status(player.getStatus() != null ? player.getStatus().name() : null)
            .color(player.getColor() != null ? player.getColor().name() : null)
            .build();
}
}