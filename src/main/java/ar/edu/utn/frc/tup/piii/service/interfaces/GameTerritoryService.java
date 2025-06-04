package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.GameTerritoryEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;

import java.util.List;

public interface GameTerritoryService {
    List<GameTerritoryEntity> getByOwner(PlayerEntity player);
    List<GameTerritoryEntity> getByContinent(GameEntity game, String continentName);
    long countWithMinArmies(PlayerEntity player, int min);
}

