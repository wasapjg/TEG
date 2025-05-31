package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.game.GameStatisticsDto;
import ar.edu.utn.frc.tup.piii.dtos.user.UserStatsDto;

import java.util.Map;

public interface StatisticsService {

    // Estadísticas de usuario
    UserStatsDto getUserStatistics(Long userId);
    int getTotalGamesPlayed(Long userId);
    int getWins(Long userId);
    int getLosses(Long userId);
    double getWinRate(Long userId);
    int getBestWinStreak(Long userId);
    int getCurrentWinStreak(Long userId);

    // Estadísticas de juego
    GameStatisticsDto getGameStatistics(Long gameId);
    int getTotalTurns(Long gameId);
    int getTotalAttacks(Long gameId);
    int getTerritoriesConquered(Long gameId);
    String getMostAggressivePlayer(Long gameId);
    String getMostDefensivePlayer(Long gameId);

    // Estadísticas de jugador en juego específico
    int getPlayerAttacks(Long playerId, Long gameId);
    int getPlayerConquests(Long playerId, Long gameId);
    int getPlayerDefenses(Long playerId, Long gameId);
    double getPlayerAttackSuccessRate(Long playerId, Long gameId);

    // Rankings globales
    Map<String, Object> getGlobalRankings();
    Map<String, Object> getTopPlayers(int limit);
    Map<String, Object> getMostActiveUsers(int limit);

    // Estadísticas generales del sistema
    long getTotalUsers();
    long getTotalGames();
    long getActiveGames();
    double getAverageGameDuration();
    Map<String, Integer> getGamesByStatus();
}
