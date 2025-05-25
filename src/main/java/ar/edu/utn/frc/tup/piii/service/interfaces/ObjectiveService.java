package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.model.entity.Objective;
import ar.edu.utn.frc.tup.piii.model.entity.Player;
import ar.edu.utn.frc.tup.piii.model.entity.Game;
import ar.edu.utn.frc.tup.piii.model.enums.ObjectiveType;
import java.util.List;
import java.util.Optional;

public interface ObjectiveService {

    // CRUD básico
    Objective save(Objective objective);
    Optional<Objective> findById(Long id);
    List<Objective> findAll();
    List<Objective> findByType(ObjectiveType type);
    void deleteById(Long id);

    // Gestión de objetivos
    List<Objective> createObjectivesForGame(Game game);
    void assignObjectivesToPlayers(Game game);
    boolean isObjectiveAchieved(Long objectiveId, Game game, Player player);

    // Tipos de objetivos
    List<Objective> getCommonObjectives();
    List<Objective> getOccupationObjectives();
    List<Objective> getDestructionObjectives();

    // Validaciones
    boolean validateObjectiveCompletion(Objective objective, Game game, Player player);
    String getObjectiveProgress(Long objectiveId, Game game, Player player);
}
