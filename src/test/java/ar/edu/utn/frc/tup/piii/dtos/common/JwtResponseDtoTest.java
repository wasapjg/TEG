package ar.edu.utn.frc.tup.piii.dtos.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtResponseDtoTest {
    @Test
    void constructor_WithToken_ShouldSetToken() {
        String token = "jwt-token-123";

        JwtResponseDto dto = new JwtResponseDto(token);

        assertThat(dto.getToken()).isEqualTo(token);
    }
}
