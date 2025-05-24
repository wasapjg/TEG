package ar.edu.utn.frc.tup.piii.model.entity;

import ar.edu.utn.frc.tup.piii.model.enums.BotLevel;
import jakarta.persistence.*;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ar.edu.utn.frc.tup.piii.model.enums.BotStrategy;

@Entity
@Table(name = "bot_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BotLevel level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BotStrategy strategy;

    @Column(name = "bot_name", nullable = false)
    private String botName;

    public static BotProfile create(BotLevel level, BotStrategy strategy) {
        BotProfile bot = new BotProfile();
        bot.setLevel(level);
        bot.setStrategy(strategy);
        bot.setBotName(generateBotName(level, strategy));
        return bot;
    }

    private static String generateBotName(BotLevel level, BotStrategy strategy) {
        return level.name().toLowerCase() + "_" + strategy.name().toLowerCase() + "_bot";
    }
}