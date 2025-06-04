package ar.edu.utn.frc.tup.piii.model;

import ar.edu.utn.frc.tup.piii.model.enums.ObjectiveType;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

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



//    private boolean checkOccupationObjective(Game game, Player player) {
//        //TODO: Implementar lógica específica para objetivos de ocupación
//        return false; // Placeholder
//    }
//
//    private boolean checkDestructionObjective(Game game, Player player) {
//        // TODO: Implementar lógica específica para objetivos de destrucción
//        return false; // Placeholder
//    }

    public List<String> getTargetContinents() {
        if (type == ObjectiveType.OCCUPATION && targetData != null && !targetData.isBlank()) {
            return List.of(targetData.split("\\s*,\\s*"));
        }
        return List.of();
    }

    public PlayerColor getTargetColor() {
        if (type == ObjectiveType.DESTRUCTION && targetData != null) {
            try {
                return PlayerColor.valueOf(targetData.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

}