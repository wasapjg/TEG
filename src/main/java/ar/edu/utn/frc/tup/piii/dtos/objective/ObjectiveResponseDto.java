package ar.edu.utn.frc.tup.piii.dtos.objective;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectiveResponseDto {
    private Long id;
    private String name;
    private String description;
    private String status;
    private String createdAt;
    private String updatedAt;
}
