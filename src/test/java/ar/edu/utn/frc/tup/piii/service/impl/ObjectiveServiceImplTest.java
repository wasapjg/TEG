package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.entities.GameTerritoryEntity;
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
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ObjectiveServiceImplTest {

    @InjectMocks
    private ObjectiveServiceImpl objectiveService;

    @Mock
    private ObjectiveRepository objectiveRepository;

    @Mock
    private ObjectiveMapper objectiveMapper;

    @Mock
    private GameMapper gameMapper;

    @Mock
    private PlayerMapper playerMapper;

    @Mock
    private GameTerritoryService gameTerritoryService;

    private Objective objective;
    private ObjectiveEntity objectiveEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        objective = new Objective();
        objective.setId(1L);
        objective.setType(ObjectiveType.COMMON);

        objectiveEntity = new ObjectiveEntity();
        objectiveEntity.setId(1L);
        objectiveEntity.setType(ObjectiveType.COMMON);
    }

    @Test
    void testSaveObjective() {
        when(objectiveMapper.toEntity(objective)).thenReturn(objectiveEntity);
        when(objectiveRepository.save(objectiveEntity)).thenReturn(objectiveEntity);
        when(objectiveMapper.toModel(objectiveEntity)).thenReturn(objective);

        Objective saved = objectiveService.save(objective);

        assertNotNull(saved);
        assertEquals(ObjectiveType.COMMON, saved.getType());
        verify(objectiveRepository).save(objectiveEntity);
    }

    @Test
    void testFindByIdExists() {
        when(objectiveRepository.findById(1L)).thenReturn(Optional.of(objectiveEntity));
        when(objectiveMapper.toModel(objectiveEntity)).thenReturn(objective);

        Optional<Objective> result = objectiveService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(ObjectiveType.COMMON, result.get().getType());
    }

    @Test
    void testFindByIdNotExists() {
        when(objectiveRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Objective> result = objectiveService.findById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void testFindByType() {
        List<ObjectiveEntity> entityList = List.of(objectiveEntity);
        List<Objective> modelList = List.of(objective);

        when(objectiveRepository.findByType(ObjectiveType.COMMON)).thenReturn(entityList);
        when(objectiveMapper.toModel(objectiveEntity)).thenReturn(objective);

        List<Objective> result = objectiveService.findByType(ObjectiveType.COMMON);

        assertEquals(1, result.size());
        verify(objectiveRepository).findByType(ObjectiveType.COMMON);
    }

    @Test
    void testValidateCommonObjective_Valid() {
        Player player = new Player();
        PlayerEntity playerEntity = new PlayerEntity();

        when(playerMapper.toEntity(player)).thenReturn(playerEntity);
        when(gameTerritoryService.countWithMinArmies(playerEntity, 2)).thenReturn(20L);

        boolean result = objectiveService.validateObjectiveCompletion(objective, new Game(), player);

        assertTrue(result);
    }

    @Test
    void testValidateDestructionObjective_Valid() {
        Player p1 = Player.builder().color(PlayerColor.BLUE).status(PlayerStatus.ELIMINATED).build();
        Player p2 = Player.builder().color(PlayerColor.BLUE).status(PlayerStatus.ELIMINATED).build();
        Game game = new Game();
        game.setPlayers(List.of(p1, p2));

        objective.setType(ObjectiveType.DESTRUCTION);
        objective.setTargetColor(PlayerColor.BLUE);

        boolean result = objectiveService.validateObjectiveCompletion(objective, game, null);

        assertTrue(result);
    }

    @Test
    void testValidateOccupationObjective_Valid() {
        Game game = new Game();
        Player player = new Player();
        Objective occupationObjective = new Objective();
        occupationObjective.setType(ObjectiveType.OCCUPATION);
        occupationObjective.setTargetContinents(List.of("South America"));

        GameEntity gameEntity = new GameEntity();
        PlayerEntity playerEntity = new PlayerEntity();
        GameTerritoryEntity t1 = new GameTerritoryEntity();
        GameTerritoryEntity t2 = new GameTerritoryEntity();
        t1.setOwner(playerEntity);
        t2.setOwner(playerEntity);

        when(gameMapper.toEntity(game)).thenReturn(gameEntity);
        when(playerMapper.toEntity(player)).thenReturn(playerEntity);
        when(gameTerritoryService.getByContinent(gameEntity, "South America")).thenReturn(List.of(t1, t2));

        boolean result = objectiveService.validateObjectiveCompletion(occupationObjective, game, player);

        assertTrue(result);
    }

    @Test
    void testGetCommonObjectives() {
        List<ObjectiveEntity> list = List.of(objectiveEntity);

        when(objectiveRepository.findByType(ObjectiveType.COMMON)).thenReturn(list);
        when(objectiveMapper.toModel(objectiveEntity)).thenReturn(objective);

        List<Objective> result = objectiveService.getCommonObjectives();

        assertEquals(1, result.size());
    }
}
