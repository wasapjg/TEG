package ar.edu.utn.frc.tup.piii.service.impl;

import ar.edu.utn.frc.tup.piii.model.entity.Game;
import ar.edu.utn.frc.tup.piii.model.entity.GameSnapshot;
import ar.edu.utn.frc.tup.piii.repository.GameRepository;
import ar.edu.utn.frc.tup.piii.repository.GameSnapshotRepository;
import ar.edu.utn.frc.tup.piii.service.interfaces.GameSnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameSnapshotServiceImpl implements GameSnapshotService {

    private final GameSnapshotRepository snapshotRepository;
    private final GameRepository gameRepository;
    private final GameSnapshotRepository gameSnapshotRepository;

    @Autowired
    public GameSnapshotServiceImpl(GameSnapshotRepository snapshotRepository, GameRepository gameRepository, GameSnapshotRepository gameSnapshotRepository){
        this.snapshotRepository = snapshotRepository;
        this.gameRepository = gameRepository;
        this.gameSnapshotRepository = gameSnapshotRepository;
    }

    @Override
    public GameSnapshot save(GameSnapshot snapshot) {
        return snapshotRepository.save(snapshot);
    }

    @Override
    public Optional<GameSnapshot> findById(Long id) {
        return snapshotRepository.findById(id);
    }

    @Override
    public List<GameSnapshot> findAll() {
        return snapshotRepository.findAll();
    }

    @Override
    public List<GameSnapshot> findByGame(Game game) {
        return snapshotRepository.findGameOrderByTurnNumberAsc(game);
    }

    @Override
    public void deleteById(Long id) {
        snapshotRepository.deleteById(id);
    }

    @Override
    public GameSnapshot createSnapshot(Game game) {
        GameSnapshot snapshot = GameSnapshot.createFrom(game);
        snapshot.setCreatedBySystem(false);
        snapshot.setSerializedState(serializeGameState(game));
        return save(snapshot);
    }

    @Override
    public GameSnapshot createAutoSnapshot(Game game) {
        GameSnapshot snapshot = GameSnapshot.createFrom(game);
        snapshot.setCreatedBySystem(true);
        snapshot.setSerializedState(serializeGameState(game));
        return save(snapshot);
    }

    @Override
    public void restoreFromSnapshot(Long gameId, Long snapshotId) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));
        GameSnapshot snapshot = snapshotRepository.findById(snapshotId).orElseThrow(() -> new IllegalArgumentException("Snapshot no encontrado"));

        Game restored = deserializeGameState(snapshot.getSerializedState());
        restored.setId(gameId); // importante mantener el ID del juego original

        gameRepository.save(restored);
    }

    @Override
    public GameSnapshot getLatestSnapshot(Long gameId) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));
        return snapshotRepository.findTopByGameOrderByTurnNumberDesc(game)
                .orElseThrow(() -> new IllegalStateException("No hay snapshots disponibles"));
    }

    @Override
    public void autoSaveSnapshot(Game game) {
        createAutoSnapshot(game);
    }

    @Override
    public void scheduleAutoSave(Game game) {
        //TODO: como manejar tareas programadas?
        autoSaveSnapshot(game);
    }

    @Override
    public void cleanOldSnapshots(Long gameId, int keepLast) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));
        List<GameSnapshot> snapshots = snapshotRepository.findByGameOrderByTurnNumberAsc(game);
        if (snapshots.size() <= keepLast) return;

        List<GameSnapshot> toDelete = snapshots.subList(0, snapshots.size() - keepLast);
        toDelete.forEach(s -> snapshotRepository.deleteById(s.getId()));
    }

    @Override
    public boolean canRestoreSnapshot(Long gameId, Long snapshotId) {
        Optional<GameSnapshot> snapshot = snapshotRepository.findById(snapshotId);
        return snapshot.map(s -> s.getGame().getId().equals(gameId)).orElse(false);
    }

    @Override
    public boolean isSnapshotValid(GameSnapshot snapshot) {
        return snapshot != null && snapshot.getSerializedState() != null && !snapshot.getSerializedState().isBlank();
    }

    @Override
    public String serializeGameState(Game game) {
        //TODO: no se qué haría esta función
        /*
         try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(game);
         } catch (Exception e) {
            throw new RuntimeException("Error al serializar el estado del juego", e);
         }
         *
         */
        return "";
    }

    @Override
    public Game deserializeGameState(String serializedState) {
        //TODO: lo mismo que la anterior
        /*
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(serializedState, Game.class);
        } catch (Exception e) {
            throw new RuntimeException("Error al deserializar el estado del juego", e);
        }
         */
        return null;
    }

    @Override
    public List<GameSnapshot> getSnapshotHistory(Long gameId) {
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));
        return snapshotRepository.findByGameOrderByTurnNumberAsc(game);
    }

    @Override
    public int getSnapshotCount(Long gameId) {
        return getSnapshotHistory(gameId).size();
    }
}
