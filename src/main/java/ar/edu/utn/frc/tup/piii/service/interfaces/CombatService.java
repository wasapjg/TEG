package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.dtos.game.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.game.CombatResultDto;
import ar.edu.utn.frc.tup.piii.model.Territory;
import java.util.List;

public interface CombatService {

    /**
     * Ejecuta un combate completo entre dos territorios.
     */
    CombatResultDto performCombat(String gameCode, AttackDto attackDto);

    /**
     * Obtiene todos los territorios que un jugador puede usar para atacar.
     */
    List<Territory> getAttackableTerritoriesForPlayer(String gameCode, Long playerId);

    /**
     * Obtiene los territorios enemigos que un territorio específico puede atacar.
     */
    List<Territory> getTargetsForTerritory(String gameCode, Long territoryId, Long playerId);
}