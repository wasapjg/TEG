package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.bot.AddBotsDto;
import ar.edu.utn.frc.tup.piii.dtos.game.CreateCodeDto;
import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.game.JoinGameDto;
import ar.edu.utn.frc.tup.piii.dtos.game.StartGameDto;
import ar.edu.utn.frc.tup.piii.dtos.game.UpdateGameSettingsDto;
import ar.edu.utn.frc.tup.piii.exceptions.ForbiddenException;
import ar.edu.utn.frc.tup.piii.exceptions.GameNotFoundException;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

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
    private GameMapper gameMapper;

    private ObjectMapper objectMapper;
    private GameResponseDto sampleDto;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();

        // -------------- Construimos sampleDto usando el Builder --------------
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


    // ====================================================================================
    // 1) TEST: POST /api/games/create-lobby
    //    ─────────────────────────────────────────────────────────────────────────────────
    //    - Caso exitoso: recibe hostUserId, crea la partida con valores por defecto y devuelve 201 + JSON
    //    - Caso error 400: falta hostUserId → se devuelve JSON con "message"
    // ====================================================================================

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


    // ====================================================================================
    // 2) TEST: GET /api/games/{gameCode}
    //    ─────────────────────────────────────────────────────────────────────────────────
    //    - Caso exitoso: existe la partida → devuelve 200 + DTO
    //    - Caso error 404: no existe la partida → lanza GameNotFoundException → status 404
    // ====================================================================================

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


    // ====================================================================================
    // 3) TEST: POST /api/games/join
    //    ─────────────────────────────────────────────────────────────────────────────────
    //    - Caso exitoso: JSON correcto → devuelve 200 + DTO con canStart=true
    //    - Caso error 400: falta gameCode o userId → JSON con "message"
    // ====================================================================================

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


    // ====================================================================================
    // 4) TEST: POST /api/games/add-bots
    //    ─────────────────────────────────────────────────────────────────────────────────
    //    - Caso exitoso: host coincide → devuelve 200 + DTO con canStart=true
    //    - Caso error 400: faltan gameCode/requesterId → JSON con "message"
    //    - Caso error 403 (en realidad 400) cuando el host no coincide
    // ====================================================================================

    @Test
    public void addBots_success() throws Exception {
        AddBotsDto addBotsDto = new AddBotsDto();
        addBotsDto.setGameCode("TEST123");
        addBotsDto.setNumberOfBots(1);
        addBotsDto.setBotLevel(BotLevel.EXPERT);
        addBotsDto.setBotStrategy(BotStrategy.AGGRESSIVE);
        addBotsDto.setRequesterId(1L);

        // Mockeamos un Game tal que getCreatedByUserId() == 1L
        Game existingGame = Mockito.mock(Game.class);
        when(existingGame.getCreatedByUserId()).thenReturn(1L);
        when(gameService.findByGameCode(eq("TEST123"))).thenReturn(existingGame);

        // Simulamos la respuesta de addBotsToGame + mapper
        Game dummyGame = new Game();
        GameResponseDto dummyDto = GameResponseDto.builder()
                .id(sampleDto.getId())
                .gameCode(sampleDto.getGameCode())
                .createdByUsername(sampleDto.getCreatedByUsername())
                .state(sampleDto.getState())
                .currentPhase(sampleDto.getCurrentPhase())
                .currentTurn(1)              // ya puede iniciar
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

        // Aquí esperamos un 400 porque ForbiddenException se traduce a Bad Request en tu app
        mockMvc.perform(post("/api/games/add-bots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addBotsDto)))
                .andExpect(status().isBadRequest());
    }


    // ====================================================================================
    // 5) TEST: POST /api/games/start
    //    ─────────────────────────────────────────────────────────────────────────────────
    //    - Caso exitoso: host coincide → 200 + DTO con currentTurn/currentPlayerIndex
    //    - Caso error 400: falta gameCode/userId → JSON con "message"
    //    - Caso “forbidden” (en realidad 400) cuando el host no coincide
    // ====================================================================================

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
                .state(GameState.IN_PROGRESS)
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
                .andExpect(jsonPath("$.state").value("IN_PROGRESS"));
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

        // Al no coincidir host, el controlador arroja ForbiddenException, que tu app
        // transforma en 400 Bad Request, por eso lo esperamos así:
        mockMvc.perform(post("/api/games/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startDto)))
                .andExpect(status().isBadRequest());
    }


    // ====================================================================================
    // 6) TEST: PUT /api/games/{gameCode}/settings
    //    ─────────────────────────────────────────────────────────────────────────────────
    //    - Caso exitoso: host coincide → 200 + DTO con nuevos valores
    //    - Caso error 400: falta requesterId → JSON con "message"
    //    - Caso “forbidden” (en realidad 400) cuando el host no coincide
    //    - Caso error 404: service lanza GameNotFoundException → 404 Not Found
    // ====================================================================================

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
                .maxPlayers(5)               // Cambiado
                .turnTimeLimit(90)           // Cambiado
                .chatEnabled(false)          // Cambiado
                .pactsAllowed(true)          // Cambiado
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

        // En tu aplicación, ForbiddenException se traduce en 400 Bad Request, así que lo comprobamos así:
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
}
