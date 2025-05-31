package ar.edu.utn.frc.tup.piii.utils;

import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ColorManagerTest {

    @InjectMocks
    private ColorManager colorManager;

    private Game game;

    @BeforeEach
    void setUp() {
        game = Game.builder()
                .id(1L)
                .players(Collections.emptyList())
                .build();
    }

    @Test
    void getAvailableRandomColor_WhenNoPlayersInGame_ShouldReturnAnyColor() {
        PlayerColor result = colorManager.getAvailableRandomColor(game);

        assertThat(result).isNotNull();
        assertThat(result).isIn(PlayerColor.values());
    }

    @Test
    void getAvailableRandomColor_WhenSomeColorsUsed_ShouldReturnAvailableColor() {
        Player player1 = Player.builder()
                .id(1L)
                .color(PlayerColor.RED)
                .status(PlayerStatus.ACTIVE)
                .build();

        Player player2 = Player.builder()
                .id(2L)
                .color(PlayerColor.BLUE)
                .status(PlayerStatus.ACTIVE)
                .build();

        game.setPlayers(Arrays.asList(player1, player2));

        PlayerColor result = colorManager.getAvailableRandomColor(game);

        assertThat(result).isNotNull();
        assertThat(result).isNotIn(PlayerColor.RED, PlayerColor.BLUE);
    }

    @Test
    void getAvailableRandomColor_WhenAllColorsUsed_ShouldReturnAnyColor() {
        Player[] players = new Player[PlayerColor.values().length];
        for (int i = 0; i < PlayerColor.values().length; i++) {
            players[i] = Player.builder()
                    .id((long) (i + 1))
                    .color(PlayerColor.values()[i])
                    .status(PlayerStatus.ACTIVE)
                    .build();
        }

        game.setPlayers(Arrays.asList(players));

        PlayerColor result = colorManager.getAvailableRandomColor(game);

        assertThat(result).isNotNull();
        assertThat(result).isIn(PlayerColor.values());
    }
}
