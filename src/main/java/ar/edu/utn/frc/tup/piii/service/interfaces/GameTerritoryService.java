package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.GameTerritoryEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.model.Territory;

import java.util.List;

public interface GameTerritoryService {
    List<GameTerritoryEntity> getByOwner(PlayerEntity player);
    List<GameTerritoryEntity> getByContinent(GameEntity game, String continentName);
    long countWithMinArmies(PlayerEntity player, int min);

    Territory getTerritoryByGameAndCountry(Long gameId, Long countryId);

    void addArmiesToTerritory(Long gameId, Long countryId, Integer armies);

    List<Territory> getAllAvailableTerritories();

    void assignTerritoryToPlayer(Long id, Long id1, Long id2, int i);

    List<Territory> getTerritoriesByOwner(Long id, Long playerId);
}

