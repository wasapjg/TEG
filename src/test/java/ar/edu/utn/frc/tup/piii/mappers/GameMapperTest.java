package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.entities.*;
import ar.edu.utn.frc.tup.piii.model.*;
import ar.edu.utn.frc.tup.piii.model.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameMapperTest {

    @Mock
    private PlayerMapper playerMapper;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private GameEventMapper gameEventMapper;

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @InjectMocks
    private GameMapper gameMapper;

    private GameEntity gameEntity;
    private Game game;
    private UserEntity userEntity;
    private PlayerEntity playerEntity;
    private GameTerritoryEntity territoryEntity;
    private CountryEntity countryEntity;
    private ContinentEntity continentEntity;
    private CardEntity cardEntity;
    private GameEventEntity gameEventEntity;
    private ChatMessageEntity chatMessageEntity;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = LocalDateTime.now();

        // Setup UserEntity
        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("testuser");

        // Setup Continent
        continentEntity = new ContinentEntity();
        continentEntity.setId(1L);
        continentEntity.setName("Test Continent");
        continentEntity.setBonusArmies(3);

        // Setup Country
        countryEntity = new CountryEntity();
        countryEntity.setId(1L);
        countryEntity.setName("Test Country");
        countryEntity.setContinent(continentEntity);
        countryEntity.setNeighbors(new HashSet<>());

        // Setup Player
        playerEntity = new PlayerEntity();
        playerEntity.setId(1L);
        playerEntity.setUser(userEntity);
        playerEntity.setStatus(PlayerStatus.ACTIVE);
        playerEntity.setColor(PlayerColor.RED);

        // Setup Territory
        territoryEntity = new GameTerritoryEntity();
        territoryEntity.setId(1L);
        territoryEntity.setCountry(countryEntity);
        territoryEntity.setOwner(playerEntity);
        territoryEntity.setArmies(5);

        // Setup Card
        cardEntity = new CardEntity();
        cardEntity.setId(1L);
        cardEntity.setType(CardType.INFANTRY);
        cardEntity.setCountry(countryEntity);

        // Setup Game Event
        gameEventEntity = new GameEventEntity();
        gameEventEntity.setId(1L);
        gameEventEntity.setType(EventType.GAME_STARTED);
        gameEventEntity.setTurnNumber(1);
        gameEventEntity.setTimestamp(testTime);

        // Setup Chat Message
        chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setId(1L);
        chatMessageEntity.setContent("Test message");
        chatMessageEntity.setSentAt(testTime);
        chatMessageEntity.setIsSystemMessage(false);

        // Setup GameEntity
        gameEntity = new GameEntity();
        gameEntity.setId(1L);
        gameEntity.setGameCode("TEST123");
        gameEntity.setCreatedBy(userEntity);
        gameEntity.setStatus(GameState.NORMAL_PLAY);
        gameEntity.setCurrentPhase(TurnPhase.ATTACK);
        gameEntity.setCurrentTurn(5);
        gameEntity.setCurrentPlayerIndex(0);
        gameEntity.setMaxPlayers(6);
        gameEntity.setTurnTimeLimit(600);
        gameEntity.setChatEnabled(true);
        gameEntity.setPactsAllowed(false);
        gameEntity.setCreatedAt(testTime);
        gameEntity.setStartedAt(testTime.minusHours(1));
        gameEntity.setFinishedAt(null);
        gameEntity.setLastModified(testTime);
        gameEntity.setPlayers(Arrays.asList(playerEntity));
        gameEntity.setTerritories(Arrays.asList(territoryEntity));
        gameEntity.setDeck(Arrays.asList(cardEntity));
        gameEntity.setEvents(Arrays.asList(gameEventEntity));
        gameEntity.setChatMessages(Arrays.asList(chatMessageEntity));

        // Setup Game model
        game = Game.builder()
                .id(1L)
                .gameCode("TEST123")
                .createdByUsername("testuser")
                .state(GameState.NORMAL_PLAY)
                .currentPhase(TurnPhase.ATTACK)
                .currentTurn(5)
                .currentPlayerIndex(0)
                .maxPlayers(6)
                .turnTimeLimit(600)
                .chatEnabled(true)
                .pactsAllowed(false)
                .createdAt(testTime)
                .startedAt(testTime.minusHours(1))
                .finishedAt(null)
                .lastModified(testTime)
                .build();
    }

    @Test
    void toModel_WithValidEntity_ShouldMapCorrectly() {
        // Given
        when(playerMapper.toModel(any(PlayerEntity.class)))
                .thenReturn(Player.builder().id(1L).build());
        when(cardMapper.toModel(any(CardEntity.class)))
                .thenReturn(Card.builder().id(1L).build());
        when(gameEventMapper.toModel(any(GameEventEntity.class)))
                .thenReturn(GameEvent.builder().id(1L).build());
        when(chatMessageMapper.toModel(any(ChatMessageEntity.class)))
                .thenReturn(ChatMessage.builder().id(1L).build());

        // When
        Game result = gameMapper.toModel(gameEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getGameCode()).isEqualTo("TEST123");
        assertThat(result.getCreatedByUsername()).isEqualTo("testuser");
        assertThat(result.getState()).isEqualTo(GameState.NORMAL_PLAY);
        assertThat(result.getCurrentPhase()).isEqualTo(TurnPhase.ATTACK);
        assertThat(result.getCurrentTurn()).isEqualTo(5);
        assertThat(result.getCurrentPlayerIndex()).isEqualTo(0);
        assertThat(result.getMaxPlayers()).isEqualTo(6);
        assertThat(result.getTurnTimeLimit()).isEqualTo(600);
        assertThat(result.getChatEnabled()).isTrue();
        assertThat(result.getPactsAllowed()).isFalse();
        assertThat(result.getCreatedAt()).isEqualTo(testTime);
        assertThat(result.getStartedAt()).isEqualTo(testTime.minusHours(1));
        assertThat(result.getFinishedAt()).isNull();
        assertThat(result.getLastModified()).isEqualTo(testTime);
        assertThat(result.getPlayers()).hasSize(1);
        assertThat(result.getTerritories()).hasSize(1);
        assertThat(result.getDeck()).hasSize(1);
        assertThat(result.getEvents()).hasSize(1);
        assertThat(result.getChatMessages()).hasSize(1);
    }

    @Test
    void toModel_WithNullEntity_ShouldReturnNull() {
        // When
        Game result = gameMapper.toModel(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toModel_WithNullCreatedBy_ShouldHandleCorrectly() {
        // Given
        gameEntity.setCreatedBy(null);

        when(playerMapper.toModel(any(PlayerEntity.class)))
                .thenReturn(Player.builder().id(1L).build());
        when(cardMapper.toModel(any(CardEntity.class)))
                .thenReturn(Card.builder().id(1L).build());
        when(gameEventMapper.toModel(any(GameEventEntity.class)))
                .thenReturn(GameEvent.builder().id(1L).build());
        when(chatMessageMapper.toModel(any(ChatMessageEntity.class)))
                .thenReturn(ChatMessage.builder().id(1L).build());

        // When
        Game result = gameMapper.toModel(gameEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCreatedByUsername()).isNull();
    }

    @Test
    void toModel_ShouldMapTerritoriesCorrectly() {
        // Given
        when(playerMapper.toModel(any(PlayerEntity.class)))
                .thenReturn(Player.builder().id(1L).build());
        when(cardMapper.toModel(any(CardEntity.class)))
                .thenReturn(Card.builder().id(1L).build());
        when(gameEventMapper.toModel(any(GameEventEntity.class)))
                .thenReturn(GameEvent.builder().id(1L).build());
        when(chatMessageMapper.toModel(any(ChatMessageEntity.class)))
                .thenReturn(ChatMessage.builder().id(1L).build());

        // When
        Game result = gameMapper.toModel(gameEntity);

        // Then
        assertThat(result.getTerritories()).isNotNull();
        assertThat(result.getTerritories()).hasSize(1);

        List<Territory> territories = result.getTerritories();
        Territory territory = territories.get(1);

        assertThat(territory).isNotNull();
        assertThat(territory.getId()).isEqualTo(1L);
        assertThat(territory.getName()).isEqualTo("Test Country");
        assertThat(territory.getContinentName()).isEqualTo("Test Continent");
        assertThat(territory.getOwnerId()).isEqualTo(1L);
        assertThat(territory.getOwnerName()).isEqualTo("testuser");
        assertThat(territory.getArmies()).isEqualTo(5);
    }

    @Test
    void toModel_WithBotOwner_ShouldMapBotName() {
        // Given
        BotProfileEntity botProfile = new BotProfileEntity();
        botProfile.setBotName("Test Bot");

        playerEntity.setUser(null);
        playerEntity.setBotProfile(botProfile);

        when(playerMapper.toModel(any(PlayerEntity.class)))
                .thenReturn(Player.builder().id(1L).build());
        when(cardMapper.toModel(any(CardEntity.class)))
                .thenReturn(Card.builder().id(1L).build());
        when(gameEventMapper.toModel(any(GameEventEntity.class)))
                .thenReturn(GameEvent.builder().id(1L).build());
        when(chatMessageMapper.toModel(any(ChatMessageEntity.class)))
                .thenReturn(ChatMessage.builder().id(1L).build());

        // When
        Game result = gameMapper.toModel(gameEntity);

        // Then
        Territory territory = result.getTerritories().get(1);
        assertThat(territory.getOwnerName()).isEqualTo("Test Bot");
    }

    @Test
    void toModel_WithUnknownOwner_ShouldMapUnknown() {
        // Given
        playerEntity.setUser(null);
        playerEntity.setBotProfile(null);

        when(playerMapper.toModel(any(PlayerEntity.class)))
                .thenReturn(Player.builder().id(1L).build());
        when(cardMapper.toModel(any(CardEntity.class)))
                .thenReturn(Card.builder().id(1L).build());
        when(gameEventMapper.toModel(any(GameEventEntity.class)))
                .thenReturn(GameEvent.builder().id(1L).build());
        when(chatMessageMapper.toModel(any(ChatMessageEntity.class)))
                .thenReturn(ChatMessage.builder().id(1L).build());

        // When
        Game result = gameMapper.toModel(gameEntity);

        // Then
        Territory territory = result.getTerritories().get(1);
        assertThat(territory.getOwnerName()).isEqualTo("Unknown");
    }

    @Test
    void toModel_WithNullOwner_ShouldMapNull() {
        // Given
        territoryEntity.setOwner(null);

        when(playerMapper.toModel(any(PlayerEntity.class)))
                .thenReturn(Player.builder().id(1L).build());
        when(cardMapper.toModel(any(CardEntity.class)))
                .thenReturn(Card.builder().id(1L).build());
        when(gameEventMapper.toModel(any(GameEventEntity.class)))
                .thenReturn(GameEvent.builder().id(1L).build());
        when(chatMessageMapper.toModel(any(ChatMessageEntity.class)))
                .thenReturn(ChatMessage.builder().id(1L).build());

        // When
        Game result = gameMapper.toModel(gameEntity);

        // Then
        Territory territory = result.getTerritories().get(1);
        assertThat(territory.getOwnerId()).isNull();
        assertThat(territory.getOwnerName()).isNull();
    }

    @Test
    void toEntity_WithValidGame_ShouldMapCorrectly() {
        // When
        GameEntity result = gameMapper.toEntity(game);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getGameCode()).isEqualTo("TEST123");
        assertThat(result.getStatus()).isEqualTo(GameState.NORMAL_PLAY);
        assertThat(result.getCurrentPhase()).isEqualTo(TurnPhase.ATTACK);
        assertThat(result.getCurrentTurn()).isEqualTo(5);
        assertThat(result.getCurrentPlayerIndex()).isEqualTo(0);
        assertThat(result.getMaxPlayers()).isEqualTo(6);
        assertThat(result.getTurnTimeLimit()).isEqualTo(600);
        assertThat(result.getChatEnabled()).isTrue();
        assertThat(result.getPactsAllowed()).isFalse();
        assertThat(result.getCreatedAt()).isEqualTo(testTime);
        assertThat(result.getStartedAt()).isEqualTo(testTime.minusHours(1));
        assertThat(result.getFinishedAt()).isNull();
        assertThat(result.getLastModified()).isEqualTo(testTime);
    }

    @Test
    void toEntity_WithNullGame_ShouldReturnNull() {
        // When
        GameEntity result = gameMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toModel_WithEmptyCollections_ShouldHandleCorrectly() {
        // Given
        gameEntity.setPlayers(Arrays.asList());
        gameEntity.setTerritories(Arrays.asList());
        gameEntity.setDeck(Arrays.asList());
        gameEntity.setEvents(Arrays.asList());
        gameEntity.setChatMessages(Arrays.asList());

        // When
        Game result = gameMapper.toModel(gameEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPlayers()).isEmpty();
        assertThat(result.getTerritories()).isEmpty();
        assertThat(result.getDeck()).isEmpty();
        assertThat(result.getEvents()).isEmpty();
        assertThat(result.getChatMessages()).isEmpty();
    }

    @Test
    void mapTerritories_WithNeighbors_ShouldMapNeighborIds() {
        // Given
        CountryEntity neighbor = new CountryEntity();
        neighbor.setId(2L);
        neighbor.setName("Neighbor Country");

        countryEntity.setNeighbors(new HashSet<>(Arrays.asList(neighbor)));

        when(playerMapper.toModel(any(PlayerEntity.class)))
                .thenReturn(Player.builder().id(1L).build());
        when(cardMapper.toModel(any(CardEntity.class)))
                .thenReturn(Card.builder().id(1L).build());
        when(gameEventMapper.toModel(any(GameEventEntity.class)))
                .thenReturn(GameEvent.builder().id(1L).build());
        when(chatMessageMapper.toModel(any(ChatMessageEntity.class)))
                .thenReturn(ChatMessage.builder().id(1L).build());

        // When
        Game result = gameMapper.toModel(gameEntity);

        // Then
        Territory territory = result.getTerritories().get(1);
        assertThat(territory.getNeighborIds()).hasSize(1);
        assertThat(territory.getNeighborIds()).contains(2L);
    }

    @Test
    void toModel_RoundTripMapping_ShouldPreserveBasicData() {
        // Given
        when(playerMapper.toModel(any(PlayerEntity.class)))
                .thenReturn(Player.builder().id(1L).build());
        when(cardMapper.toModel(any(CardEntity.class)))
                .thenReturn(Card.builder().id(1L).build());
        when(gameEventMapper.toModel(any(GameEventEntity.class)))
                .thenReturn(GameEvent.builder().id(1L).build());
        when(chatMessageMapper.toModel(any(ChatMessageEntity.class)))
                .thenReturn(ChatMessage.builder().id(1L).build());

        // When
        Game mappedGame = gameMapper.toModel(gameEntity);
        GameEntity mappedBackEntity = gameMapper.toEntity(mappedGame);

        // Then
        assertThat(mappedBackEntity.getId()).isEqualTo(gameEntity.getId());
        assertThat(mappedBackEntity.getGameCode()).isEqualTo(gameEntity.getGameCode());
        assertThat(mappedBackEntity.getStatus()).isEqualTo(gameEntity.getStatus());
        assertThat(mappedBackEntity.getCurrentPhase()).isEqualTo(gameEntity.getCurrentPhase());
        assertThat(mappedBackEntity.getCurrentTurn()).isEqualTo(gameEntity.getCurrentTurn());
        assertThat(mappedBackEntity.getCurrentPlayerIndex()).isEqualTo(gameEntity.getCurrentPlayerIndex());
        assertThat(mappedBackEntity.getMaxPlayers()).isEqualTo(gameEntity.getMaxPlayers());
        assertThat(mappedBackEntity.getTurnTimeLimit()).isEqualTo(gameEntity.getTurnTimeLimit());
        assertThat(mappedBackEntity.getChatEnabled()).isEqualTo(gameEntity.getChatEnabled());
        assertThat(mappedBackEntity.getPactsAllowed()).isEqualTo(gameEntity.getPactsAllowed());
    }
}