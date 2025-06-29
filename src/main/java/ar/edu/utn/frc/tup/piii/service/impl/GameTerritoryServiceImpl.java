package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.GameTerritoryEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.entities.CountryEntity;
import ar.edu.utn.frc.tup.piii.exceptions.GameNotFoundException;
import ar.edu.utn.frc.tup.piii.exceptions.PlayerNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.mappers.TerritoryMapper;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.repository.*;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GameTerritoryServiceImpl implements GameTerritoryService {

        @Autowired
        private GameTerritoryRepository gameTerritoryRepository;

        @Autowired
        private CountryRepository countryRepository;

        @Autowired
        private GameRepository gameRepository;

        @Autowired
        private PlayerRepository playerRepository;

        @Autowired
        private TerritoryMapper territoryMapper;

        @Autowired
        private PlayerMapper playerMapper;



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

        public List<Territory> getAllAvailableTerritories() {
                return countryRepository.findAll().stream()
                                .map(this::convertCountryToTerritory)
                                .collect(Collectors.toList());
        }
       /*
        @Override
        public void save(GameTerritoryEntity territory) {
                gameTerritoryRepository.save(territory);
        }
        */
        @Transactional
        public void assignTerritoryToPlayer(Long gameId, Long countryId, Long playerId, int initialArmies) {
                GameEntity game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

                CountryEntity country = countryRepository.findById(countryId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Country not found with id: " + countryId));

                PlayerEntity player = playerRepository.findById(playerId)
                                .orElseThrow(() -> new PlayerNotFoundException(
                                                "Player not found with id: " + playerId));

                // existe el territorio en el juego?
                Optional<GameTerritoryEntity> existingTerritory = gameTerritoryRepository.findByGameAndCountry(game,
                                country);

                if (existingTerritory.isPresent()) {
                        // Actualizar el dueño y los armies
                        GameTerritoryEntity territory = existingTerritory.get();
                        territory.setOwner(player);
                        territory.setArmies(initialArmies);
                        gameTerritoryRepository.save(territory);
                } else {
                        // crear territoty
                        GameTerritoryEntity newTerritory = new GameTerritoryEntity();
                        newTerritory.setGame(game);
                        newTerritory.setCountry(country);
                        newTerritory.setOwner(player);
                        newTerritory.setArmies(initialArmies);
                        gameTerritoryRepository.save(newTerritory);
                }
        }

        public Territory getTerritoryByGameAndCountry(Long gameId, Long countryId) {
                GameEntity game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

                CountryEntity country = countryRepository.findById(countryId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Country not found with id: " + countryId));

                return gameTerritoryRepository.findByGameAndCountry(game, country)
                                .map(this::convertToTerritory)
                                .orElse(null);
        }

        @Override
        @Transactional
        public void addArmiesToTerritory(Long gameId, Long countryId, Integer armies) {
                GameEntity game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

                CountryEntity country = countryRepository.findById(countryId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Country not found with id: " + countryId));

                GameTerritoryEntity territory = gameTerritoryRepository.findByGameAndCountry(game, country)
                                .orElseThrow(() -> new IllegalArgumentException("Territory not found in game"));

                territory.setArmies(territory.getArmies() + armies);
                gameTerritoryRepository.save(territory);
        }

        public List<Territory> getTerritoriesByOwner(Long gameId, Long playerId) {
                GameEntity game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

                PlayerEntity player = playerRepository.findById(playerId)
                                .orElseThrow(() -> new PlayerNotFoundException(
                                                "Player not found with id: " + playerId));

                return gameTerritoryRepository.findByGameAndOwner(game, player).stream()
                                .map(this::convertToTerritory)
                                .collect(Collectors.toList());
        }

        public List<Territory> getAllTerritoriesInGame(Long gameId) {
                GameEntity game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

                return gameTerritoryRepository.findByGame(game).stream()
                                .map(this::convertToTerritory)
                                .collect(Collectors.toList());
        }

        public boolean doesPlayerControlContinent(Long gameId, Long playerId, String continentName) {
                GameEntity game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

                PlayerEntity player = playerRepository.findById(playerId)
                                .orElseThrow(() -> new PlayerNotFoundException(
                                                "Player not found with id: " + playerId));

                List<GameTerritoryEntity> continentTerritories = gameTerritoryRepository
                                .findTerritoriesByContinentName(game, continentName);

                return continentTerritories.stream()
                                .allMatch(territory -> territory.getOwner().equals(player));
        }

        public int getTotalArmiesByPlayer(Long gameId, Long playerId) {
                GameEntity game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

                PlayerEntity player = playerRepository.findById(playerId)
                                .orElseThrow(() -> new PlayerNotFoundException(
                                                "Player not found with id: " + playerId));

                Integer totalArmies = gameTerritoryRepository.getTotalArmiesByPlayer(player);
                return totalArmies != null ? totalArmies : 0;
        }

        public List<Territory> getTerritoriesCanAttack(Long gameId, Long playerId) {
                GameEntity game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

                PlayerEntity player = playerRepository.findById(playerId)
                                .orElseThrow(() -> new PlayerNotFoundException(
                                                "Player not found with id: " + playerId));

                return gameTerritoryRepository.findPlayerTerritoriesCanAttack(player).stream()
                                .map(this::convertToTerritory)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public void transferTerritoryOwnership(Long gameId, Long countryId, Long newOwnerId, int armies) {
                GameEntity game = gameRepository.findById(gameId)
                                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

                CountryEntity country = countryRepository.findById(countryId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Country not found with id: " + countryId));

                PlayerEntity newOwner = playerRepository.findById(newOwnerId)
                                .orElseThrow(() -> new PlayerNotFoundException(
                                                "Player not found with id: " + newOwnerId));

                GameTerritoryEntity territory = gameTerritoryRepository.findByGameAndCountry(game, country)
                                .orElseThrow(() -> new IllegalArgumentException("Territory not found in game"));

                territory.setOwner(newOwner);

                //registrar conquista
                GameStateServiceImpl.registerConquest(game.getId(), newOwnerId);



                territory.setArmies(armies);
                gameTerritoryRepository.save(territory);
        }

        @Override
        public boolean areTerritoriesNeighbors(Long countryId1, Long countryId2) {

                return countryRepository.areCountriesNeighbors(countryId1, countryId2);
                /*
                 * CountryEntity country1 = countryRepository.findById(countryId1)
                 * .orElseThrow(() -> new IllegalArgumentException("Country not found with id: "
                 * + countryId1));
                 * 
                 * CountryEntity country2 = countryRepository.findById(countryId2)
                 * .orElseThrow(() -> new IllegalArgumentException("Country not found with id: "
                 * + countryId2));
                 * 
                 * return country1.getNeighbors().stream()
                 * .anyMatch(neighbor -> neighbor.getId().equals(countryId2));
                 */
        }

        @Override
        public List<Territory> getNeighborTerritories(Long gameId, Long countryId) {
                /*
                 * GameEntity game = gameRepository.findById(gameId)
                 * .orElseThrow(() -> new GameNotFoundException("Game not found with id: " +
                 * gameId));
                 * 
                 * List<CountryEntity> neighbors =
                 * countryRepository.findNeighborsByCountryId(countryId);
                 * 
                 * return neighbors.stream()
                 * .map(neighbor -> {
                 * Optional<GameTerritoryEntity> territoryOpt = gameTerritoryRepository
                 * .findByGameAndCountry(game, neighbor);
                 * return territoryOpt.map(this::convertToTerritory).orElse(null);
                 * })
                 * .filter(territory -> territory != null)
                 * .collect(Collectors.toList());
                 */
                Set<Long> neighborIds = new HashSet<>();

                // 1. Obtener vecinos directos (donde countryId es el "country_id" en la tabla)
                CountryEntity country = countryRepository.findById(countryId).orElse(null);
                if (country != null) {
                        country.getNeighbors().forEach(neighbor -> neighborIds.add(neighbor.getId()));
                }

                // 2. Obtener vecinos inversos (donde countryId es el "neighbor_id" en la tabla)
                List<CountryEntity> countriesWithThisAsNeighbor = countryRepository
                                .findCountriesThatHaveAsNeighbor(countryId);
                countriesWithThisAsNeighbor.forEach(c -> neighborIds.add(c.getId()));

                // 3. Convertir IDs de países a territorios del juego específico
                return neighborIds.stream()
                                .map(neighborId -> getTerritoryByGameAndCountry(gameId, neighborId))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
        }

    @Override
    public Territory getTerritoryByGameAndCountryName(Long gameId, String countryName) {
        GameEntity game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + gameId));

        CountryEntity country = countryRepository.findByName(countryName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Country not found with name: " + countryName));

        return gameTerritoryRepository.findByGameAndCountry(game, country)
                .map(this::convertToTerritory)
                .orElse(null);
    }

    //Metodos de conversion

        /**
         * Convierte un CountryEntity a Territory básico (sin propietario).
         */
        private Territory convertCountryToTerritory(CountryEntity country) {
                // Get both direct and inverse neighbors
                Set<Long> neighborIds = new HashSet<>();
                
                // Direct neighbors (where this country is the source)
                country.getNeighbors().forEach(neighbor -> neighborIds.add(neighbor.getId()));
                
                // Inverse neighbors (where other countries have this country as neighbor)
                List<CountryEntity> countriesWithThisAsNeighbor = countryRepository
                        .findCountriesThatHaveAsNeighbor(country.getId());
                countriesWithThisAsNeighbor.forEach(c -> neighborIds.add(c.getId()));
                
                return Territory.builder()
                                .id(country.getId())
                                .name(country.getName())
                                .continentName(country.getContinent().getName())
                                .armies(1) // Por defecto
                                .neighborIds(neighborIds)
                                .build();
        }

        /**
         * Convierte un GameTerritoryEntity a Territory completo.
         */
        private Territory convertToTerritory(GameTerritoryEntity entity) {

            String ownerName = null;
            if (entity.getOwner() != null) {
                // CAMBIO: Usar PlayerMapper para obtener el displayName correcto
                Player ownerPlayer = playerMapper.toModel(entity.getOwner());
                ownerName = ownerPlayer.getDisplayName();
            }

            return Territory.builder()
                    .id(entity.getCountry().getId())
                    .name(entity.getCountry().getName())
                    .continentName(entity.getCountry().getContinent().getName())
                    .ownerId(entity.getOwner() != null ? entity.getOwner().getId() : null)
                    .ownerName(ownerName)  // ← Ahora incluye el # y número para bots
                    .armies(entity.getArmies())
                    .neighborIds(entity.getCountry().getNeighbors().stream()
                            .map(CountryEntity::getId)
                            .collect(Collectors.toSet()))
                    .build();
        }
}