package ar.edu.utn.frc.tup.piii.utils;

import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ColorManager {
    private static final Random RANDOM = new Random();

    public PlayerColor getAvailableRandomColor(Game game) {
        // Obtener colores ya usados
        Set<PlayerColor> usedColors = game.getPlayers().stream()
                .map(Player::getColor)
                .collect(Collectors.toSet());

        // Filtrar colores disponibles
        List<PlayerColor> availableColors = Arrays.stream(PlayerColor.values())
                .filter(color -> !usedColors.contains(color))
                .collect(Collectors.toList());

        // Si no hay disponibles, devolver uno aleatorio (puede repetirse)
        return availableColors.isEmpty() ?
                PlayerColor.values()[RANDOM.nextInt(PlayerColor.values().length)] :
                availableColors.get(RANDOM.nextInt(availableColors.size()));
    }
}