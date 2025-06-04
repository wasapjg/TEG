package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.GameTerritoryEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.repository.GameTerritoryRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameTerritoryServiceImpl implements GameTerritoryService {

    @Autowired
    private GameTerritoryRepository repository;

    @Override
    public List<GameTerritoryEntity> getByOwner(PlayerEntity player) {
        return repository.findByOwner(player);
    }

    @Override
    public List<GameTerritoryEntity> getByContinent(GameEntity game, String continentName) {
        return repository.findTerritoriesByContinentName(game, continentName);
    }

    @Override
    public long countWithMinArmies(PlayerEntity player, int min) {
        return repository.findByOwner(player).stream()
                .filter(t -> t.getArmies() >= min)
                .count();
    }
}
