package ar.edu.utn.frc.tup.piii.mappers;
import ar.edu.utn.frc.tup.piii.dtos.country.CountryResponseDto;
import ar.edu.utn.frc.tup.piii.dtos.game.GameResponseDto;
import ar.edu.utn.frc.tup.piii.entities.*;
import ar.edu.utn.frc.tup.piii.model.*;
import ar.edu.utn.frc.tup.piii.model.enums.GameState;
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
    @Autowired private TerritoryMapper territoryMapper;
    @Autowired private ContinentMapper continentMapper;       // para continentes
    @Autowired private ChatMessageMapper chatMessageMapperDto;


    public Game toModel(GameEntity entity) {
        if (entity == null) return null;

        return Game.builder()
                .id(entity.getId())
                .gameCode(entity.getGameCode())
                .createdByUsername(entity.getCreatedBy() != null ? entity.getCreatedBy().getUsername() : null)
                .state(entity.getStatus())
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
        entity.setStatus(model.getState());
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


    public GameResponseDto toResponseDto(Game model) {
        if (model == null) {
            return null;
        }

        GameResponseDto.GameResponseDtoBuilder builder = GameResponseDto.builder()
                .id(model.getId())
                .gameCode(model.getGameCode())
                .createdByUsername(model.getCreatedByUsername())
                .state(model.getState() != null ? model.getState() : null)            // GameState
                .currentPhase(model.getCurrentPhase() != null ? model.getCurrentPhase() : null) // TurnPhase
                .currentTurn(model.getCurrentTurn())
                .currentPlayerIndex(model.getCurrentPlayerIndex())
                .maxPlayers(model.getMaxPlayers())
                .turnTimeLimit(model.getTurnTimeLimit())
                .chatEnabled(model.getChatEnabled())
                .pactsAllowed(model.getPactsAllowed())
                .createdAt(model.getCreatedAt())
                .startedAt(model.getStartedAt())
                .finishedAt(model.getFinishedAt())
                .currentPlayerName(model.getCreatedByUsername())
//              .winnerName(model.getWinnerName())

                .canStart(
                        model.getState() == GameState.WAITING_FOR_PLAYERS &&
                                model.getPlayers() != null &&
                                model.getPlayers().size() >= 2
                )

                .isGameOver(model.getState() == GameState.FINISHED);

        // 1) Lista de jugadores:
        if (model.getPlayers() != null && !model.getPlayers().isEmpty()) {
            builder.players(
                    model.getPlayers().stream()
                            .map(playerMapper::toResponseDto)
                            .collect(Collectors.toList())
            );
        }
        // 2) Mapear territorios

        if (model.getTerritories() != null && !model.getTerritories().isEmpty()) {
            Map<Long, CountryResponseDto> mappedTerritories =
                    model.getTerritories().entrySet().stream()
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    entry -> territoryMapper.toResponseDto(entry.getValue())
                            ));

            builder.territories(mappedTerritories);
        }
//         3) Mapear continentes

//         if (model.getContinents() != null && !model.getContinents().isEmpty()) {
//             builder.continents(
//                 model.getContinents().stream()
//                     .map(continentMapper::toResponseDto)
//                     .collect(Collectors.toList())
//             );
//         }
//         4) Mapear eventos recientes
        if (model.getEvents() != null && !model.getEvents().isEmpty()) {
            builder.recentEvents(
                    model.getEvents().stream()
                            .map(gameEventMapper::toDto)
                            .collect(Collectors.toList())
            );
        }
//         5) Mapear mensajes de chat
        if (model.getChatMessages() != null && !model.getChatMessages().isEmpty()) {
            builder.recentMessages(
                    model.getChatMessages().stream()
                            .map(chatMessageMapper::toResponseDto)
                            .collect(Collectors.toList())
            );
        }

        return builder.build();
    }



}