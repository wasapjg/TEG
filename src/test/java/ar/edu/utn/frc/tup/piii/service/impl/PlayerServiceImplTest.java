package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.mappers.GameMapper;
import ar.edu.utn.frc.tup.piii.mappers.ObjectiveMapper;
import ar.edu.utn.frc.tup.piii.mappers.PlayerMapper;
import ar.edu.utn.frc.tup.piii.mappers.UserMapper;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.User;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerStatus;
import ar.edu.utn.frc.tup.piii.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class PlayerServiceImplTest {

    private PlayerRepository playerRepository = mock(PlayerRepository.class);
    private PlayerMapper playerMapper = mock(PlayerMapper.class);
    private GameMapper gameMapper = mock(GameMapper.class);
    private UserMapper userMapper = mock(UserMapper.class);
    private ObjectiveMapper objectiveMapper = mock(ObjectiveMapper.class);

    private PlayerServiceImpl playerService;

    @BeforeEach
    void setup() throws Exception {
        playerService = new PlayerServiceImpl();

        setPrivateField(playerService, "playerRepository", playerRepository);
        setPrivateField(playerService, "playerMapper", playerMapper);
        setPrivateField(playerService, "gameMapper", gameMapper);
        setPrivateField(playerService, "userMapper", userMapper);
        setPrivateField(playerService, "objectiveMapper", objectiveMapper);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void createHumanPlayer_shouldPersistPlayerCorrectly() {
        Game game = new Game();
        game.setPlayers(List.of());
        User user = new User();
        GameEntity gameEntity = new GameEntity();

        PlayerEntity entityToSave = new PlayerEntity();
        entityToSave.setSeatOrder(1);
        entityToSave.setStatus(PlayerStatus.ACTIVE);
        entityToSave.setJoinedAt(LocalDateTime.now());
        entityToSave.setColor(PlayerColor.RED);

        PlayerEntity savedEntity = new PlayerEntity();
        Player expectedPlayer = new Player();

        when(userMapper.toEntity(user)).thenReturn(mock());
        when(gameMapper.toEntity(game)).thenReturn(gameEntity);
        when(playerRepository.save(any())).thenReturn(savedEntity);
        when(playerMapper.toModel(savedEntity)).thenReturn(expectedPlayer);

        Player result = playerService.createHumanPlayer(user, game, 1);

        assertThat(result).isEqualTo(expectedPlayer);
        verify(playerRepository).save(any());
    }

    @Test
    void findAll_shouldReturnPlayers() {
        List<PlayerEntity> entities = List.of(new PlayerEntity(), new PlayerEntity());
        when(playerRepository.findAll()).thenReturn(entities);
        when(playerMapper.toModel(any())).thenReturn(new Player());

        List<Player> result = playerService.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void findById_shouldReturnPlayerIfExists() {
        PlayerEntity entity = new PlayerEntity();
        Player player = new Player();
        when(playerRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(playerMapper.toModel(entity)).thenReturn(player);

        Optional<Player> result = playerService.findById(1L);

        assertThat(result).isPresent().contains(player);
    }

    @Test
    void deleteById_shouldCallRepository() {
        playerService.deleteById(5L);
        verify(playerRepository).deleteById(5L);
    }

    @Test
    void isEliminated_shouldReturnTrueIfPlayerStatusIsEliminated() {
        PlayerEntity entity = new PlayerEntity();
        entity.setStatus(PlayerStatus.ELIMINATED);

        when(playerRepository.findById(10L)).thenReturn(Optional.of(entity));

        boolean result = playerService.isEliminated(10L);

        assertThat(result).isTrue();
    }

    @Test
    void getArmiesToPlace_shouldReturnZeroIfNotFound() {
        when(playerRepository.findById(9L)).thenReturn(Optional.empty());

        int result = playerService.getArmiesToPlace(9L);

        assertThat(result).isEqualTo(0);
    }
}

