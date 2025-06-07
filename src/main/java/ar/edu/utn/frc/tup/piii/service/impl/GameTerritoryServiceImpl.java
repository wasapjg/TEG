package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.GameTerritoryEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.entities.CountryEntity;
import ar.edu.utn.frc.tup.piii.exceptions.GameNotFoundException;
import ar.edu.utn.frc.tup.piii.exceptions.PlayerNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.TerritoryMapper;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.repository.*;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación completa del servicio de territorios del juego.
 * Solo usa GameTerritoryRepository y repositorios relacionados directamente con territorios.
 */
@Service
public class GameTerritoryServiceImpl implements GameTerritoryService {

    // Repositorios específicos de territorios
    @Autowired
    private GameTerritoryRepository gameTerritoryRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    // Mapper para conversión
    @Autowired
    private TerritoryMapper territoryMapper;

    @Override
    public List<GameTerritoryEntity> getByOwner(PlayerEntity player) {
        return gameTerritoryRepository.findByOwner(player);
    }

    @Override
    public List<GameTerritoryEntity> getByContinent(GameEntity game, String continentName) {
        return gameTerritoryRepository.findTerritoriesByContinentName(game, continentName);
    }

    @Override
    public long countWithMinArmies(PlayerEntity player, int min) {
        return gameTerritoryRepository.findByOwner(player).stream()
                .filter(t -> t.getArmies() >= min)
                .count();
    }

    /**
     * Obtiene todos los territorios disponibles para repartir.
     */
    public List<Territory> getAllAvailableTerritories() {
        return countryRepository.findAll().stream()
                .map(this::convertCountryToTerritory)
                .collect(Collectors.toList());
    }

