package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.entities.ObjectiveEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.mappers.ObjectiveMapper;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.model.*;
import ar.edu.utn.frc.tup.piii.model.enums.ObjectiveType;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.repository.ObjectiveRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.ObjectiveService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ObjectiveServiceImpl implements ObjectiveService {

    @Autowired
    private ObjectiveRepository objectiveRepository;

    @Autowired
    private ObjectiveMapper objectiveMapper;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private PlayerMapper playerMapper;

    @Autowired
    private GameTerritoryService gameTerritoryService;

    @Override
    public Objective save(Objective objective) {
        ObjectiveEntity saved = objectiveRepository.save(objectiveMapper.toEntity(objective));
        return objectiveMapper.toModel(saved);
    }

    @Override
    public Optional<Objective> findById(Long id) {
        return objectiveRepository.findById(id).map(objectiveMapper::toModel);
    }

    @Override
    public List<Objective> findAll() {
        return objectiveRepository.findAll().stream()
                .map(objectiveMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<Objective> findByType(ObjectiveType type) {
        return objectiveRepository.findByType(type).stream()
                .map(objectiveMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        objectiveRepository.deleteById(id);
    }

    @Override
    public List<Objective> createObjectivesForGame(Game game) {
        return findAll(); // Podrías filtrar por tipo o cantidad
    }

    @Override
    public void assignObjectivesToPlayers(Game game) {
        List<Objective> objectives = new ArrayList<>(findAll());
        Collections.shuffle(objectives);

        List<Player> players = game.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            Objective o = objectives.get(i % objectives.size());
            p.setObjective(o);
        }
    }
    @Override
    public boolean isObjectiveAchieved(Long objectiveId, Game game, Player player) {
        Optional<Objective> obj = findById(objectiveId);
        return obj.map(o -> validateObjectiveCompletion(o, game, player)).orElse(false);
    }


    @Override
    public boolean validateObjectiveCompletion(Objective objective, Game game, Player player) {
        ObjectiveType type = objective.getType();

        switch (type) {
            case COMMON:
                return validateCommonObjective(player);
            case OCCUPATION:
                return validateOccupationObjective(objective, game, player);
            case DESTRUCTION:
                return validateDestructionObjective(objective, game);
            default:
                return false;
        }
    }

    private boolean validateCommonObjective(Player player) {
        PlayerEntity entity = playerMapper.toEntity(player);
        return gameTerritoryService.countWithMinArmies(entity, 2) >= 18;
    }

    private boolean validateOccupationObjective(Objective objective, Game game, Player player) {
        GameEntity gameEntity = gameMapper.toEntity(game);
        PlayerEntity playerEntity = playerMapper.toEntity(player);

        List<String> targetContinents = objective.getTargetContinents();

        for (String continent : targetContinents) {
            boolean ownsAll = gameTerritoryService.getByContinent(gameEntity, continent)
                    .stream()
                    .allMatch(t -> t.getOwner().equals(playerEntity));
            if (!ownsAll) return false;
        }

        return true;
    }

    private boolean validateDestructionObjective(Objective objective, Game game) {
        PlayerColor targetColor = objective.getTargetColor();

        return game.getPlayers().stream()
                .filter(p -> p.getColor() == targetColor)
                .allMatch(p -> p.getStatus() == PlayerStatus.ELIMINATED);
    }


    @Override
    public List<Objective> getCommonObjectives() {
        return findByType(ObjectiveType.COMMON);
    }

    @Override
    public List<Objective> getOccupationObjectives() {
        return findByType(ObjectiveType.OCCUPATION);
    }

    @Override
    public List<Objective> getDestructionObjectives() {
        return findByType(ObjectiveType.DESTRUCTION);
    }

    @Override
    public String getObjectiveProgress(Long objectiveId, Game game, Player player) {
        return "Progress tracking not implemented yet";
    }
}
