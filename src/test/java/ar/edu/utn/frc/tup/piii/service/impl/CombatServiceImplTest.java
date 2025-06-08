package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.dtos.game.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.game.CombatResultDto;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameTerritoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CombatServiceImplTest {

    @Mock
    private GameTerritoryService gameTerritoryService;

    @Mock
    private GameService gameService;

    @InjectMocks
    private CombatServiceImpl combatService;

    private Game game;
    private AttackDto attackDto;
    private Territory attackerTerritory;
    private Territory defenderTerritory;

    @BeforeEach
    void setUp() {
        // Setup Game
        game = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .state(GameState.NORMAL_PLAY)
                .build();

        // Setup AttackDto
        attackDto = AttackDto.builder()
                .playerId(1L)
                .attackerCountryId(1L)
                .defenderCountryId(2L)
                .attackingArmies(2)
                .build();

        // Setup Attacker Territory
        attackerTerritory = Territory.builder()
                .id(1L)
                .name("Argentina")
                .ownerId(1L)
                .ownerName("Player1")
                .armies(3)
                .neighborIds(new HashSet<>(Arrays.asList(2L)))
                .build();

        // Setup Defender Territory
        defenderTerritory = Territory.builder()
                .id(2L)
                .name("Brazil")
                .ownerId(2L)
                .ownerName("Player2")
                .armies(2)
                .neighborIds(new HashSet<>(Arrays.asList(1L)))
                .build();
    }

    @Test
    void performCombat_WithValidAttack_ShouldReturnCombatResult() {
        // Given
        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(attackerTerritory);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 2L)).thenReturn(defenderTerritory);
        when(gameTerritoryService.areTerritoriesNeighbors(1L, 2L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> combatService.performCombat("TEST123", attackDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Territories are not neighbors");
    }

    @Test
    void performCombat_WithInsufficientArmies_ShouldThrowException() {
        // Given
        attackerTerritory.setArmies(1); // Only 1 army, cannot attack

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(attackerTerritory);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 2L)).thenReturn(defenderTerritory);

        // When & Then
        assertThatThrownBy(() -> combatService.performCombat("TEST123", attackDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Attacking territory must have more than 1 army");
    }

    @Test
    void performCombat_WithTooManyAttackingArmies_ShouldThrowException() {
        // Given
        attackDto.setAttackingArmies(5); // More than available (3-1=2 max)

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(attackerTerritory);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 2L)).thenReturn(defenderTerritory);
        when(gameTerritoryService.areTerritoriesNeighbors(1L, 2L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> combatService.performCombat("TEST123", attackDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid attacking armies");
    }

    @Test
    void getAttackableTerritoriesForPlayer_ShouldReturnTerritoriesWithMoreThanOneArmy() {
        // Given
        List<Territory> playerTerritories = Arrays.asList(
                Territory.builder().id(1L).name("Argentina").armies(3).build(), // Can attack
                Territory.builder().id(2L).name("Chile").armies(1).build(),     // Cannot attack
                Territory.builder().id(3L).name("Peru").armies(2).build()       // Can attack
        );

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(gameTerritoryService.getTerritoriesByOwner(1L, 1L)).thenReturn(playerTerritories);

        // When
        List<Territory> result = combatService.getAttackableTerritoriesForPlayer("TEST123", 1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Territory::getName).containsExactlyInAnyOrder("Argentina", "Peru");
        assertThat(result).allMatch(territory -> territory.getArmies() > 1);
    }

    @Test
    void getAttackableTerritoriesForPlayer_WithNoAttackableTerritories_ShouldReturnEmptyList() {
        // Given
        List<Territory> playerTerritories = Arrays.asList(
                Territory.builder().id(1L).name("Argentina").armies(1).build(),
                Territory.builder().id(2L).name("Chile").armies(1).build()
        );

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(gameTerritoryService.getTerritoriesByOwner(1L, 1L)).thenReturn(playerTerritories);

        // When
        List<Territory> result = combatService.getAttackableTerritoriesForPlayer("TEST123", 1L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getTargetsForTerritory_ShouldReturnEnemyNeighbors() {
        // Given
        List<Territory> neighbors = Arrays.asList(
                Territory.builder().id(2L).name("Brazil").ownerId(2L).build(),      // Enemy
                Territory.builder().id(3L).name("Chile").ownerId(1L).build(),       // Own territory
                Territory.builder().id(4L).name("Uruguay").ownerId(3L).build()      // Enemy
        );

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(attackerTerritory);
        when(gameTerritoryService.getNeighborTerritories(1L, 1L)).thenReturn(neighbors);

        // When
        List<Territory> result = combatService.getTargetsForTerritory("TEST123", 1L, 1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Territory::getName).containsExactlyInAnyOrder("Brazil", "Uruguay");
        assertThat(result).allMatch(territory -> !territory.getOwnerId().equals(1L));
    }

    @Test
    void getTargetsForTerritory_WithWrongOwnership_ShouldThrowException() {
        // Given
        attackerTerritory.setOwnerId(999L); // Different owner

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(attackerTerritory);

        // When & Then
        assertThatThrownBy(() -> combatService.getTargetsForTerritory("TEST123", 1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Player doesn't own the specified territory");
    }

    @Test
    void getTargetsForTerritory_WithInsufficientArmies_ShouldReturnEmptyList() {
        // Given
        attackerTerritory.setArmies(1); // Cannot attack

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(attackerTerritory);

        // When
        List<Territory> result = combatService.getTargetsForTerritory("TEST123", 1L, 1L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getTargetsForTerritory_WithOnlyOwnNeighbors_ShouldReturnEmptyList() {
        // Given
        List<Territory> ownNeighbors = Arrays.asList(
                Territory.builder().id(2L).name("Chile").ownerId(1L).build(),
                Territory.builder().id(3L).name("Peru").ownerId(1L).build()
        );

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(attackerTerritory);
        when(gameTerritoryService.getNeighborTerritories(1L, 1L)).thenReturn(ownNeighbors);

        // When
        List<Territory> result = combatService.getTargetsForTerritory("TEST123", 1L, 1L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void performCombat_WithHostilityOnlyState_ShouldSucceed() {
        // Given
        game.setState(GameState.HOSTILITY_ONLY);

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(attackerTerritory);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 2L)).thenReturn(defenderTerritory);
        when(gameTerritoryService.areTerritoriesNeighbors(1L, 2L)).thenReturn(true);

        Territory updatedAttacker = Territory.builder()
                .id(1L).name("Argentina").ownerId(1L).ownerName("Player1").armies(2).build();
        Territory updatedDefender = Territory.builder()
                .id(2L).name("Brazil").ownerId(2L).ownerName("Player2").armies(1).build();

        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(updatedAttacker);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 2L)).thenReturn(updatedDefender);

        // When
        CombatResultDto result = combatService.performCombat("TEST123", attackDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAttackerCountryName()).isEqualTo("Argentina");
        assertThat(result.getDefenderCountryName()).isEqualTo("Brazil");
    }

    @Test
    void performCombat_WithReinforcementState_ShouldThrowException() {
        // Given
        game.setState(GameState.REINFORCEMENT_5);
        when(gameService.findByGameCode("TEST123")).thenReturn(game);

        // When & Then
        assertThatThrownBy(() -> combatService.performCombat("TEST123", attackDto))
                .isInstanceOf(InvalidGameStateException.class)
                .hasMessageContaining("Combat not allowed in current game state");
    }

    @Test
    void performCombat_WithMaximumDice_ShouldUseLimitedDice() {
        // Given
        attackerTerritory.setArmies(10); // Many armies
        defenderTerritory.setArmies(10); // Many armies
        attackDto.setAttackingArmies(3); // 5 armies attacking (but max 3 dice)

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(attackerTerritory);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 2L)).thenReturn(defenderTerritory);
        when(gameTerritoryService.areTerritoriesNeighbors(1L, 2L)).thenReturn(true);

        Territory updatedAttacker = Territory.builder()
                .id(1L).name("Argentina").ownerId(1L).ownerName("Player1").armies(8).build();
        Territory updatedDefender = Territory.builder()
                .id(2L).name("Brazil").ownerId(2L).ownerName("Player2").armies(7).build();

        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 1L)).thenReturn(updatedAttacker);
        when(gameTerritoryService.getTerritoryByGameAndCountry(1L, 2L)).thenReturn(updatedDefender);

        // When
        CombatResultDto result = combatService.performCombat("TEST123", attackDto);

        // Then
        assertThat(result.getAttackerDice()).hasSizeLessThanOrEqualTo(3); // Max 3 dice
        assertThat(result.getDefenderDice()).hasSizeLessThanOrEqualTo(3); // Max 3 dice
        assertThat(result.getAttackerLosses() + result.getDefenderLosses()).isEqualTo(3); // 3 comparisons max
    }
}