    /**
     * Asigna un territorio a un jugador con una cantidad inicial de ejércitos.
     */
    @Transactional
    public void assignTerritoryToPlayer(Long gameId, Long countryId, Long playerId, int initialArmies) {
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

        CountryEntity country = countryRepository.findById(countryId)
                .orElseThrow(() -> new IllegalArgumentException("Country not found with id: " + countryId));

        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + playerId));

        // Verificar si ya existe el territorio en el juego
        Optional<GameTerritoryEntity> existingTerritory =
                gameTerritoryRepository.findByGameAndCountry(game, country);

        if (existingTerritory.isPresent()) {
            // Actualizar el propietario y ejércitos
            GameTerritoryEntity territory = existingTerritory.get();
            territory.setOwner(player);
            territory.setArmies(initialArmies);
            gameTerritoryRepository.save(territory);
        } else {
            // Crear nuevo territorio
            GameTerritoryEntity newTerritory = new GameTerritoryEntity();
            newTerritory.setGame(game);
            newTerritory.setCountry(country);
            newTerritory.setOwner(player);
            newTerritory.setArmies(initialArmies);
            gameTerritoryRepository.save(newTerritory);
        }
    }

    /**
     * Obtiene un territorio específico por juego y país.
     */
    public Territory getTerritoryByGameAndCountry(Long gameId, Long countryId) {
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

        CountryEntity country = countryRepository.findById(countryId)
                .orElseThrow(() -> new IllegalArgumentException("Country not found with id: " + countryId));

        return gameTerritoryRepository.findByGameAndCountry(game, country)
                .map(this::convertToTerritory)
                .orElse(null);
    }

    /**
     * Añade ejércitos a un territorio específico.
     */
    @Override
    @Transactional
    public void addArmiesToTerritory(Long gameId, Long countryId, Integer armies) {
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

        CountryEntity country = countryRepository.findById(countryId)
                .orElseThrow(() -> new IllegalArgumentException("Country not found with id: " + countryId));

        GameTerritoryEntity territory = gameTerritoryRepository.findByGameAndCountry(game, country)
                .orElseThrow(() -> new IllegalArgumentException("Territory not found in game"));

        territory.setArmies(territory.getArmies() + armies);
        gameTerritoryRepository.save(territory);
    }

    /**
     * Obtiene todos los territorios controlados por un jugador en un juego.
     */
    public List<Territory> getTerritoriesByOwner(Long gameId, Long playerId) {
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + playerId));

        return gameTerritoryRepository.findByGameAndOwner(game, player).stream()
                .map(this::convertToTerritory)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los territorios de un juego.
     */
    public List<Territory> getAllTerritoriesInGame(Long gameId) {
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

        return gameTerritoryRepository.findByGame(game).stream()
                .map(this::convertToTerritory)
                .collect(Collectors.toList());
    }

    /**
     * Verifica si un jugador controla completamente un continente.
     */
    public boolean doesPlayerControlContinent(Long gameId, Long playerId, String continentName) {
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + playerId));

        List<GameTerritoryEntity> continentTerritories =
                gameTerritoryRepository.findTerritoriesByContinentName(game, continentName);

        return continentTerritories.stream()
                .allMatch(territory -> territory.getOwner().equals(player));
    }

    /**
     * Obtiene la cantidad total de ejércitos de un jugador.
     */
    public int getTotalArmiesByPlayer(Long gameId, Long playerId) {
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + playerId));

        Integer totalArmies = gameTerritoryRepository.getTotalArmiesByPlayer(player);
        return totalArmies != null ? totalArmies : 0;
    }

    /**
     * Obtiene territorios que pueden atacar (con más de 1 ejército).
     */
    public List<Territory> getTerritoriesCanAttack(Long gameId, Long playerId) {
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + playerId));

        return gameTerritoryRepository.findPlayerTerritoriesCanAttack(player).stream()
                .map(this::convertToTerritory)
                .collect(Collectors.toList());
    }

    /**
     * Transfiere la propiedad de un territorio de un jugador a otro.
     */
    @Override
    @Transactional
    public void transferTerritoryOwnership(Long gameId, Long countryId, Long newOwnerId, int armies) {
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

        CountryEntity country = countryRepository.findById(countryId)
                .orElseThrow(() -> new IllegalArgumentException("Country not found with id: " + countryId));

        PlayerEntity newOwner = playerRepository.findById(newOwnerId)
                .orElseThrow(() -> new PlayerNotFoundException("Player not found with id: " + newOwnerId));

        GameTerritoryEntity territory = gameTerritoryRepository.findByGameAndCountry(game, country)
                .orElseThrow(() -> new IllegalArgumentException("Territory not found in game"));

        territory.setOwner(newOwner);
        territory.setArmies(armies);
        gameTerritoryRepository.save(territory);
    }

    /**
     * Verifica si dos territorios son vecinos.
     */
    @Override
    public boolean areTerritoriesNeighbors(Long countryId1, Long countryId2) {
        CountryEntity country1 = countryRepository.findById(countryId1)
                .orElseThrow(() -> new IllegalArgumentException("Country not found with id: " + countryId1));

        return country1.getNeighbors().stream()
                .anyMatch(neighbor -> neighbor.getId().equals(countryId2));
    }

    /**
     * Obtiene los vecinos de un territorio.
     */
    @Override
    public List<Territory> getNeighborTerritories(Long gameId, Long countryId) {
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

        List<CountryEntity> neighbors = countryRepository.findNeighborsByCountryId(countryId);

        return neighbors.stream()
                .map(neighbor -> {
                    Optional<GameTerritoryEntity> territoryOpt =
                            gameTerritoryRepository.findByGameAndCountry(game, neighbor);
                    return territoryOpt.map(this::convertToTerritory).orElse(null);
                })
                .filter(territory -> territory != null)
                .collect(Collectors.toList());
    }

    // === MÉTODOS DE CONVERSIÓN ===

    /**
     * Convierte un CountryEntity a Territory básico (sin propietario).
     */
    private Territory convertCountryToTerritory(CountryEntity country) {
        return Territory.builder()
                .id(country.getId())
                .name(country.getName())
                .continentName(country.getContinent().getName())
                .armies(1) // Por defecto
                .neighborIds(country.getNeighbors().stream()
                        .map(CountryEntity::getId)
                        .collect(Collectors.toSet()))
                .build();
    }

    /**
     * Convierte un GameTerritoryEntity a Territory completo.
     */
    private Territory convertToTerritory(GameTerritoryEntity entity) {
        String ownerName = null;
        if (entity.getOwner() != null) {
            if (entity.getOwner().getUser() != null) {
                ownerName = entity.getOwner().getUser().getUsername();
            } else if (entity.getOwner().getBotProfile() != null) {
                ownerName = entity.getOwner().getBotProfile().getBotName();
            }
        }

        return Territory.builder()
                .id(entity.getCountry().getId())
                .name(entity.getCountry().getName())
                .continentName(entity.getCountry().getContinent().getName())
                .ownerId(entity.getOwner() != null ? entity.getOwner().getId() : null)
                .ownerName(ownerName)
                .armies(entity.getArmies())
                .neighborIds(entity.getCountry().getNeighbors().stream()
                        .map(CountryEntity::getId)
                        .collect(Collectors.toSet()))
                .build();
    }
}