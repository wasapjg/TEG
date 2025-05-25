package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.game.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.game.CombatResultDto;
import ar.edu.utn.frc.tup.piii.model.entity.CombatResult;
import ar.edu.utn.frc.tup.piii.model.entity.Country;
import ar.edu.utn.frc.tup.piii.model.entity.Game;
import ar.edu.utn.frc.tup.piii.model.entity.Player;
import java.util.List;

public interface CombatService {

    // Gestión de combates
    CombatResultDto executeAttack(AttackDto attackDto);
    CombatResult simulateCombat(Country attacker, Country defender, int attackerDice, int defenderDice);
    List<Integer> rollDice(int numberOfDice);
    CombatResult resolveCombat(List<Integer> attackerDice, List<Integer> defenderDice,
                               Country attackerCountry, Country defenderCountry);

    // Validaciones de combate
    boolean canAttack(Country from, Country to, Player attacker);
    boolean hasEnoughArmies(Country country, int requiredArmies);
    boolean areCountriesAdjacent(Country country1, Country country2);
    boolean isValidAttack(Long gameId, Long attackerCountryId, Long defenderCountryId, Long playerId);

    // Cálculos de probabilidad
    double calculateAttackProbability(int attackerArmies, int defenderArmies);
    double calculateWinProbability(int attackerDice, int defenderDice);

    // Gestión de conquistas
    void conquestTerritory(Country conqueredCountry, Player conqueror, int movingArmies);
    boolean wouldPlayerBeEliminated(Player player, Country lostCountry);
    void checkPlayerElimination(Game game, Player player);

    // Historial de combates
    List<CombatResult> getCombatHistory(Long gameId);
    List<CombatResult> getCombatHistoryByPlayer(Long playerId);

    // Estadísticas de combate
    int getTotalAttacks(Long gameId);
    int getSuccessfulAttacks(Long playerId);
    int getFailedAttacks(Long playerId);
    double getAttackSuccessRate(Long playerId);
}
