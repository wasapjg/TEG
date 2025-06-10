package ar.edu.utn.frc.tup.piii.utils;

import ar.edu.utn.frc.tup.piii.entities.GameEntity;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.model.Game;
import ar.edu.utn.frc.tup.piii.model.Player;
import ar.edu.utn.frc.tup.piii.model.enums.PlayerColor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ColorManager {

    @Autowired
    private Random random;

    // Reemplazamos Game por GameEntity:
    public PlayerColor getAvailableRandomColor(GameEntity gameEntity) {
        Set<PlayerColor> usedColors = gameEntity.getPlayers().stream()
                .map(PlayerEntity::getColor)
                .collect(Collectors.toSet());

        List<PlayerColor> availableColors = Arrays.stream(PlayerColor.values())
                .filter(color -> !usedColors.contains(color))
                .collect(Collectors.toList());

        return availableColors.isEmpty() ?
                PlayerColor.values()[random.nextInt(PlayerColor.values().length)] :
                availableColors.get(random.nextInt(availableColors.size()));
    }
}
