package ar.edu.utn.frc.tup.piii.dtos.objective;

import ar.edu.utn.frc.tup.piii.model.enums.ObjectiveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectiveResponseDto {
    private Long id;
    private ObjectiveType type;
    private String description;
    private Boolean isCommon;
    private Boolean isAchieved;
    private String progressDescription;
}
