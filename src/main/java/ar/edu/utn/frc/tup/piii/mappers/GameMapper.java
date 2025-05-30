package ar.edu.utn.frc.tup.piii.mappers;
import ar.edu.utn.frc.tup.piii.entities.*;
import ar.edu.utn.frc.tup.piii.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@Component
public class GameMapper {

    @Autowired
    private PlayerMapper playerMapper;

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private GameEventMapper gameEventMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    public Game toModel(GameEntity entity) {
        if (entity == null) return null;

        return Game.builder()
                .id(entity.getId())
                .gameCode(entity.getGameCode())
                .createdByUsername(entity.getCreatedBy() != null ? entity.getCreatedBy().getUsername() : null)
                .status(entity.getStatus())
                .currentPhase(entity.getCurrentPhase())
                .currentTurn(entity.getCurrentTurn())
                .currentPlayerIndex(entity.getCurrentPlayerIndex())
                .maxPlayers(entity.getMaxPlayers())
                .turnTimeLimit(entity.getTurnTimeLimit())
                .chatEnabled(entity.getChatEnabled())
                .pactsAllowed(entity.getPactsAllowed())
                .createdAt(entity.getCreatedAt())
                .startedAt(entity.getStartedAt())
                .finishedAt(entity.getFinishedAt())
                .lastModified(entity.getLastModified())
                .players(entity.getPlayers().stream()
                        .map(playerMapper::toModel)
                        .collect(Collectors.toList()))
                .territories(mapTerritories(entity.getTerritories()))
                .deck(entity.getDeck().stream()
                        .map(cardMapper::toModel)
                        .collect(Collectors.toList()))
                .events(entity.getEvents().stream()
                        .map(gameEventMapper::toModel)
                        .collect(Collectors.toList()))
                .chatMessages(entity.getChatMessages().stream()
                        .map(chatMessageMapper::toModel)
                        .collect(Collectors.toList()))
                .build();
    }

    public GameEntity toEntity(Game model) {
        if (model == null) return null;

        GameEntity entity = new GameEntity();
        entity.setId(model.getId());
        entity.setGameCode(model.getGameCode());
        entity.setStatus(model.getStatus());
        entity.setCurrentPhase(model.getCurrentPhase());
        entity.setCurrentTurn(model.getCurrentTurn());
        entity.setCurrentPlayerIndex(model.getCurrentPlayerIndex());
        entity.setMaxPlayers(model.getMaxPlayers());
        entity.setTurnTimeLimit(model.getTurnTimeLimit());
        entity.setChatEnabled(model.getChatEnabled());
        entity.setPactsAllowed(model.getPactsAllowed());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setStartedAt(model.getStartedAt());
        entity.setFinishedAt(model.getFinishedAt());
        entity.setLastModified(model.getLastModified());

        return entity;
    }

    private Map<Long, Territory> mapTerritories(java.util.List<GameTerritoryEntity> territoryEntities) {
        Map<Long, Territory> territories = new HashMap<>();
        for (GameTerritoryEntity entity : territoryEntities) {
            Territory territory = Territory.builder()
                    .id(entity.getCountry().getId())
                    .name(entity.getCountry().getName())
                    .continentName(entity.getCountry().getContinent().getName())
                    .ownerId(entity.getOwner() != null ? entity.getOwner().getId() : null)
                    .ownerName(entity.getOwner() != null ? getPlayerDisplayName(entity.getOwner()) : null)
                    .armies(entity.getArmies())
                    .positionX(entity.getCountry().getPositionX())
                    .positionY(entity.getCountry().getPositionY())
                    .neighborIds(entity.getCountry().getNeighbors().stream()
                            .map(CountryEntity::getId)
                            .collect(Collectors.toSet()))
                    .build();
            territories.put(territory.getId(), territory);
        }
        return territories;
    }

    private String getPlayerDisplayName(PlayerEntity player) {
        if (player.getUser() != null) {
            return player.getUser().getUsername();
        } else if (player.getBotProfile() != null) {
            return player.getBotProfile().getBotName();
        }
        return "Unknown";
    }
}