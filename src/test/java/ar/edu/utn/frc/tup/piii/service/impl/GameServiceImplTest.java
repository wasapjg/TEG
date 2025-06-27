package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.entities.CountryEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.GameTerritoryEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.exceptions.GameNotFoundException;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.repository.*;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock private GameRepository gameRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private CountryRepository countryRepository;
    @Mock private GameTerritoryRepository gameTerritoryRepository;
    @Mock private ObjectiveRepository objectiveRepository;
    @Mock private GameMapper gameMapper;
    @Mock private GameStateServiceImpl gameStateServiceImpl;

    @InjectMocks private GameServiceImpl gameService;

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

    @Test
    void startGame_WhenGameDoesNotExist_ShouldThrowException() {
        when(gameRepository.findByGameCode("CODEX")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> gameService.startGame("CODEX"))
                .isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void prepareInitialPlacementPhase_WhenCountryNotOwned_ShouldThrowException() {
        //lo que necesito
        String gameCode = "CODE1";
        Long playerId = 10L;
        Map<Long, Integer> armies = Map.of(1L, 5);
        GameEntity gameEntity = new GameEntity();
        CountryEntity country = new CountryEntity();
        country.setId(1L);
        GameTerritoryEntity territory = new GameTerritoryEntity();
        PlayerEntity owner = new PlayerEntity();
        owner.setId(999L); // otro jugador
        territory.setOwner(owner);
        territory.setCountry(country);

        PlayerEntity jugador = new PlayerEntity();
        jugador.setId(playerId);
        gameEntity.getPlayers().add(jugador);

        List<PlayerEntity> jugadores = new ArrayList<>();
        jugadores.add(owner);
        jugadores.add(jugador);
        gameEntity.setPlayers(jugadores);


        //when
        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.of(gameEntity));
        when(countryRepository.getReferenceById(1L)).thenReturn(country);
        when(gameTerritoryRepository.findByGameAndCountry(gameEntity, country))
                .thenReturn(Optional.of(territory));
        when(gameMapper.toModel(any())).thenReturn(new Game()); // mockea el modelo
        when(gameService.findByGameCode(gameCode)).thenReturn(new Game());



        assertThatThrownBy(() -> gameService.prepareInitialPlacementPhase(gameCode, playerId, armies))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("don't own country");
    }
    @Test
    void prepareInitialPlacementPhase_WhenTotalArmiesInvalid_ShouldThrowException() {
        String gameCode = "CODE1";
        Long playerId = 10L;
        Map<Long, Integer> armies = Map.of(1L, 4); // ni 5 ni 3
        CountryEntity country = new CountryEntity();
        country.setId(1L);
        PlayerEntity player = new PlayerEntity();
        player.setId(playerId);
        player.setArmiesToPlace(5);
        GameTerritoryEntity territory = new GameTerritoryEntity();
        territory.setCountry(country);
        territory.setOwner(player);

        GameEntity gameEntity = new GameEntity();
        gameEntity.setPlayers(List.of(player));
        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.of(gameEntity));
        when(countryRepository.getReferenceById(1L)).thenReturn(country);
        when(gameTerritoryRepository.findByGameAndCountry(gameEntity, country)).thenReturn(Optional.of(territory));
        when(gameRepository.findByGameCode("CODE1")).thenReturn(Optional.of(gameEntity));
        when(gameMapper.toModel(any())).thenReturn(new Game());



        assertThatThrownBy(() -> gameService.prepareInitialPlacementPhase(gameCode, playerId, armies))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must place exactly 5 or 3 armies");
    }
    @Test
    void prepareInitialPlacementPhase_WhenValidPlacement_ShouldSucceed() {
        String gameCode = "CODE1";
        Long playerId = 10L;
        Map<Long, Integer> armies = Map.of(1L, 5);
        CountryEntity country = new CountryEntity();
        country.setId(1L);

        PlayerEntity player = new PlayerEntity();
        player.setId(playerId);
        player.setArmiesToPlace(8);

        GameTerritoryEntity territory = new GameTerritoryEntity();
        territory.setCountry(country);
        territory.setOwner(player);
        territory.setArmies(1);

        GameEntity gameEntity = new GameEntity();
        gameEntity.setPlayers(List.of(player));
        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.of(gameEntity));
        when(countryRepository.getReferenceById(1L)).thenReturn(country);
        when(gameTerritoryRepository.findByGameAndCountry(gameEntity, country)).thenReturn(Optional.of(territory));
        when(gameMapper.toModel(any())).thenReturn(new Game());
        when(gameRepository.findByGameCode("CODE1")).thenReturn(Optional.of(gameEntity));

        gameService.prepareInitialPlacementPhase(gameCode, playerId, armies);

        assertThat(player.getArmiesToPlace()).isEqualTo(3);
        assertThat(territory.getArmies()).isEqualTo(6);
        verify(playerRepository).save(player);
        verify(gameTerritoryRepository).save(territory);
    }
    @Test
    void startFirstTurn_ShouldSetCurrentPlayerIndex_UsingReflection() throws Exception {
        // preparo
        String gameCode = "CODE123";

        GameEntity gameEntity = new GameEntity();
        gameEntity.setGameCode(gameCode);

        PlayerEntity player0 = new PlayerEntity();
        player0.setSeatOrder(0);
        player0.setId(1L);

        PlayerEntity player1 = new PlayerEntity();
        player1.setSeatOrder(1);
        player1.setId(2L);

        List<PlayerEntity> jugadores = new ArrayList<>();
        jugadores.add(player0);
        jugadores.add(player1);

        gameEntity.setPlayers(jugadores);

        Game mockGame = new Game();
        mockGame.setGameCode(gameCode);

        //when
        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.of(gameEntity));
        when(gameMapper.toModel(gameEntity)).thenReturn(mockGame);

        // es privado
        Method method = GameServiceImpl.class.getDeclaredMethod("startFirstTurn", GameEntity.class);
        method.setAccessible(true);
        method.invoke(gameService, gameEntity);

        // asser
        assertThat(gameEntity.getCurrentPlayerIndex()).isEqualTo(0);
        verify(gameRepository).save(gameEntity);
    }

    @Test
    void startFirstTurn_WhenNoPlayerWithSeatOrderZero_ShouldThrowException() throws Exception {
        // preparo
        GameEntity gameEntity = new GameEntity();
        gameEntity.setGameCode("CODE_X");

        PlayerEntity player1 = new PlayerEntity();
        player1.setSeatOrder(1); // no hay seatOrder = 0

        gameEntity.setPlayers(List.of(player1));

        when(gameRepository.findByGameCode("CODE_X")).thenReturn(Optional.of(gameEntity));
        when(gameMapper.toModel(gameEntity)).thenReturn(new Game());

        Method method = GameServiceImpl.class.getDeclaredMethod("startFirstTurn", GameEntity.class);
        method.setAccessible(true);

        // Aassert
        assertThatThrownBy(() -> method.invoke(gameService, gameEntity))
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage("No player with seatOrder = 0 found");
    }




}