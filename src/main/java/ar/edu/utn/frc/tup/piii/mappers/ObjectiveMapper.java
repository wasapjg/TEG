package ar.edu.utn.frc.tup.piii.mappers;

import ar.edu.utn.frc.tup.piii.entities.ObjectiveEntity;
import ar.edu.utn.frc.tup.piii.model.Objective;
import org.springframework.stereotype.Component;

@Component
public class ObjectiveMapper {

    public Objective toModel(ObjectiveEntity entity) {
        if (entity == null) return null;

        return Objective.builder()
                .id(entity.getId())
                .type(entity.getType())
                .description(entity.getDescription())
                .targetData(entity.getTargetData())
                .isCommon(entity.getIsCommon())
                .isAchieved(false) // Se calculará dinámicamente
                .build();
    }

    public ObjectiveEntity toEntity(Objective model) {
        if (model == null) return null;

        ObjectiveEntity entity = new ObjectiveEntity();
        entity.setId(model.getId());
        entity.setType(model.getType());
        entity.setDescription(model.getDescription());
        entity.setTargetData(model.getTargetData());
        entity.setIsCommon(model.getIsCommon());

        return entity;
    }
}
