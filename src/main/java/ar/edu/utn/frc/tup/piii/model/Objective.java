package ar.edu.utn.frc.tup.piii.model;

import ar.edu.utn.frc.tup.piii.model.enums.ObjectiveType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Objective {
    private Long id;
    private ObjectiveType type;
    private String description;
    private String targetData;
    private Boolean isCommon;
    private Boolean isAchieved;

    public boolean isAchieved(Game game, Player player) {
        // Implementar lógica de verificación de objetivos
        switch (type) {
            case COMMON:
                return player.getTerritoryCount() >= 30;
            case OCCUPATION:
                return checkOccupationObjective(game, player);
            case DESTRUCTION:
                return checkDestructionObjective(game, player);
            default:
                return false;
        }
    }

    private boolean checkOccupationObjective(Game game, Player player) {
        // Implementar lógica específica para objetivos de ocupación
        return false; // Placeholder
    }

    private boolean checkDestructionObjective(Game game, Player player) {
        // Implementar lógica específica para objetivos de destrucción
        return false; // Placeholder
    }
}