package ar.edu.utn.frc.tup.piii.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationErrorDto {
    private String message;
    private List<String> errors;
    private String timestamp;
}