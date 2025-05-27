package ar.edu.utn.frc.tup.piii.dtos.country;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CountryResponseDto {
    private String code;
    private String name;
}
