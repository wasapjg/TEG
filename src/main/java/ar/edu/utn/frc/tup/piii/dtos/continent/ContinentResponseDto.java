package ar.edu.utn.frc.tup.piii.dtos.continent;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContinentResponseDto {
    private String code;
    private String name;
}