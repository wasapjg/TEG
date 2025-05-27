package ar.edu.utn.frc.tup.piii.dtos.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameCreationDto {
    @JsonProperty("CreatedByUserId")
    private Long createdByUserId;
    private Integer maxPlayers;
    private Integer turnTimeLimit; // minutos
    private Boolean chatEnabled;
    private Boolean pactsAllowed;

}
