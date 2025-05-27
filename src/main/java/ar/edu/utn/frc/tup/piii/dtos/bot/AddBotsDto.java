package ar.edu.utn.frc.tup.piii.dtos.bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddBotsDto {
    private String gameCode;
    private Integer count;
    private String botLevel;     // e.g., "NOVICE"
    private String botStrategy;  // e.g., "DEFENSIVE"
}
