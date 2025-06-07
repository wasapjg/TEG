package ar.edu.utn.frc.tup.piii.dtos.country;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TerritoryDto {
    private Long id;
    private String name;
    private String continentName;
    private Integer armies;
    private Double positionX;
    private Double positionY;
}