package ar.edu.utn.frc.tup.piii.dtos.common;

public class JwtResponseDto {
    private String token;
    public JwtResponseDto(String token) { this.token = token; }
    public String getToken() { return token; }
}
