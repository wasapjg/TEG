package ar.edu.utn.frc.tup.piii.FactoryBots;
import ar.edu.utn.frc.tup.piii.FactoryBots.BalancedStrategies.BalancedAggressiveExecutor;
import ar.edu.utn.frc.tup.piii.dtos.game.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.game.CombatResultDto;
import ar.edu.utn.frc.tup.piii.dtos.game.FortifyDto;
import ar.edu.utn.frc.tup.piii.entities.CountryEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.entities.BotProfileEntity;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import ar.edu.utn.frc.tup.piii.service.interfaces.CombatService;
import ar.edu.utn.frc.tup.piii.service.interfaces.FortificationService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalancedAggressiveExecutorTest {

    @Mock
    private CombatService combatService;

    @Mock
    private FortificationService fortificationService;

    @Mock
    private GameTerritoryService gameTerritoryService;

    @InjectMocks
    private BalancedAggressiveExecutor executor;

    private PlayerEntity botPlayer;
    private GameEntity game;
    private BotProfileEntity botProfile;

    @BeforeEach
    void setUp() {
        botProfile = new BotProfileEntity();
        botProfile.setBotName("TestBot");

        botPlayer = new PlayerEntity();
        botPlayer.setId(1L);
        botPlayer.setBotProfile(botProfile);

        game = new GameEntity();
        game.setId(1L);
        game.setGameCode("TEST_GAME");
    }

    @Test
    void testGetLevel() {
        assertEquals(BotLevel.BALANCED, executor.getLevel());
    }

    @Test
    void testGetStrategy() {
        assertEquals(BotStrategy.AGGRESSIVE, executor.getStrategy());
    }

    @Test
    void testExecuteTurn() {
        // Arrange
        List<Territory> territories = createMockTerritories();
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenReturn(territories);
        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong()))
                .thenReturn(Collections.emptyList());
        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong()))
                .thenReturn(Collections.emptyList());

        // Act
        executor.executeTurn(botPlayer, game);

        // Assert
        verify(gameTerritoryService, atLeastOnce()).getTerritoriesByOwner(game.getId(), botPlayer.getId());
        verify(combatService, atLeastOnce()).getAttackableTerritoriesForPlayer(game.getGameCode(), botPlayer.getId());
        verify(fortificationService, atLeastOnce()).getFortifiableTerritoriesForPlayer(game.getGameCode(), botPlayer.getId());
    }

    @Test
    void testPerformBotReinforcement_WithNoTerritories() {
        // Arrange
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());

        // Act
        executor.performBotReinforcement(botPlayer, game);

        // Assert
        verify(gameTerritoryService).getTerritoriesByOwner(game.getId(), botPlayer.getId());
    }

    @Test
    void testPerformBotReinforcement_WithNoBorderTerritories() {
        // Arrange
        List<Territory> territories = createMockTerritories();
        territories.forEach(t -> t.setOwnerId(botPlayer.getId())); // Todos propios

        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenReturn(territories);

        // Mock neighbors que todos son del mismo jugador (sin frontera)
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(territories.subList(0, 1)); // Solo devolver vecinos propios

        // Act
        executor.performBotReinforcement(botPlayer, game);

        // Assert
        verify(gameTerritoryService).getTerritoriesByOwner(game.getId(), botPlayer.getId());
        verify(gameTerritoryService, atLeastOnce()).getNeighborTerritories(anyLong(), anyLong());
    }

    @Test
    void testPerformBotReinforcement_WithBorderTerritories() {
        // Arrange
        List<Territory> playerTerritories = createMockTerritories();
        playerTerritories.forEach(t -> t.setOwnerId(botPlayer.getId()));

        List<Territory> enemyNeighbors = createEnemyTerritories();

        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenReturn(playerTerritories);
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(enemyNeighbors); // Devolver vecinos enemigos

        // Act
        executor.performBotReinforcement(botPlayer, game);

        // Assert
        verify(gameTerritoryService).getTerritoriesByOwner(game.getId(), botPlayer.getId());
        verify(gameTerritoryService, atLeastOnce()).getNeighborTerritories(anyLong(), anyLong());
    }

    @Test
    void testPerformBotAttack_NoAttackableTerritories() {
        // Arrange
        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong()))
                .thenReturn(Collections.emptyList());

        // Act
        executor.performBotAttack(botPlayer, game);

        // Assert
        verify(combatService).getAttackableTerritoriesForPlayer(game.getGameCode(), botPlayer.getId());
    }

    @Test
    void testPerformBotAttack_WithAttackableTerritories() {
        // Arrange
        List<Territory> attackableTerritories = createMockTerritories();
        List<Territory> targets = createEnemyTerritories();

        CombatResultDto combatResult = new CombatResultDto();
        combatResult.setTerritoryConquered(true);

        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong()))
                .thenReturn(attackableTerritories);
        when(combatService.getTargetsForTerritory(anyString(), anyLong(), anyLong()))
                .thenReturn(targets);
        when(combatService.performCombat(anyString(), any(AttackDto.class)))
                .thenReturn(combatResult);

        // Act
        executor.performBotAttack(botPlayer, game);

        // Assert
        verify(combatService).getAttackableTerritoriesForPlayer(game.getGameCode(), botPlayer.getId());
        verify(combatService, atLeastOnce()).getTargetsForTerritory(anyString(), anyLong(), anyLong());
    }

    @Test
    void testPerformBotAttack_NoTargetsForTerritories() {
        // Arrange
        List<Territory> attackableTerritories = createMockTerritories();

        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong()))
                .thenReturn(attackableTerritories);
        when(combatService.getTargetsForTerritory(anyString(), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());

        // Act
        executor.performBotAttack(botPlayer, game);

        // Assert
        verify(combatService).getAttackableTerritoriesForPlayer(game.getGameCode(), botPlayer.getId());
        verify(combatService, atLeastOnce()).getTargetsForTerritory(anyString(), anyLong(), anyLong());
    }

    @Test
    void testPerformBotFortify_NoFortifiableTerritories() {
        // Arrange
        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong()))
                .thenReturn(Collections.emptyList());

        // Act
        executor.performBotFortify(botPlayer, game);

        // Assert
        verify(fortificationService).getFortifiableTerritoriesForPlayer(game.getGameCode(), botPlayer.getId());
    }

    @Test
    void testPerformBotFortify_WithFortifiableTerritories() {
        // Arrange
        List<Territory> fortifiableTerritories = createMockTerritories();
        List<Territory> targets = createMockTerritories();

        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong()))
                .thenReturn(fortifiableTerritories);
        when(fortificationService.getFortificationTargetsForTerritory(anyString(), anyLong(), anyLong()))
                .thenReturn(targets);
        when(fortificationService.getMaxMovableArmies(anyString(), anyLong()))
                .thenReturn(3);
        when(fortificationService.performFortification(anyString(), any(FortifyDto.class)))
                .thenReturn(true);
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(createEnemyTerritories());

        // Act
        executor.performBotFortify(botPlayer, game);

        // Assert
        verify(fortificationService).getFortifiableTerritoriesForPlayer(game.getGameCode(), botPlayer.getId());
        verify(fortificationService, atLeastOnce()).getFortificationTargetsForTerritory(anyString(), anyLong(), anyLong());
    }

    @Test
    void testPerformBotFortify_NoMovableArmies() {
        // Arrange
        List<Territory> fortifiableTerritories = createMockTerritories();
        List<Territory> targets = createMockTerritories();

        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong()))
                .thenReturn(fortifiableTerritories);
        when(fortificationService.getFortificationTargetsForTerritory(anyString(), anyLong(), anyLong()))
                .thenReturn(targets);
        when(fortificationService.getMaxMovableArmies(anyString(), anyLong()))
                .thenReturn(0);
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(createEnemyTerritories());

        // Act
        executor.performBotFortify(botPlayer, game);

        // Assert
        verify(fortificationService).getMaxMovableArmies(anyString(), anyLong());
        verify(fortificationService, never()).performFortification(anyString(), any(FortifyDto.class));
    }

    @Test
    void testEvaluateAttackProbability_NoAttackingArmies() {
        // Act
        double probability = executor.evaluateAttackProbability(botPlayer, 1, 5);

        // Assert
        assertEquals(0.0, probability);
    }

    @Test
    void testEvaluateAttackProbability_HighRatio() {
        // Act
        double probability = executor.evaluateAttackProbability(botPlayer, 15, 5);

        // Assert
        assertEquals(0.9, probability);
    }

    @Test
    void testEvaluateAttackProbability_ModerateRatio() {
        // Act
        double probability = executor.evaluateAttackProbability(botPlayer, 10, 5);

        // Assert
        assertEquals(0.75, probability);
    }

    @Test
    void testEvaluateAttackProbability_LowRatio() {
        // Act
        double probability = executor.evaluateAttackProbability(botPlayer, 7, 5);

        // Assert
        assertEquals(0.4, probability);
    }

    @Test
    void testEvaluateAttackProbability_VeryLowRatio() {
        // Act
        double probability = executor.evaluateAttackProbability(botPlayer, 6, 5);

        // Assert
        assertEquals(0.4, probability);
    }

    @Test
    void testEvaluateAttackProbability_PoorRatio() {
        // Act
        double probability = executor.evaluateAttackProbability(botPlayer, 5, 5);

        // Assert
        assertEquals(0.2, probability);
    }

    @Test
    void testGetBestAttackTargets() {
        // Act
        List<CountryEntity> targets = executor.getBestAttackTargets(botPlayer, game);

        // Assert
        assertNotNull(targets);
        assertTrue(targets.isEmpty());
    }

    @Test
    void testGetBestDefensePositions() {
        // Act
        List<CountryEntity> positions = executor.getBestDefensePositions(botPlayer, game);

        // Assert
        assertNotNull(positions);
        assertTrue(positions.isEmpty());
    }

    @Test
    void testExceptionHandling_Reinforcement() {
        // Arrange
        when(gameTerritoryService.getTerritoriesByOwner(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Test exception"));

        // Act & Assert - No debería lanzar excepción
        assertDoesNotThrow(() -> executor.performBotReinforcement(botPlayer, game));
    }

    @Test
    void testExceptionHandling_Attack() {
        // Arrange
        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong()))
                .thenThrow(new RuntimeException("Test exception"));

        // Act & Assert - No debería lanzar excepción
        assertDoesNotThrow(() -> executor.performBotAttack(botPlayer, game));
    }

    @Test
    void testExceptionHandling_Fortify() {
        // Arrange
        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong()))
                .thenThrow(new RuntimeException("Test exception"));

        // Act & Assert - No debería lanzar excepción
        assertDoesNotThrow(() -> executor.performBotFortify(botPlayer, game));
    }

    @Test
    void testPerformBotAttack_LowProbabilityTargets() {
        // Arrange
        List<Territory> attackableTerritories = createStrongAttackerTerritories();
        List<Territory> strongTargets = createStrongEnemyTerritories(); // Objetivos muy fuertes

        when(combatService.getAttackableTerritoriesForPlayer(anyString(), anyLong()))
                .thenReturn(attackableTerritories);
        when(combatService.getTargetsForTerritory(anyString(), anyLong(), anyLong()))
                .thenReturn(strongTargets);

        // Act
        executor.performBotAttack(botPlayer, game);

        // Assert - No debería realizar ataques por baja probabilidad
        verify(combatService, never()).performCombat(anyString(), any(AttackDto.class));
    }

    @Test
    void testPerformBotFortify_FailedFortification() {
        // Arrange
        List<Territory> fortifiableTerritories = createMockTerritories();
        List<Territory> targets = createMockTerritories();

        when(fortificationService.getFortifiableTerritoriesForPlayer(anyString(), anyLong()))
                .thenReturn(fortifiableTerritories);
        when(fortificationService.getFortificationTargetsForTerritory(anyString(), anyLong(), anyLong()))
                .thenReturn(targets);
        when(fortificationService.getMaxMovableArmies(anyString(), anyLong()))
                .thenReturn(3);
        when(fortificationService.performFortification(anyString(), any(FortifyDto.class)))
                .thenReturn(false); // Fallar la fortificación
        when(gameTerritoryService.getNeighborTerritories(anyLong(), anyLong()))
                .thenReturn(createEnemyTerritories());

        // Act
        executor.performBotFortify(botPlayer, game);

        // Assert
        verify(fortificationService).performFortification(anyString(), any(FortifyDto.class));
    }

    // Métodos auxiliares para crear datos de test

    private List<Territory> createMockTerritories() {
        List<Territory> territories = new ArrayList<>();

        Territory t1 = new Territory();
        t1.setId(1L);
        t1.setName("Territory1");
        t1.setOwnerId(botPlayer.getId());
        t1.setArmies(3);

        Territory t2 = new Territory();
        t2.setId(2L);
        t2.setName("Territory2");
        t2.setOwnerId(botPlayer.getId());
        t2.setArmies(5);

        territories.add(t1);
        territories.add(t2);

        return territories;
    }

    private List<Territory> createEnemyTerritories() {
        List<Territory> territories = new ArrayList<>();

        Territory enemy1 = new Territory();
        enemy1.setId(10L);
        enemy1.setName("EnemyTerritory1");
        enemy1.setOwnerId(2L); // Diferente jugador
        enemy1.setArmies(2);

        Territory enemy2 = new Territory();
        enemy2.setId(11L);
        enemy2.setName("EnemyTerritory2");
        enemy2.setOwnerId(3L); // Diferente jugador
        enemy2.setArmies(4);

        territories.add(enemy1);
        territories.add(enemy2);

        return territories;
    }

    private List<Territory> createStrongAttackerTerritories() {
        List<Territory> territories = new ArrayList<>();

        Territory strong = new Territory();
        strong.setId(20L);
        strong.setName("StrongAttacker");
        strong.setOwnerId(botPlayer.getId());
        strong.setArmies(2); // Pocas tropas para atacar

        territories.add(strong);
        return territories;
    }

    private List<Territory> createStrongEnemyTerritories() {
        List<Territory> territories = new ArrayList<>();

        Territory strongEnemy = new Territory();
        strongEnemy.setId(30L);
        strongEnemy.setName("StrongEnemy");
        strongEnemy.setOwnerId(2L);
        strongEnemy.setArmies(10); // Muy fuerte para defender

        territories.add(strongEnemy);
        return territories;
    }
}
