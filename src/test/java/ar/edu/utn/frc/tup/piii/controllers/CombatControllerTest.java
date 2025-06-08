package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.game.AttackDto;
import ar.edu.utn.frc.tup.piii.dtos.game.CombatResultDto;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Territory;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.model.enums.TurnPhase;
import ar.edu.utn.frc.tup.piii.service.interfaces.CombatService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameStateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CombatController.class)
class CombatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CombatService combatService;

    @MockBean
    private GameService gameService;

    @MockBean
    private GameStateService gameStateService;

    @Autowired
    private ObjectMapper objectMapper;

    private Game game;
    private AttackDto attackDto;
    private CombatResultDto combatResult;
    private Territory attackerTerritory;
    private Territory defenderTerritory;

    @BeforeEach
    void setUp() {
        // Setup Game
        game = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .state(GameState.NORMAL_PLAY)
                .currentPhase(TurnPhase.ATTACK)
                .currentPlayerIndex(0)
                .build();

        // Setup AttackDto
        attackDto = AttackDto.builder()
                .playerId(1L)
                .attackerCountryId(1L)
                .defenderCountryId(2L)
                .attackingArmies(2)
                .build();

        // Setup Territories
        attackerTerritory = Territory.builder()
                .id(1L)
                .name("Argentina")
                .ownerId(1L)
                .ownerName("Player1")
                .armies(3)
                .build();

        defenderTerritory = Territory.builder()
                .id(2L)
                .name("Brazil")
                .ownerId(2L)
                .ownerName("Player2")
                .armies(2)
                .build();

        // Setup CombatResult
        combatResult = CombatResultDto.builder()
                .attackerCountryId(1L)
                .attackerCountryName("Argentina")
                .defenderCountryId(2L)
                .defenderCountryName("Brazil")
                .attackerPlayerName("Player1")
                .defenderPlayerName("Player2")
                .attackerDice(Arrays.asList(6, 4))
                .defenderDice(Arrays.asList(5, 3))
                .attackerLosses(1)
                .defenderLosses(1)
                .territoryConquered(false)
                .attackerRemainingArmies(2)
                .defenderRemainingArmies(1)
                .build();
    }

    @Test
    void attack_WithValidInput_ShouldReturnCombatResult() throws Exception {
        // Given
        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(game, "attack")).thenReturn(true);
        when(combatService.performCombat("TEST123", attackDto)).thenReturn(combatResult);

        // When & Then
        mockMvc.perform(post("/api/games/TEST123/combat/attack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attackDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attackerCountryName").value("Argentina"))
                .andExpect(jsonPath("$.defenderCountryName").value("Brazil"))
                .andExpect(jsonPath("$.attackerLosses").value(1))
                .andExpect(jsonPath("$.defenderLosses").value(1))
                .andExpect(jsonPath("$.territoryConquered").value(false))
                .andExpect(jsonPath("$.attackerDice.length()").value(2))
                .andExpect(jsonPath("$.defenderDice.length()").value(2));
    }

    @Test
    void attack_WithInvalidGameState_ShouldReturnBadRequest() throws Exception {
        // Given
        game.setState(GameState.WAITING_FOR_PLAYERS);
        when(gameService.findByGameCode("TEST123")).thenReturn(game);

        // When & Then
        mockMvc.perform(post("/api/games/TEST123/combat/attack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attackDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void attack_WithInvalidTurn_ShouldReturnBadRequest() throws Exception {
        // Given
        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/games/TEST123/combat/attack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attackDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void attack_WithInvalidPhase_ShouldReturnBadRequest() throws Exception {
        // Given
        game.setCurrentPhase(TurnPhase.REINFORCEMENT);
        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(game, "attack")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/games/TEST123/combat/attack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attackDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void attack_WithTerritoryConquest_ShouldReturnConquestResult() throws Exception {
        // Given
        CombatResultDto conquestResult = CombatResultDto.builder()
                .attackerCountryId(1L)
                .attackerCountryName("Argentina")
                .defenderCountryId(2L)
                .defenderCountryName("Brazil")
                .attackerPlayerName("Player1")
                .defenderPlayerName("Player2")
                .attackerDice(Arrays.asList(6, 5))
                .defenderDice(Arrays.asList(4, 3))
                .attackerLosses(0)
                .defenderLosses(2)
                .territoryConquered(true)
                .attackerRemainingArmies(1)
                .defenderRemainingArmies(0)
                .build();

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(game, "attack")).thenReturn(true);
        when(combatService.performCombat("TEST123", attackDto)).thenReturn(conquestResult);

        // When & Then
        mockMvc.perform(post("/api/games/TEST123/combat/attack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attackDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.territoryConquered").value(true))
                .andExpect(jsonPath("$.defenderRemainingArmies").value(0))
                .andExpect(jsonPath("$.attackerLosses").value(0))
                .andExpect(jsonPath("$.defenderLosses").value(2));
    }

    @Test
    void getAttackableTerritories_WithValidPlayer_ShouldReturnTerritories() throws Exception {
        // Given
        List<Territory> attackableTerritories = Arrays.asList(
                Territory.builder().id(1L).name("Argentina").armies(3).build(),
                Territory.builder().id(3L).name("Chile").armies(2).build()
        );

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(combatService.getAttackableTerritoriesForPlayer("TEST123", 1L))
                .thenReturn(attackableTerritories);

        // When & Then
        mockMvc.perform(get("/api/games/TEST123/combat/attackable-territories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Argentina"))
                .andExpect(jsonPath("$[0].armies").value(3))
                .andExpect(jsonPath("$[1].name").value("Chile"))
                .andExpect(jsonPath("$[1].armies").value(2));
    }

    @Test
    void getAttackableTerritories_WithNoAttackableTerritories_ShouldReturnEmptyList() throws Exception {
        // Given
        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(combatService.getAttackableTerritoriesForPlayer("TEST123", 1L))
                .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/games/TEST123/combat/attackable-territories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAttackTargets_WithValidTerritory_ShouldReturnEnemyNeighbors() throws Exception {
        // Given
        List<Territory> targets = Arrays.asList(
                Territory.builder().id(2L).name("Brazil").ownerId(2L).armies(2).build(),
                Territory.builder().id(4L).name("Uruguay").ownerId(3L).armies(1).build()
        );

        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(combatService.getTargetsForTerritory("TEST123", 1L, 1L))
                .thenReturn(targets);

        // When & Then
        mockMvc.perform(get("/api/games/TEST123/combat/attack-targets/1/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Brazil"))
                .andExpect(jsonPath("$[0].ownerId").value(2))
                .andExpect(jsonPath("$[1].name").value("Uruguay"))
                .andExpect(jsonPath("$[1].ownerId").value(3));
    }

    @Test
    void getAttackTargets_WithNoTargets_ShouldReturnEmptyList() throws Exception {
        // Given
        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(combatService.getTargetsForTerritory("TEST123", 1L, 1L))
                .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/games/TEST123/combat/attack-targets/1/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void canPlayerAttack_WithValidConditions_ShouldReturnTrue() throws Exception {
        // Given
        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(game, "attack")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/games/TEST123/combat/can-attack/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void canPlayerAttack_WithInvalidTurn_ShouldReturnFalse() throws Exception {
        // Given
        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(false);
        when(gameStateService.canPerformAction(game, "attack")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/games/TEST123/combat/can-attack/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void canPlayerAttack_WithInvalidPhase_ShouldReturnFalse() throws Exception {
        // Given
        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(game, "attack")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/games/TEST123/combat/can-attack/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void canPlayerAttack_WithInvalidGameState_ShouldReturnFalse() throws Exception {
        // Given
        game.setState(GameState.WAITING_FOR_PLAYERS);
        when(gameService.findByGameCode("TEST123")).thenReturn(game);
        when(gameStateService.isPlayerTurn(game, 1L)).thenReturn(true);
        when(gameStateService.canPerformAction(game, "attack")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/games/TEST123/combat/can-attack/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void attack_WithInvalidInput_ShouldReturnBadRequest() throws Exception {
        // Given - AttackDto sin playerId
        AttackDto invalidDto = AttackDto.builder()
                .attackerCountryId(1L)
                .defenderCountryId(2L)
                .attackingArmies(2)
                .build();

        // When & Then
        mockMvc.perform(post("/api/games/TEST123/combat/attack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void attack_WithZeroAttackingArmies_ShouldReturnBadRequest() throws Exception {
        // Given
        AttackDto invalidDto = AttackDto.builder()
                .playerId(1L)
                .attackerCountryId(1L)
                .defenderCountryId(2L)
                .attackingArmies(0) // Inválido
                .build();

        // When & Then
        mockMvc.perform(post("/api/games/TEST123/combat/attack")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}