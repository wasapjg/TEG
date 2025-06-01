package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.entities.CardEntity;
import ar.edu.utn.frc.tup.piii.model.Card;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

    public Card toModel(CardEntity entity) {
        if (entity == null) return null;

        return Card.builder()
                .id(entity.getId())
                .countryName(entity.getCountry() != null ? entity.getCountry().getName() : null)
                .type(entity.getType())
                .ownerId(entity.getOwner() != null ? entity.getOwner().getId() : null)
                .isInDeck(entity.getIsInDeck())
                .build();
    }

    public CardEntity toEntity(Card model) {
        if (model == null) return null;

        CardEntity entity = new CardEntity();
        entity.setId(model.getId());
        entity.setType(model.getType());
        entity.setIsInDeck(model.getIsInDeck());

        return entity;
    }
}