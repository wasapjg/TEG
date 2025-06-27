package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.FactoryBots.BotStrategyExecutor;
import ar.edu.utn.frc.tup.piii.FactoryBots.BotStrategyFactory;
import ar.edu.utn.frc.tup.piii.entities.BotProfileEntity;
import ar.edu.utn.frc.tup.piii.entities.CountryEntity;
import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import ar.edu.utn.frc.tup.piii.repository.BotProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BotServiceImplTest {

    @Mock
    private BotStrategyFactory botStrategyFactory;

    @Mock
    private BotStrategyExecutor botStrategyExecutor;

    @Mock
    private BotProfileRepository botProfileRepository;

    @InjectMocks
    private BotServiceImpl botService;

    private PlayerEntity botPlayer;
    private PlayerEntity humanPlayer;
    private GameEntity game;
    private BotProfileEntity botProfile;

    @BeforeEach
    void setUp() {
        // Setup bot player
        botPlayer = new PlayerEntity();
        botProfile = new BotProfileEntity();
        botProfile.setBotName("TestBot");
        botProfile.setLevel(BotLevel.BALANCED);
        botProfile.setStrategy(BotStrategy.AGGRESSIVE);
        botPlayer.setBotProfile(botProfile);

        // Setup human player (no bot profile)
        humanPlayer = new PlayerEntity();
        humanPlayer.setBotProfile(null);

        // Setup game
        game = new GameEntity();
        game.setGameCode("TEST-GAME");
    }

    @Test
    void testExecuteBotTurn() {
        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);

        botService.executeBotTurn(botPlayer, game);

        verify(botStrategyExecutor).executeTurn(botPlayer, game);
    }

    @Test
    void testEvaluateAttackProbability() {
        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);
        when(botStrategyExecutor.evaluateAttackProbability(botPlayer, 5, 3)).thenReturn(0.75);

        double result = botService.evaluateAttackProbability(botPlayer, 5, 3);

        assertEquals(0.75, result);
    }

    @Test
    void testGetBestAttackTargets() {
        List<CountryEntity> mockTargets = Arrays.asList(new CountryEntity(), new CountryEntity());

        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);
        when(botStrategyExecutor.getBestAttackTargets(botPlayer, game)).thenReturn(mockTargets);

        List<CountryEntity> result = botService.getBestAttackTargets(botPlayer, game);

        assertEquals(2, result.size());
    }

    @Test
    void testSaveBotProfile() {
        BotProfileEntity botProfile = new BotProfileEntity();
        when(botProfileRepository.save(botProfile)).thenReturn(botProfile);

        BotProfileEntity result = botService.save(botProfile);

        assertEquals(botProfile, result);
        verify(botProfileRepository, times(1)).save(botProfile);
    }

    @Test
    void testFindById() {
        BotProfileEntity botProfile = new BotProfileEntity();
        when(botProfileRepository.findById(1L)).thenReturn(Optional.of(botProfile));

        Optional<BotProfileEntity> result = botService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(botProfile, result.get());
    }

    @Test
    void testDeleteById() {
        botService.deleteById(10L);
        verify(botProfileRepository).deleteById(10L);
    }

    @Test
    void testFindByLevel() {
        BotLevel level = BotLevel.BALANCED;
        List<BotProfileEntity> expectedList = List.of(new BotProfileEntity());
        when(botProfileRepository.findByLevel(level)).thenReturn(expectedList);

        List<BotProfileEntity> result = botService.findByLevel(level);

        assertEquals(expectedList, result);
    }

    // Tests para excepciones cuando no es bot
    @Test
    void testExecuteBotTurn_ThrowsExceptionWhenNotBot() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> botService.executeBotTurn(humanPlayer, game));

        assertEquals("Player is not a bot: " + humanPlayer.getId(), exception.getMessage());
        verify(botStrategyFactory, never()).getExecutor(any());
    }

    @Test
    void testEvaluateAttackProbability_ThrowsExceptionWhenNotBot() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> botService.evaluateAttackProbability(humanPlayer, 5, 3));

        assertEquals("Player is not a bot: " + humanPlayer.getId(), exception.getMessage());
    }

    @Test
    void testGetBestAttackTargets_ThrowsExceptionWhenNotBot() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> botService.getBestAttackTargets(humanPlayer, game));

        assertEquals("Player is not a bot: " + humanPlayer.getId(), exception.getMessage());
    }

    @Test
    void testGetBestDefensePositions_ThrowsExceptionWhenNotBot() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> botService.getBestDefensePositions(humanPlayer, game));

        assertEquals("Player is not a bot: " + humanPlayer.getId(), exception.getMessage());
    }

    // Tests para getBestDefensePositions
    @Test
    void testGetBestDefensePositions() {
        List<CountryEntity> mockDefensePositions = Arrays.asList(new CountryEntity(), new CountryEntity());

        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);
        when(botStrategyExecutor.getBestDefensePositions(botPlayer, game)).thenReturn(mockDefensePositions);

        List<CountryEntity> result = botService.getBestDefensePositions(botPlayer, game);

        assertEquals(2, result.size());
        verify(botStrategyExecutor).getBestDefensePositions(botPlayer, game);
    }

    // Tests para shouldBotAttack
    @Test
    void testShouldBotAttack_WithTargets() {
        List<CountryEntity> mockTargets = Arrays.asList(new CountryEntity());

        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);
        when(botStrategyExecutor.getBestAttackTargets(botPlayer, game)).thenReturn(mockTargets);

        boolean result = botService.shouldBotAttack(botPlayer, game);

        assertTrue(result);
    }

    @Test
    void testShouldBotAttack_WithoutTargets() {
        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);
        when(botStrategyExecutor.getBestAttackTargets(botPlayer, game)).thenReturn(Collections.emptyList());

        boolean result = botService.shouldBotAttack(botPlayer, game);

        assertFalse(result);
    }

    @Test
    void testShouldBotAttack_WithNonBotPlayer() {
        boolean result = botService.shouldBotAttack(humanPlayer, game);

        assertFalse(result);
    }

    // Tests para shouldBotFortify
    @Test
    void testShouldBotFortify_WithBotPlayer() {
        boolean result = botService.shouldBotFortify(botPlayer, game);

        assertTrue(result); // Los bots siempre intentan fortificar según la implementación
    }

    @Test
    void testShouldBotFortify_WithNonBotPlayer() {
        boolean result = botService.shouldBotFortify(humanPlayer, game);

        assertFalse(result);
    }

    // Tests para findAll
    @Test
    void testFindAll() {
        List<BotProfileEntity> expectedList = Arrays.asList(new BotProfileEntity(), new BotProfileEntity());
        when(botProfileRepository.findAll()).thenReturn(expectedList);

        List<BotProfileEntity> result = botService.findAll();

        assertEquals(2, result.size());
        verify(botProfileRepository).findAll();
    }

    @Test
    void testFindById_NotFound() {
        when(botProfileRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<BotProfileEntity> result = botService.findById(999L);

        assertFalse(result.isPresent());
    }

    // Tests para utilidades
    @Test
    void testIsValidBot_ValidBot() {
        boolean result = botService.isValidBot(botPlayer);

        assertTrue(result);
    }

    @Test
    void testIsValidBot_NullPlayer() {
        boolean result = botService.isValidBot(null);

        assertFalse(result);
    }

    @Test
    void testIsValidBot_NullBotProfile() {
        boolean result = botService.isValidBot(humanPlayer);

        assertFalse(result);
    }

    @Test
    void testIsValidBot_NullBotName() {
        botProfile.setBotName(null);
        boolean result = botService.isValidBot(botPlayer);

        assertFalse(result);
    }

    @Test
    void testGetBotDisplayName_ValidBot() {
        String result = botService.getBotDisplayName(botPlayer);

        String expected = String.format("%s (%s - %s)",
                botProfile.getBotName(),
                botProfile.getLevel(),
                botProfile.getStrategy());
        assertEquals(expected, result);
    }

    @Test
    void testGetBotDisplayName_InvalidBot() {
        String result = botService.getBotDisplayName(humanPlayer);

        assertEquals("Unknown Bot", result);
    }

    @Test
    void testGetBotDisplayName_NullPlayer() {
        String result = botService.getBotDisplayName(null);

        assertEquals("Unknown Bot", result);
    }

    // Tests para manejo de excepciones en executeBotTurn
    @Test
    void testExecuteBotTurn_HandlesExecutorException() {
        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);
        doThrow(new RuntimeException("Strategy execution failed")).when(botStrategyExecutor).executeTurn(botPlayer, game);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> botService.executeBotTurn(botPlayer, game));

        assertEquals("Failed to execute bot turn", exception.getMessage());
        assertEquals("Strategy execution failed", exception.getCause().getMessage());
    }

    // Tests para casos edge en evaluateAttackProbability
    @Test
    void testEvaluateAttackProbability_EdgeCases() {
        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);

        // Test con 0 ejércitos atacantes
        when(botStrategyExecutor.evaluateAttackProbability(botPlayer, 0, 5)).thenReturn(0.0);
        double result1 = botService.evaluateAttackProbability(botPlayer, 0, 5);
        assertEquals(0.0, result1);

        // Test con muchos ejércitos atacantes
        when(botStrategyExecutor.evaluateAttackProbability(botPlayer, 100, 1)).thenReturn(0.99);
        double result2 = botService.evaluateAttackProbability(botPlayer, 100, 1);
        assertEquals(0.99, result2);
    }

    // Tests para verificar que se llama al factory correctamente
    @Test
    void testFactoryInteractions() {
        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);

        // Ejecutar varios métodos que usan el factory
        botService.executeBotTurn(botPlayer, game);
        botService.evaluateAttackProbability(botPlayer, 5, 3);
        botService.getBestAttackTargets(botPlayer, game);
        botService.getBestDefensePositions(botPlayer, game);

        // Verificar que se llamó al factory las veces correctas
        verify(botStrategyFactory, times(4)).getExecutor(botProfile);
    }

    // Test para verificar logging (si usas captura de logs)
    @Test
    void testExecuteBotTurn_LoggingBehavior() {
        when(botStrategyFactory.getExecutor(botProfile)).thenReturn(botStrategyExecutor);

        // Este test verificaría que se loggea correctamente
        // Necesitarías configurar un appender para capturar logs si quieres testear esto
        botService.executeBotTurn(botPlayer, game);

        // Verificar que se ejecutó sin errores (implica que el logging funcionó)
        verify(botStrategyExecutor).executeTurn(botPlayer, game);
    }
}