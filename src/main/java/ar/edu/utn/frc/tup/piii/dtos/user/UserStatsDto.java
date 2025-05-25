package ar.edu.utn.frc.tup.piii.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsDto {
    private String username;
    private Integer totalGames;
    private Integer wins;
    private Integer losses;
    private Double winRate;
    private Integer bestStreak;
    private Integer currentStreak;
    private String favoriteStrategy;
    private Integer totalPlayTime; // en minutos
}