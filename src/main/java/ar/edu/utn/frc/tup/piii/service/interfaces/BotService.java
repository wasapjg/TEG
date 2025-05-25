package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.model.entity.BotProfile;
import ar.edu.utn.frc.tup.piii.model.entity.Country;
import ar.edu.utn.frc.tup.piii.model.entity.Player;
import ar.edu.utn.frc.tup.piii.model.entity.Game;
import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;
import java.util.List;
import java.util.Optional;

public interface BotService {

    // CRUD básico
    BotProfile save(BotProfile botProfile);
    Optional<BotProfile> findById(Long id);
    List<BotProfile> findAll();
    List<BotProfile> findByLevel(BotLevel level);
    void deleteById(Long id);

    // Creación de bots
    BotProfile createBot(BotLevel level, BotStrategy strategy);
    Player createBotPlayer(BotLevel level, Game game);

    // Inteligencia artificial
    void executeBotTurn(Player botPlayer, Game game);
    void performBotAttack(Player botPlayer, Game game);
    void performBotReinforcement(Player botPlayer, Game game);
    void performBotFortify(Player botPlayer, Game game);

    // Estrategias
    void executeNoviceStrategy(Player botPlayer, Game game);
    void executeBalancedStrategy(Player botPlayer, Game game);
    void executeExpertStrategy(Player botPlayer, Game game);

    // Evaluación
    double evaluateAttackProbability(int attackerArmies, int defenderArmies);
    List<Country> getBestAttackTargets(Player botPlayer, Game game);
    List<Country> getBestDefensePositions(Player botPlayer, Game game);
}