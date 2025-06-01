package ar.edu.utn.frc.tup.piii.dtos.bot;

import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddBotsDto {
    private String gameCode;
    private Integer count;
    private BotLevel botLevel;   // e.g. "EASY"
    private BotStrategy botStrategy; // e.g. "DEFENSIVE"
}