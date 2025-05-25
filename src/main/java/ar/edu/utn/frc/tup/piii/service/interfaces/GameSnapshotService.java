package ar.edu.utn.frc.tup.piii.service.interfaces;

import ar.edu.utn.frc.tup.piii.model.entity.GameSnapshot;
import ar.edu.utn.frc.tup.piii.model.entity.Game;
import java.util.List;
import java.util.Optional;

public interface GameSnapshotService {

    // CRUD básico
    GameSnapshot save(GameSnapshot snapshot);
    Optional<GameSnapshot> findById(Long id);
    List<GameSnapshot> findAll();
    List<GameSnapshot> findByGame(Game game);
    void deleteById(Long id);

    // Creación y restauración de snapshots
    GameSnapshot createSnapshot(Game game);
    GameSnapshot createAutoSnapshot(Game game);
    void restoreFromSnapshot(Long gameId, Long snapshotId);
    GameSnapshot getLatestSnapshot(Long gameId);

    // Gestión automática
    void autoSaveSnapshot(Game game);
    void scheduleAutoSave(Game game);
    void cleanOldSnapshots(Long gameId, int keepLast);

    // Validaciones
    boolean canRestoreSnapshot(Long gameId, Long snapshotId);
    boolean isSnapshotValid(GameSnapshot snapshot);

    // Utilidades
    String serializeGameState(Game game);
    Game deserializeGameState(String serializedState);
    List<GameSnapshot> getSnapshotHistory(Long gameId);
    int getSnapshotCount(Long gameId);
}
