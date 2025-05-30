package ar.edu.utn.frc.tup.piii.mappers;
import ar.edu.utn.frc.tup.piii.entities.PlayerEntity;
import ar.edu.utn.frc.tup.piii.model.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class PlayerMapper {

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private ObjectiveMapper objectiveMapper;

    public Player toModel(PlayerEntity entity) {
        if (entity == null) return null;

        return Player.builder()
                .id(entity.getId())
                .username(entity.getUser() != null ? entity.getUser().getUsername() : null)
                .displayName(getDisplayName(entity))
                .isBot(entity.getBotProfile() != null)
                .botLevel(entity.getBotProfile() != null ? entity.getBotProfile().getLevel() : null)
                .status(entity.getStatus())
                .color(entity.getColor())
                .armiesToPlace(entity.getArmiesToPlace())
                .seatOrder(entity.getSeatOrder())
                .joinedAt(entity.getJoinedAt())
                .eliminatedAt(entity.getEliminatedAt())
                .objective(objectiveMapper.toModel(entity.getObjective()))
                .hand(entity.getHand().stream()
                        .map(cardMapper::toModel)
                        .collect(Collectors.toList()))
                .territoryIds(entity.getTerritories().stream()
                        .map(territory -> territory.getCountry().getId())
                        .collect(Collectors.toList()))
                .build();
    }

    public PlayerEntity toEntity(Player model) {
        if (model == null) return null;

        PlayerEntity entity = new PlayerEntity();
        entity.setId(model.getId());
        entity.setStatus(model.getStatus());
        entity.setColor(model.getColor());
        entity.setArmiesToPlace(model.getArmiesToPlace());
        entity.setSeatOrder(model.getSeatOrder());
        entity.setJoinedAt(model.getJoinedAt());
        entity.setEliminatedAt(model.getEliminatedAt());

        return entity;
    }

    private String getDisplayName(PlayerEntity entity) {
        if (entity.getUser() != null) {
            return entity.getUser().getUsername();
        } else if (entity.getBotProfile() != null) {
            return entity.getBotProfile().getBotName();
        }
        return "Unknown Player";
    }
}
