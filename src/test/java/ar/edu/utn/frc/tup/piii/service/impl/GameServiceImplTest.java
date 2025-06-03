package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.exceptions.GameNotFoundException;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.repository.GameRepository;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GameMapper gameMapper;

    @InjectMocks
    private GameServiceImpl gameService;

    @Test
    void findById_WhenGameExists_ShouldReturnGame() {
        // Given
        Long gameId = 1L;
        Game game = new Game();
        game.setId(gameId);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(new GameEntity()));
        when(gameMapper.toModel(any())).thenReturn(game);

        // When
        Game result = gameService.findById(gameId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gameId);
    }

    @Test
    void findById_WhenGameNotExists_ShouldThrowException() {
        // Given
        Long gameId = 999L;
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> gameService.findById(gameId))
                .isInstanceOf(GameNotFoundException.class)
                .hasMessage("Game not found with id: 999");
    }

    @Test
    void existsById_WhenGameExists_ShouldReturnTrue() {
        // Given
        Long gameId = 1L;
        when(gameRepository.existsById(gameId)).thenReturn(true);

        // When
        boolean result = gameService.existsById(gameId);

        // Then
        assertThat(result).isTrue();
    }
}