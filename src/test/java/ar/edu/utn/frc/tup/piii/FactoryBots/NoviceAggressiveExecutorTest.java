package ar.edu.utn.frc.tup.piii.FactoryBots;

import ar.edu.utn.frc.tup.piii.FactoryBots.NoviceStrategies.NoviceAggressiveExecutor;
import ar.edu.utn.frc.tup.piii.dtos.game.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.game.CombatResultDto;
import ar.edu.utn.frc.tup.piii.dtos.game.FortifyDto;
import ar.edu.utn.frc.tup.piii.entities.BotProfileEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import ar.edu.utn.frc.tup.piii.service.interfaces.CombatService;
import ar.edu.utn.frc.tup.piii.service.interfaces.FortificationService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NoviceAggressiveExecutorTest {

    @Mock
    private CombatService combatService;

    @Mock
    private FortificationService fortificationService;

    @Mock
    private GameTerritoryService gameTerritoryService;

    @InjectMocks
    private NoviceAggressiveExecutor executor;

    private PlayerEntity botPlayer;
    private BotProfileEntity botProfile;
    private GameEntity game;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        botPlayer = new PlayerEntity();
        botPlayer.setId(1L);

        botProfile = new BotProfileEntity();
        botProfile.setId(2L);
        botProfile.setBotName("botNovato");
        botProfile.setStrategy(BotStrategy.AGGRESSIVE);
        botProfile.setLevel(BotLevel.NOVICE); // Novice level

        botPlayer.setBotProfile(botProfile);

        game = new GameEntity();
        game.setId(100L);
        game.setGameCode("GAME-001");
    }

    @Test
    void testPerformBotReinforcement_WithBorderTerritories() {
        Territory t1 = Territory.builder()
                .id(10L)
                .name("Territorio1")
                .ownerId(botPlayer.getId())
                .armies(5)
                .neighborIds(Set.of(20L))
                .build();

        Territory t2 = Territory.builder()
                .id(20L)
                .name("Territorio2")
                .ownerId(2L)  // enemigo
                .armies(2)
                .neighborIds(Set.of(10L))
                .build();

        when(gameTerritoryService.getTerritoriesByOwner(game.getId(), botPlayer.getId()))
                .thenReturn(List.of(t1));
        when(gameTerritoryService.getNeighborTerritories(game.getId(), t1.getId()))
                .thenReturn(List.of(t2));

        executor.performBotReinforcement(botPlayer, game);

        verify(gameTerritoryService).getTerritoriesByOwner(game.getId(), botPlayer.getId());
        verify(gameTerritoryService).getNeighborTerritories(game.getId(), t1.getId());
    }

    @Test
    void testPerformBotAttack_AttackOnce() {
        Territory attacker = Territory.builder()
                .id(10L)
                .name("Atacante")
                .ownerId(botPlayer.getId())
                .armies(5)
                .neighborIds(Set.of(20L))
                .build();

        Territory target = Territory.builder()
                .id(20L)
                .name("Objetivo")
                .ownerId(2L)
                .armies(1)
                .neighborIds(Set.of(10L))
                .build();

        when(combatService.getAttackableTerritoriesForPlayer(game.getGameCode(), botPlayer.getId()))
                .thenReturn(List.of(attacker));
        when(combatService.getTargetsForTerritory(game.getGameCode(), attacker.getId(), botPlayer.getId()))
                .thenReturn(List.of(target));

        CombatResultDto combatResult = CombatResultDto.builder()
                .attackerCountryId(attacker.getId())
                .defenderCountryId(target.getId())
                .territoryConquered(true)
                .build();

        when(combatService.performCombat(eq(game.getGameCode()), any(AttackDto.class)))
                .thenReturn(combatResult);

        executor.performBotAttack(botPlayer, game);

        verify(combatService).getAttackableTerritoriesForPlayer(game.getGameCode(), botPlayer.getId());
        verify(combatService).getTargetsForTerritory(game.getGameCode(), attacker.getId(), botPlayer.getId());
        verify(combatService).performCombat(eq(game.getGameCode()), any(AttackDto.class));
    }

    @Test
    void testPerformBotFortify_SuccessfulFortification() {
        Territory safeTerritory = Territory.builder()
                .id(10L)
                .name("Seguro")
                .ownerId(botPlayer.getId())
                .armies(10)
                .neighborIds(Set.of(20L))
                .build();

        Territory targetTerritory = Territory.builder()
                .id(20L)
                .name("Fronterizo")
                .ownerId(botPlayer.getId())
                .armies(3)
                .neighborIds(Set.of(10L))
                .build();

        when(fortificationService.getFortifiableTerritoriesForPlayer(game.getGameCode(), botPlayer.getId()))
                .thenReturn(List.of(safeTerritory));

        when(gameTerritoryService.getNeighborTerritories(game.getId(), safeTerritory.getId()))
                .thenReturn(List.of(safeTerritory, targetTerritory)); // vecinos, todos propios

        when(fortificationService.getFortificationTargetsForTerritory(game.getGameCode(), safeTerritory.getId(), botPlayer.getId()))
                .thenReturn(List.of(targetTerritory));

        when(fortificationService.getMaxMovableArmies(game.getGameCode(), safeTerritory.getId()))
                .thenReturn(6);

        when(fortificationService.performFortification(eq(game.getGameCode()), any(FortifyDto.class)))
                .thenReturn(true);

        executor.performBotFortify(botPlayer, game);

        verify(fortificationService).performFortification(eq(game.getGameCode()), any(FortifyDto.class));
    }
}
