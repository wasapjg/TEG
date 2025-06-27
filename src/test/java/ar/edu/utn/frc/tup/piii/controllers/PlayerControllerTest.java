package ar.edu.utn.frc.tup.piii.controllers;

import ar.edu.utn.frc.tup.piii.dtos.player.PlayerRequestDto;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.User;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameService;
import ar.edu.utn.frc.tup.piii.service.interfaces.PlayerService;
import ar.edu.utn.frc.tup.piii.service.interfaces.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlayerService playerService;

    @MockBean
    private GameService gameService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private Player testPlayer;
    private Game testGame;
    private User testUser;

    @BeforeEach
    void setUp() {
        testGame = Game.builder()
                .createdByUserId(1L)
                .createdAt(LocalDateTime.now())
                .gameCode("ABC123")
                .maxPlayers(6)
                .build();

        testUser = User.builder()
                .id(1L)
                .username("player1")
                .build();

        testPlayer = Player.builder()
                .id(1L)
                .username("player1")
                .isBot(false)
                .status(PlayerStatus.ACTIVE)
                .color(PlayerColor.RED)
                .seatOrder(1)
                .joinedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllPlayers_ShouldReturnList() throws Exception {
        when(playerService.findAll()).thenReturn(List.of(testPlayer));

        mockMvc.perform(get("/api/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testPlayer.getId()))
                .andExpect(jsonPath("$[0].username").value(testPlayer.getUsername()));
    }

    @Test
    void getPlayerById_ShouldReturnPlayer() throws Exception {
        when(playerService.findById(1L)).thenReturn(Optional.of(testPlayer));

        mockMvc.perform(get("/api/players/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testPlayer.getId()))
                .andExpect(jsonPath("$.username").value(testPlayer.getUsername()));
    }

    @Test
    void createHumanPlayer_ShouldReturnCreatedPlayer() throws Exception {
        PlayerRequestDto dto = new PlayerRequestDto();
        dto.setUserId(1L);
        dto.setGameId(1L);
        dto.setIsBot(false);
        dto.setSeatOrder(1);

        when(userService.getUserById(1L)).thenReturn(testUser);
        when(gameService.findById(1L)).thenReturn(testGame);
        when(playerService.createHumanPlayer(testUser, testGame, 1)).thenReturn(testPlayer);

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("player1"));
    }

    @Test
    void createBotPlayer_ShouldReturnCreatedPlayer() throws Exception {
        PlayerRequestDto dto = new PlayerRequestDto();
        dto.setGameId(1L);
        dto.setIsBot(true);
        dto.setBotLevel("NOVICE");

        Player botPlayer = Player.builder()
                .id(2L)
                .username(null)
                .isBot(true)
                .botLevel(BotLevel.NOVICE)
                .status(PlayerStatus.ACTIVE)
                .seatOrder(1)
                .joinedAt(LocalDateTime.now())
                .build();

        when(gameService.findById(1L)).thenReturn(testGame);
        when(playerService.createBotPlayer(BotLevel.NOVICE, testGame)).thenReturn(botPlayer);

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isBot").value(true));
    }

    @Test
    void deletePlayer_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/players/1"))
                .andExpect(status().isOk());
    }
}
