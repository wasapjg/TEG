package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.bot.AddBotsDto;
import ar.edu.utn.frc.tup.piii.dtos.game.*;
import ar.edu.utn.frc.tup.piii.dtos.player.PlayerResponseDto;
import ar.edu.utn.frc.tup.piii.exceptions.ForbiddenException;
import ar.edu.utn.frc.tup.piii.exceptions.GameNotFoundException;
import ar.edu.utn.frc.tup.piii.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.piii.exceptions.PlayerNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.service.impl.InitialPlacementServiceImpl;
import ar.edu.utn.frc.tup.piii.service.interfaces.CombatService;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.InitialPlacementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(GameController.class)
public class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @MockBean
    private InitialPlacementService initialPlacementService;

    @MockBean
    private CombatService combatService;

    @MockBean
    private GameMapper gameMapper;

    private ObjectMapper objectMapper;
    private GameResponseDto sampleDto;
    private GameResponseDto responseDto;
    private Game sampleGame;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        sampleGame = new Game();

        sampleDto = GameResponseDto.builder()
                .id(1L)
                .gameCode("LOBBY123")
                .createdByUsername("host")
                .state(GameState.WAITING_FOR_PLAYERS)
                .createdAt(LocalDateTime.of(2025, 6, 24, 18, 0))
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .build();

        responseDto = GameResponseDto.builder()
                .id(1L)
                .gameCode("CODE123")
                .createdByUsername("hostUser")
                .state(GameState.WAITING_FOR_PLAYERS)
                .turnTimeLimit(60)
                .chatEnabled(true)
                .pactsAllowed(false)
                .createdAt(LocalDateTime.of(2025, 6, 24, 23, 0))
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .canStart(true)
                .isGameOver(false)
                .build();
        sampleDto = GameResponseDto.builder()
                .id(1L)
                .gameCode("TEST123")
                .createdByUsername("hostUser")
                .state(GameState.WAITING_FOR_PLAYERS)
                .currentPhase(null)
                .currentTurn(null)
                .currentPlayerIndex(null)
                .maxPlayers(6)
                .turnTimeLimit(120)
                .chatEnabled(true)
                .pactsAllowed(false)
                .createdAt(LocalDateTime.of(2025, 6, 3, 2, 10, 0))
                .startedAt(null)
                .finishedAt(null)
                .currentPlayerName(null)
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .canStart(false)
                .isGameOver(false)
                .winnerName(null)
                .build();
    }

    @Test
    public void createLobby_success() throws Exception {
        CreateCodeDto requestDto = new CreateCodeDto();
        requestDto.setHostUserId(1L);

        Game dummyGame = new Game();
        when(gameService.createLobbyWithDefaults(eq(1L))).thenReturn(dummyGame);
        when(gameMapper.toResponseDto(dummyGame)).thenReturn(sampleDto);

        mockMvc.perform(post("/api/games/create-lobby")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameCode").value("TEST123"))
                .andExpect(jsonPath("$.createdByUsername").value("hostUser"))
                .andExpect(jsonPath("$.maxPlayers").value(6))
                .andExpect(jsonPath("$.chatEnabled").value(true));
    }

    @Test
    public void createLobby_missingHostUserId() throws Exception {
        mockMvc.perform(post("/api/games/create-lobby")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Debe enviar hostUserId en el CreateCodeDto"));
    }

    @Test
    public void getGameByCode_success() throws Exception {
        when(gameService.getGameByCode(eq("TEST123"))).thenReturn(sampleDto);

        mockMvc.perform(get("/api/games/TEST123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameCode").value("TEST123"))
                .andExpect(jsonPath("$.createdByUsername").value("hostUser"))
                .andExpect(jsonPath("$.state").value("WAITING_FOR_PLAYERS"));
    }

    @Test
    public void getGameByCode_notFound() throws Exception {
        when(gameService.getGameByCode(eq("NOT_EXISTS")))
                .thenThrow(new GameNotFoundException("not found"));

        mockMvc.perform(get("/api/games/NOT_EXISTS"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void joinGame_success() throws Exception {
        JoinGameDto joinDto = new JoinGameDto();
        joinDto.setGameCode("TEST123");
        joinDto.setUserId(2L);

        Game updatedGame = new Game();
        GameResponseDto updatedDto = GameResponseDto.builder()
                .id(sampleDto.getId())
                .gameCode(sampleDto.getGameCode())
                .createdByUsername(sampleDto.getCreatedByUsername())
                .state(sampleDto.getState())
                .currentPhase(sampleDto.getCurrentPhase())
                .currentTurn(1)
                .currentPlayerIndex(0)
                .maxPlayers(sampleDto.getMaxPlayers())
                .turnTimeLimit(sampleDto.getTurnTimeLimit())
                .chatEnabled(sampleDto.getChatEnabled())
                .pactsAllowed(sampleDto.getPactsAllowed())
                .createdAt(sampleDto.getCreatedAt())
                .startedAt(sampleDto.getStartedAt())
                .finishedAt(sampleDto.getFinishedAt())
                .currentPlayerName(null)
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .canStart(true)
                .isGameOver(false)
                .winnerName(null)
                .build();

        when(gameService.joinGame(any(JoinGameDto.class))).thenReturn(updatedGame);
        when(gameMapper.toResponseDto(eq(updatedGame))).thenReturn(updatedDto);

        mockMvc.perform(post("/api/games/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameCode").value("TEST123"))
                .andExpect(jsonPath("$.canStart").value(true));
    }

    @Test
    public void joinGame_missingParams() throws Exception {
        mockMvc.perform(post("/api/games/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameCode\":\"TEST123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Debe enviar gameCode y userId en el JoinGameDto"));
    }

    @Test
    public void addBots_success() throws Exception {
        AddBotsDto addBotsDto = new AddBotsDto();
        addBotsDto.setGameCode("TEST123");
        addBotsDto.setNumberOfBots(1);
        addBotsDto.setBotLevel(BotLevel.EXPERT);
        addBotsDto.setBotStrategy(BotStrategy.AGGRESSIVE);
        addBotsDto.setRequesterId(1L);

        Game existingGame = Mockito.mock(Game.class);
        when(existingGame.getCreatedByUserId()).thenReturn(1L);
        when(gameService.findByGameCode(eq("TEST123"))).thenReturn(existingGame);

        Game dummyGame = new Game();
        GameResponseDto dummyDto = GameResponseDto.builder()
                .id(sampleDto.getId())
                .gameCode(sampleDto.getGameCode())
                .createdByUsername(sampleDto.getCreatedByUsername())
                .state(sampleDto.getState())
                .currentPhase(sampleDto.getCurrentPhase())
                .currentTurn(1)
                .currentPlayerIndex(0)
                .maxPlayers(sampleDto.getMaxPlayers())
                .turnTimeLimit(sampleDto.getTurnTimeLimit())
                .chatEnabled(sampleDto.getChatEnabled())
                .pactsAllowed(sampleDto.getPactsAllowed())
                .createdAt(sampleDto.getCreatedAt())
                .startedAt(sampleDto.getStartedAt())
                .finishedAt(sampleDto.getFinishedAt())
                .currentPlayerName(null)
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .canStart(true)
                .isGameOver(false)
                .winnerName(null)
                .build();

        when(gameService.addBotsToGame(any(AddBotsDto.class))).thenReturn(dummyGame);
        when(gameMapper.toResponseDto(eq(dummyGame))).thenReturn(dummyDto);

        mockMvc.perform(post("/api/games/add-bots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addBotsDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameCode").value("TEST123"))
                .andExpect(jsonPath("$.canStart").value(true));
    }

    @Test
    public void addBots_missingParams() throws Exception {
        mockMvc.perform(post("/api/games/add-bots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numberOfBots\":1, \"requesterId\":1 }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Debe enviar gameCode y requesterId en el AddBotsDto"));
    }

    @Test
    public void addBots_forbidden() throws Exception {
        AddBotsDto addBotsDto = new AddBotsDto();
        addBotsDto.setGameCode("TEST123");
        addBotsDto.setNumberOfBots(1);
        addBotsDto.setBotLevel(BotLevel.EXPERT);
        addBotsDto.setBotStrategy(BotStrategy.BALANCED);
        addBotsDto.setRequesterId(2L); // no coincide con el host

        Game existingGame = Mockito.mock(Game.class);
        when(existingGame.getCreatedByUserId()).thenReturn(1L);
        when(gameService.findByGameCode(eq("TEST123"))).thenReturn(existingGame);

        mockMvc.perform(post("/api/games/add-bots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addBotsDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void startGame_success() throws Exception {
        StartGameDto startDto = new StartGameDto();
        startDto.setGameCode("TEST123");
        startDto.setUserId(1L);

        Game existingGame = Mockito.mock(Game.class);
        when(existingGame.getCreatedByUserId()).thenReturn(1L);
        when(gameService.findByGameCode(eq("TEST123"))).thenReturn(existingGame);

        Game dummyGame = new Game();
        GameResponseDto dummyDto = GameResponseDto.builder()
                .id(sampleDto.getId())
                .gameCode(sampleDto.getGameCode())
                .createdByUsername(sampleDto.getCreatedByUsername())
                .state(GameState.NORMAL_PLAY)
                .currentPhase(sampleDto.getCurrentPhase())
                .currentTurn(1)
                .currentPlayerIndex(0)
                .maxPlayers(sampleDto.getMaxPlayers())
                .turnTimeLimit(sampleDto.getTurnTimeLimit())
                .chatEnabled(sampleDto.getChatEnabled())
                .pactsAllowed(sampleDto.getPactsAllowed())
                .createdAt(sampleDto.getCreatedAt())
                .startedAt(LocalDateTime.of(2025,6,3,2,15,0))
                .finishedAt(null)
                .currentPlayerName(null)
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .canStart(false)
                .isGameOver(false)
                .winnerName(null)
                .build();

        when(gameService.startGame(eq("TEST123"))).thenReturn(dummyGame);
        when(gameMapper.toResponseDto(eq(dummyGame))).thenReturn(dummyDto);

        mockMvc.perform(post("/api/games/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameCode").value("TEST123"))
                .andExpect(jsonPath("$.currentTurn").value(1))
                .andExpect(jsonPath("$.currentPlayerIndex").value(0))
                .andExpect(jsonPath("$.state").value("NORMAL_PLAY"));
    }

    @Test
    public void startGame_missingParams() throws Exception {
        mockMvc.perform(post("/api/games/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gameCode\":\"TEST123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Debe enviar gameCode y userId en el StartGameDto"));
    }

    @Test
    public void startGame_forbidden() throws Exception {
        StartGameDto startDto = new StartGameDto();
        startDto.setGameCode("TEST123");
        startDto.setUserId(2L); // no coincide con host

        Game existingGame = Mockito.mock(Game.class);
        when(existingGame.getCreatedByUserId()).thenReturn(1L);
        when(gameService.findByGameCode(eq("TEST123"))).thenReturn(existingGame);

        mockMvc.perform(post("/api/games/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateSettings_success() throws Exception {
        UpdateGameSettingsDto settingsDto = new UpdateGameSettingsDto();
        settingsDto.setRequesterId(1L);
        settingsDto.setMaxPlayers(5);
        settingsDto.setTurnTimeLimit(90);
        settingsDto.setChatEnabled(false);
        settingsDto.setPactsAllowed(true);

        Game dummyGame = new Game();
        GameResponseDto dummyDto = GameResponseDto.builder()
                .id(sampleDto.getId())
                .gameCode(sampleDto.getGameCode())
                .createdByUsername(sampleDto.getCreatedByUsername())
                .state(sampleDto.getState())
                .currentPhase(sampleDto.getCurrentPhase())
                .currentTurn(sampleDto.getCurrentTurn())
                .currentPlayerIndex(sampleDto.getCurrentPlayerIndex())
                .maxPlayers(5)
                .turnTimeLimit(90)
                .chatEnabled(false)
                .pactsAllowed(true)
                .createdAt(sampleDto.getCreatedAt())
                .startedAt(sampleDto.getStartedAt())
                .finishedAt(sampleDto.getFinishedAt())
                .currentPlayerName(null)
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .canStart(sampleDto.getCanStart())
                .isGameOver(sampleDto.getIsGameOver())
                .winnerName(sampleDto.getWinnerName())
                .build();

        when(gameService.updateGameSettings(eq("TEST123"), any(UpdateGameSettingsDto.class)))
                .thenReturn(dummyGame);
        when(gameMapper.toResponseDto(eq(dummyGame))).thenReturn(dummyDto);

        mockMvc.perform(put("/api/games/TEST123/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settingsDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxPlayers").value(5))
                .andExpect(jsonPath("$.turnTimeLimit").value(90))
                .andExpect(jsonPath("$.chatEnabled").value(false))
                .andExpect(jsonPath("$.pactsAllowed").value(true));
    }

    @Test
    public void updateSettings_missingRequesterId() throws Exception {
        mockMvc.perform(put("/api/games/TEST123/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxPlayers\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("requesterId is required"));
    }

    @Test
    public void updateSettings_forbidden() throws Exception {
        UpdateGameSettingsDto forbiddenDto = new UpdateGameSettingsDto();
        forbiddenDto.setRequesterId(2L);
        forbiddenDto.setMaxPlayers(4);

        doThrow(new ForbiddenException("Solo el anfitrión puede modificar"))
                .when(gameService).updateGameSettings(eq("TEST123"), any(UpdateGameSettingsDto.class));

        mockMvc.perform(put("/api/games/TEST123/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forbiddenDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateSettings_gameNotFound() throws Exception {
        UpdateGameSettingsDto dto = new UpdateGameSettingsDto();
        dto.setRequesterId(1L);
        dto.setMaxPlayers(4);

        doThrow(new GameNotFoundException("not found"))
                .when(gameService).updateGameSettings(eq("UNKNOWN"), any(UpdateGameSettingsDto.class));

        mockMvc.perform(put("/api/games/UNKNOWN/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void kickPlayer_success() throws Exception {
        KickPlayerDto requestDto = new KickPlayerDto();
        requestDto.setGameCode("CODE123");
        requestDto.setPlayerId(2L);

        // Simulamos que el servicio expulsa y retorna un Game de dominio
        Game dummyGameModel = new Game();
        when(gameService.kickPlayer(any(KickPlayerDto.class))).thenReturn(dummyGameModel);

        // Creamos un PlayerResponseDto concreto
        PlayerResponseDto playerDto = new PlayerResponseDto();
        playerDto.setId(2L);
        playerDto.setUsername("usuario2");
        playerDto.setDisplayName("usuario2");
        playerDto.setStatus("ELIMINATED");
        playerDto.setColor("BLACK");
        playerDto.setIsBot(false);
        playerDto.setBotLevel(null);
        playerDto.setArmiesToPlace(0);
        playerDto.setSeatOrder(2);
        playerDto.setJoinedAt(LocalDateTime.of(2025, 6, 3, 11, 55));
        playerDto.setEliminatedAt(LocalDateTime.of(2025, 6, 3, 12, 30));
        playerDto.setHand(null);
        playerDto.setTerritoryIds(Collections.emptyList());
        playerDto.setObjective(null);
        playerDto.setTerritoryCount(0);
        playerDto.setTotalArmies(null);

        GameResponseDto kickResultDto = GameResponseDto.builder()
                .id(1L)
                .gameCode("CODE123")
                .createdByUsername("hostUser")
                .state(GameState.WAITING_FOR_PLAYERS)
                .currentPhase(null)
                .currentTurn(0)
                .currentPlayerIndex(0)
                .maxPlayers(4)
                .turnTimeLimit(60)
                .chatEnabled(true)
                .pactsAllowed(false)
                .createdAt(LocalDateTime.of(2025, 6, 3, 12, 0))
                .startedAt(null)
                .finishedAt(null)
                .currentPlayerName("hostUser")
                .players(Collections.singletonList(playerDto))
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .canStart(true)
                .isGameOver(false)
                .winnerName(null)
                .build();

        when(gameMapper.toResponseDto(eq(dummyGameModel))).thenReturn(kickResultDto);

        mockMvc.perform(post("/api/games/kick-player")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameCode").value("CODE123"))
                .andExpect(jsonPath("$.players[0].status").value("ELIMINATED"))
                .andExpect(jsonPath("$.players[0].eliminatedAt").exists());
    }

    @Test
    public void kickPlayer_missingParams() throws Exception {
        mockMvc.perform(post("/api/games/kick-player")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Debe enviar gameCode y playerId en el KickPlayerDto"));
    }

    @Test
    public void kickPlayer_gameNotFound() throws Exception {
        KickPlayerDto requestDto = new KickPlayerDto("NO_CODE", 2L);

        when(gameService.kickPlayer(any(KickPlayerDto.class)))
                .thenThrow(new GameNotFoundException("not found"));

        mockMvc.perform(post("/api/games/kick-player")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void kickPlayer_playerNotFound() throws Exception {
        KickPlayerDto requestDto = new KickPlayerDto("CODE123", 99L);

        when(gameService.kickPlayer(any(KickPlayerDto.class)))
                .thenThrow(new PlayerNotFoundException("player not in game"));

        mockMvc.perform(post("/api/games/kick-player")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void kickPlayer_invalidState() throws Exception {
        KickPlayerDto requestDto = new KickPlayerDto("CODE123", 2L);

        when(gameService.kickPlayer(any(KickPlayerDto.class)))
                .thenThrow(new InvalidGameStateException("Game not in waiting state"));

        mockMvc.perform(post("/api/games/kick-player")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.winnerName").value("Game not in waiting state"));
    }

    @Test
    public void kickPlayer_forbidden() throws Exception {
        KickPlayerDto requestDto = new KickPlayerDto("CODE123", 1L);

        when(gameService.kickPlayer(any(KickPlayerDto.class)))
                .thenThrow(new ForbiddenException("Cannot kick host"));

        mockMvc.perform(post("/api/games/kick-player")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void leaveGame_success() throws Exception {
        LeaveGameDto leaveDto = new LeaveGameDto();
        leaveDto.setGameCode("TEST123");
        leaveDto.setUserId(2L);

        Game game = new Game();

        when(gameService.leaveGame(eq(leaveDto))).thenReturn(game);
        when(gameMapper.toResponseDto(eq(game))).thenReturn(sampleDto);

        mockMvc.perform(post("/api/games/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameCode").value("TEST123"))
                .andExpect(jsonPath("$.createdByUsername").value("hostUser"));
    }

    @Test
    public void leaveGame_missingParams() throws Exception {
        mockMvc.perform(post("/api/games/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Debe enviar gameCode y userId en el LeaveGameDto"));
    }

    @Test
    public void leaveGame_gameNotFound() throws Exception {
        LeaveGameDto leaveDto = new LeaveGameDto();
        leaveDto.setGameCode("NOT_FOUND");
        leaveDto.setUserId(2L);

        when(gameService.leaveGame(eq(leaveDto)))
                .thenThrow(new GameNotFoundException("not found"));

        mockMvc.perform(post("/api/games/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void placeInitialArmiesLegacy_success() throws Exception {
        InitialArmyPlacementDto dto = new InitialArmyPlacementDto();
        dto.setPlayerId(1L);
        dto.setArmiesByCountry(Map.of(101L, 3, 102L, 2));

        mockMvc.perform(post("/api/games/TEST123/place-initial-armies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Armies placed successfully. Consider using /api/games/{gameCode}/initial-placement/place-armies for new implementations."));
    }

    @Test
    public void placeInitialArmiesLegacy_gameOrPlayerNotFound() throws Exception {
        InitialArmyPlacementDto dto = new InitialArmyPlacementDto();
        dto.setPlayerId(1L);
        dto.setArmiesByCountry(Map.of(101L, 3));

        doThrow(new GameNotFoundException("Game not found"))
                .when(initialPlacementService).placeInitialArmies(eq("TEST123"), eq(1L), any());

        mockMvc.perform(post("/api/games/TEST123/place-initial-armies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Game not found"));
    }
    @Test
    public void placeInitialArmiesLegacy_invalidState() throws Exception {
        InitialArmyPlacementDto dto = new InitialArmyPlacementDto();
        dto.setPlayerId(2L);
        dto.setArmiesByCountry(Map.of(103L, 5));

        doThrow(new InvalidGameStateException("Not in placement phase"))
                .when(initialPlacementService).placeInitialArmies(eq("TEST123"), eq(2L), any());

        mockMvc.perform(post("/api/games/TEST123/place-initial-armies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Not in placement phase"));
    }

    @Test
    public void placeInitialArmiesLegacy_internalError() throws Exception {
        InitialArmyPlacementDto dto = new InitialArmyPlacementDto();
        dto.setPlayerId(3L);
        dto.setArmiesByCountry(Map.of(104L, 4));

        doThrow(new RuntimeException("Unexpected"))
                .when(initialPlacementService).placeInitialArmies(eq("TEST123"), eq(3L), any());

        mockMvc.perform(post("/api/games/TEST123/place-initial-armies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Internal Error: Unexpected"));
    }

    @Test
    public void testCombat_success() throws Exception {
        String gameCode = "TEST123";

        // DTO de entrada
        AttackDto attackDto = AttackDto.builder()
                .playerId(1L)
                .attackerCountryId(10L)
                .defenderCountryId(20L)
                .attackingArmies(3)
                .attackerDice(3)
                .defenderDice(2)
                .build();

        // DTO de salida simulado
        CombatResultDto resultDto = CombatResultDto.builder()
                .attackerCountryId(10L)
                .attackerCountryName("Argentina")
                .defenderCountryId(20L)
                .defenderCountryName("Brasil")
                .attackerPlayerName("Jugador A")
                .defenderPlayerName("Jugador B")
                .attackerDice(List.of(6, 4, 3))
                .defenderDice(List.of(5, 2))
                .attackerLosses(1)
                .defenderLosses(2)
                .territoryConquered(true)
                .attackerRemainingArmies(5)
                .defenderRemainingArmies(0)
                .build();

        when(combatService.performCombat(eq(gameCode), any(AttackDto.class)))
                .thenReturn(resultDto);

        mockMvc.perform(post("/api/games/test-combat/" + gameCode)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attackDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attackerCountryId").value(10))
                .andExpect(jsonPath("$.defenderCountryId").value(20))
                .andExpect(jsonPath("$.attackerPlayerName").value("Jugador A"))
                .andExpect(jsonPath("$.territoryConquered").value(true))
                .andExpect(jsonPath("$.attackerLosses").value(1))
                .andExpect(jsonPath("$.defenderLosses").value(2));
    }




    @Test
    public void testCombat_gameNotFound() throws Exception {
        AttackDto dto = AttackDto.builder()
                .playerId(1L)
                .attackerCountryId(10L)
                .defenderCountryId(20L)
                .attackingArmies(3)
                .attackerDice(2)
                .defenderDice(2)
                .build();

        when(combatService.performCombat(eq("NO_GAME"), any(AttackDto.class)))
                .thenThrow(new GameNotFoundException("Game not found"));

        mockMvc.perform(post("/api/games/test-combat/NO_GAME")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }
    @Test
    public void joinGameLobby_success() throws Exception {
        PlayerIdRequestDto dto = new PlayerIdRequestDto(10L);

        Game game = new Game();
        Mockito.when(gameService.joinGameLobby(eq("CODE123"), eq(10L))).thenReturn(game);
        Mockito.when(gameMapper.toResponseDto(eq(game))).thenReturn(responseDto);

        mockMvc.perform(post("/api/games/CODE123/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameCode").value("CODE123"))
                .andExpect(jsonPath("$.createdByUsername").value("hostUser"));
    }

    @Test
    public void joinGameLobby_gameNotFound() throws Exception {
        PlayerIdRequestDto dto = new PlayerIdRequestDto(10L);

        Mockito.when(gameService.joinGameLobby(eq("CODE123"), eq(10L)))
                .thenThrow(new GameNotFoundException("not found"));

        mockMvc.perform(post("/api/games/CODE123/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void joinGameLobby_playerNotFound() throws Exception {
        PlayerIdRequestDto dto = new PlayerIdRequestDto(10L);

        Mockito.when(gameService.joinGameLobby(eq("CODE123"), eq(10L)))
                .thenThrow(new PlayerNotFoundException("not found"));

        mockMvc.perform(post("/api/games/CODE123/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void joinGameLobby_invalidGameState() throws Exception {
        PlayerIdRequestDto dto = new PlayerIdRequestDto(10L);

        Mockito.when(gameService.joinGameLobby(eq("CODE123"), eq(10L)))
                .thenThrow(new InvalidGameStateException("not allowed"));

        mockMvc.perform(post("/api/games/CODE123/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void joinGameLobby_internalError() throws Exception {
        PlayerIdRequestDto dto = new PlayerIdRequestDto(10L);

        Mockito.when(gameService.joinGameLobby(eq("CODE123"), eq(10L)))
                .thenThrow(new RuntimeException("Unexpected"));

        mockMvc.perform(post("/api/games/CODE123/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError());
    }
    @Test
    public void togglePlayerReady_success() throws Exception {
        Game game = new Game();
        when(gameService.togglePlayerReady("TEST123", 1L)).thenReturn(game);
        when(gameMapper.toResponseDto(game)).thenReturn(sampleDto);

        mockMvc.perform(put("/api/games/ABC123/player/1/ready"))
                .andExpect(status().isOk());
    }

    @Test
    public void togglePlayerReady_gameNotFound() throws Exception {
        when(gameService.togglePlayerReady("NOT_FOUND", 1L))
                .thenThrow(new GameNotFoundException("Not found"));

        mockMvc.perform(put("/api/games/NOT_FOUND/player/1/ready"))
                .andExpect(status().isNotFound());
    }


    @Test
    public void togglePlayerReady_internalError() throws Exception {
        when(gameService.togglePlayerReady("TEST123", 1L))
                .thenThrow(new RuntimeException("Unexpected"));

        mockMvc.perform(put("/api/games/TEST123/player/1/ready"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void getGameLobbyStatus_success() throws Exception {
        sampleGame = new Game();

        sampleDto = GameResponseDto.builder()
                .id(1L)
                .gameCode("LOBBY123")
                .createdByUsername("host")
                .state(GameState.WAITING_FOR_PLAYERS)
                .createdAt(LocalDateTime.of(2025, 6, 24, 18, 0))
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .build();

        Mockito.when(gameService.getGameLobbyStatus(eq("LOBBY123"), eq(1L))).thenReturn(sampleGame);
        Mockito.when(gameMapper.toResponseDto(eq(sampleGame))).thenReturn(sampleDto);

        mockMvc.perform(get("/api/games/LOBBY123/status")
                        .param("playerId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameCode").value("LOBBY123"))
                .andExpect(jsonPath("$.createdByUsername").value("host"));
    }

    @Test
    public void getGameLobbyStatus_withoutPlayerId() throws Exception {
        sampleGame = new Game();

        sampleDto = GameResponseDto.builder()
                .id(1L)
                .gameCode("LOBBY123")
                .createdByUsername("host")
                .state(GameState.WAITING_FOR_PLAYERS)
                .createdAt(LocalDateTime.of(2025, 6, 24, 18, 0))
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .build();

        Mockito.when(gameService.getGameLobbyStatus(eq("LOBBY123"), eq(null))).thenReturn(sampleGame);
        Mockito.when(gameMapper.toResponseDto(eq(sampleGame))).thenReturn(sampleDto);

        mockMvc.perform(get("/api/games/LOBBY123/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameCode").value("LOBBY123"));
    }

    @Test
    public void getGameLobbyStatus_gameNotFound() throws Exception {
        sampleGame = new Game();

        sampleDto = GameResponseDto.builder()
                .id(1L)
                .gameCode("LOBBY123")
                .createdByUsername("host")
                .state(GameState.WAITING_FOR_PLAYERS)
                .createdAt(LocalDateTime.of(2025, 6, 24, 18, 0))
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .build();

        Mockito.when(gameService.getGameLobbyStatus(eq("UNKNOWN"), any()))
                .thenThrow(new GameNotFoundException("not found"));

        mockMvc.perform(get("/api/games/UNKNOWN/status")
                        .param("playerId", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getGameLobbyStatus_internalError() throws Exception {
        sampleGame = new Game();

        sampleDto = GameResponseDto.builder()
                .id(1L)
                .gameCode("LOBBY123")
                .createdByUsername("host")
                .state(GameState.WAITING_FOR_PLAYERS)
                .createdAt(LocalDateTime.of(2025, 6, 24, 18, 0))
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .build();
        Mockito.when(gameService.getGameLobbyStatus(eq("ERROR123"), any()))
                .thenThrow(new RuntimeException("unexpected error"));

        mockMvc.perform(get("/api/games/ERROR123/status")
                        .param("playerId", "2"))
                .andExpect(status().isInternalServerError());
    }
    @Test
    public void resumeGame_success() throws Exception {
        sampleGame = new Game();

        responseDto = GameResponseDto.builder()
                .id(1L)
                .gameCode("GAME123")
                .createdByUsername("host")
                .createdAt(LocalDateTime.of(2025, 6, 24, 18, 0))
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .build();

        when(gameService.resumeGame(eq("GAME123"))).thenReturn(sampleGame);
        when(gameMapper.toResponseDto(eq(sampleGame))).thenReturn(responseDto);

        mockMvc.perform(post("/api/games/GAME123/resume")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameCode").value("GAME123"))
                .andExpect(jsonPath("$.createdByUsername").value("host"));
    }

    @Test
    public void resumeGame_notFound() throws Exception {
        sampleGame = new Game();

        responseDto = GameResponseDto.builder()
                .id(1L)
                .gameCode("GAME123")
                .createdByUsername("host")
                .createdAt(LocalDateTime.of(2025, 6, 24, 18, 0))
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .build();

        when(gameService.resumeGame(eq("INVALID"))).thenThrow(new GameNotFoundException("not found"));

        mockMvc.perform(post("/api/games/INVALID/resume")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void resumeGame_invalidState() throws Exception {
        sampleGame = new Game();

        responseDto = GameResponseDto.builder()
                .id(1L)
                .gameCode("GAME123")
                .createdByUsername("host")
                .createdAt(LocalDateTime.of(2025, 6, 24, 18, 0))
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .build();

        when(gameService.resumeGame(eq("GAME123"))).thenThrow(new InvalidGameStateException("invalid state"));

        mockMvc.perform(post("/api/games/GAME123/resume")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void resumeGame_internalError() throws Exception {
        sampleGame = new Game();

        responseDto = GameResponseDto.builder()
                .id(1L)
                .gameCode("GAME123")
                .createdByUsername("host")
                .createdAt(LocalDateTime.of(2025, 6, 24, 18, 0))
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .build();

        when(gameService.resumeGame(eq("GAME123"))).thenThrow(new RuntimeException("unexpected error"));

        mockMvc.perform(post("/api/games/GAME123/resume")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
    @Test
    public void disconnectFromLobby_success() throws Exception {
        sampleDto = GameResponseDto.builder()
                .id(1L)
                .gameCode("CODE123")
                .createdByUsername("hostUser")
                .state(GameState.WAITING_FOR_PLAYERS)
                .currentPhase(null)
                .currentTurn(0)
                .currentPlayerIndex(0)
                .maxPlayers(4)
                .turnTimeLimit(60)
                .chatEnabled(true)
                .pactsAllowed(false)
                .createdAt(LocalDateTime.of(2025, 6, 3, 12, 0))
                .startedAt(null)
                .finishedAt(null)
                .currentPlayerName("hostUser")
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .canStart(true)
                .isGameOver(false)
                .winnerName(null)
                .build();

        Game dummyGame = new Game();
        Mockito.when(gameService.disconnectFromLobby(eq("CODE123"), eq(2L))).thenReturn(dummyGame);
        Mockito.when(gameMapper.toResponseDto(dummyGame)).thenReturn(sampleDto);

        mockMvc.perform(post("/api/games/CODE123/player/2/disconnect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameCode").value("CODE123"));
    }

    @Test
    public void disconnectFromLobby_gameNotFound() throws Exception {
        sampleDto = GameResponseDto.builder()
                .id(1L)
                .gameCode("CODE123")
                .createdByUsername("hostUser")
                .state(GameState.WAITING_FOR_PLAYERS)
                .currentPhase(null)
                .currentTurn(0)
                .currentPlayerIndex(0)
                .maxPlayers(4)
                .turnTimeLimit(60)
                .chatEnabled(true)
                .pactsAllowed(false)
                .createdAt(LocalDateTime.of(2025, 6, 3, 12, 0))
                .startedAt(null)
                .finishedAt(null)
                .currentPlayerName("hostUser")
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .canStart(true)
                .isGameOver(false)
                .winnerName(null)
                .build();

        Mockito.when(gameService.disconnectFromLobby(eq("NOT_FOUND"), eq(2L)))
                .thenThrow(new GameNotFoundException("not found"));

        mockMvc.perform(post("/api/games/NOT_FOUND/player/2/disconnect"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void disconnectFromLobby_playerNotFound() throws Exception {
        sampleDto = GameResponseDto.builder()
                .id(1L)
                .gameCode("CODE123")
                .createdByUsername("hostUser")
                .state(GameState.WAITING_FOR_PLAYERS)
                .currentPhase(null)
                .currentTurn(0)
                .currentPlayerIndex(0)
                .maxPlayers(4)
                .turnTimeLimit(60)
                .chatEnabled(true)
                .pactsAllowed(false)
                .createdAt(LocalDateTime.of(2025, 6, 3, 12, 0))
                .startedAt(null)
                .finishedAt(null)
                .currentPlayerName("hostUser")
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .canStart(true)
                .isGameOver(false)
                .winnerName(null)
                .build();

        Mockito.when(gameService.disconnectFromLobby(eq("CODE123"), eq(99L)))
                .thenThrow(new PlayerNotFoundException("player not found"));

        mockMvc.perform(post("/api/games/CODE123/player/99/disconnect"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void disconnectFromLobby_invalidState() throws Exception {
        sampleDto = GameResponseDto.builder()
                .id(1L)
                .gameCode("CODE123")
                .createdByUsername("hostUser")
                .state(GameState.WAITING_FOR_PLAYERS)
                .currentPhase(null)
                .currentTurn(0)
                .currentPlayerIndex(0)
                .maxPlayers(4)
                .turnTimeLimit(60)
                .chatEnabled(true)
                .pactsAllowed(false)
                .createdAt(LocalDateTime.of(2025, 6, 3, 12, 0))
                .startedAt(null)
                .finishedAt(null)
                .currentPlayerName("hostUser")
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .canStart(true)
                .isGameOver(false)
                .winnerName(null)
                .build();

        Mockito.when(gameService.disconnectFromLobby(eq("CODE123"), eq(2L)))
                .thenThrow(new InvalidGameStateException("invalid state"));

        mockMvc.perform(post("/api/games/CODE123/player/2/disconnect"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void disconnectFromLobby_internalError() throws Exception {
        sampleDto = GameResponseDto.builder()
                .id(1L)
                .gameCode("CODE123")
                .createdByUsername("hostUser")
                .state(GameState.WAITING_FOR_PLAYERS)
                .currentPhase(null)
                .currentTurn(0)
                .currentPlayerIndex(0)
                .maxPlayers(4)
                .turnTimeLimit(60)
                .chatEnabled(true)
                .pactsAllowed(false)
                .createdAt(LocalDateTime.of(2025, 6, 3, 12, 0))
                .startedAt(null)
                .finishedAt(null)
                .currentPlayerName("hostUser")
                .players(Collections.emptyList())
                .territories(Collections.emptyMap())
                .continents(Collections.emptyList())
                .recentEvents(Collections.emptyList())
                .recentMessages(Collections.emptyList())
                .canStart(true)
                .isGameOver(false)
                .winnerName(null)
                .build();

        Mockito.when(gameService.disconnectFromLobby(eq("CODE123"), eq(2L)))
                .thenThrow(new RuntimeException("unexpected"));

        mockMvc.perform(post("/api/games/CODE123/player/2/disconnect"))
                .andExpect(status().isInternalServerError());
    }












}
